package di

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
class Module {

    lateinit var scope: Scope
    val instanceMap: MutableMap<KClass<*>, Any> = mutableMapOf()
    val instanceFactory: MutableMap<KClass<*>, Factory<*>> = mutableMapOf()

    /**
     * Provide a singleton definition
     */
    inline fun <reified T> bean(noinline definition: () -> T) =
        provide(true, definition)

    /**
     * Provide a factory definition
     * (recreates instance each time)
     */
    inline fun <reified T> factory(noinline definition: () -> T) =
        provide(false, definition)

    inline fun <reified T> provide(isSingleton: Boolean = true, noinline definition: () -> T) {
        instanceFactory[T::class] = Factory(isSingleton, definition)
    }

    inline fun <reified T : Any> get(): T {
        val factory = scope.resolveFactory<T>(T::class)
        return scope.resolveInstance(factory, T::class)
    }

    fun <T : Any> resolveInstance(factory: Factory<T>, type: KClass<*>): T {
        return if (factory.isSingleton)
            instanceMap.getOrPut(type) { factory.instance() } as T
        else
            factory.instance()
    }

    fun <T> resolveFactory(type: KClass<*>): Factory<T> =
        instanceFactory[type] as Factory<T>?
                ?: throw NoFactoryFoundException(type)

    fun hasDefinition(type: KClass<*>): Boolean = instanceFactory[type] != null

}

data class Factory<T>(val isSingleton: Boolean, val definition: () -> T) {
    fun instance() = definition.invoke()
}

class NoFactoryFoundException(clazz: KClass<*>) : Exception("No factory registered for $clazz")

open class Scope {

    private var parent: Scope? = null
    private var modules: List<Module> = listOf()

    fun init(modules: List<Module>, parent: Scope? = null): Scope {
        this.parent = parent
        this.modules = modules
        modules.forEach { it.scope = this }
        return this
    }

    fun <T : Any> get(type: KClass<*>): T = resolveInstance(resolveFactory(type), type)

    fun <T : Any> resolveInstance(factory: Factory<T>, type: KClass<*>): T =
        resolveInstanceInternal(factory, type) ?: throw NoFactoryFoundException(type)

    private fun <T : Any> resolveInstanceInternal(factory: Factory<T>, type: KClass<*>): T? {
        val instance =
            modules.firstOrNull { it.hasDefinition(type) }?.resolveInstance(factory, type)

        if (instance != null)
            return instance

        return parent?.resolveInstanceInternal(factory, type)
    }

    fun <T> resolveFactory(type: KClass<*>): Factory<T> =
        resolveFactoryInternal(type) ?: throw NoFactoryFoundException(type)

    private fun <T> resolveFactoryInternal(type: KClass<*>): Factory<T>? {
        val factory = modules.firstOrNull { it.hasDefinition(type) }?.resolveFactory<T>(type)

        if (factory != null)
            return factory

        return parent?.resolveFactory(type)
    }
}
