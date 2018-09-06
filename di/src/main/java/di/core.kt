package di

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
class Module {

    lateinit var scope: Scope
    private val instanceMap: MutableMap<BeanDefinition, Any> = mutableMapOf()
    val instanceFactory: MutableMap<BeanDefinition, Factory<*>> = mutableMapOf()

    /**
     * Provide a singleton definition
     */
    inline fun <reified T> bean(name: String = "", noinline definition: () -> T) =
        provide(true, BeanDefinition(name, T::class), definition)

    /**
     * Provide a factory definition
     * (recreates instance each time)
     */
    inline fun <reified T> factory(name: String = "", noinline definition: () -> T) =
        provide(false, BeanDefinition(name, T::class), definition)

    inline fun <reified T> provide(
        isSingleton: Boolean = true,
        beanDefinition: BeanDefinition,
        noinline definition: () -> T
    ) {
        instanceFactory[beanDefinition] = Factory(isSingleton, definition)
    }

    inline fun <reified T : Any> get(name: String = ""): T {
        val factory = scope.resolveFactory<T>(name, T::class)
        return scope.resolveInstance(factory, name, T::class)
    }

    fun <T : Any> resolveInstance(factory: Factory<T>, name: String, type: KClass<*>): T {
        return if (factory.isSingleton)
            instanceMap.getOrPut(BeanDefinition(name, type)) { factory.instance() } as T
        else
            factory.instance()
    }

    fun <T> resolveFactory(name: String, type: KClass<*>): Factory<T> =
        instanceFactory[BeanDefinition(name, type)] as Factory<T>?
            ?: throw NoFactoryFoundException(type)


    fun hasDefinition(name: String, type: KClass<*>): Boolean =
        instanceFactory.contains(BeanDefinition(name, type))

}

open class Scope {

    private var parent: Scope? = null
    private var modules: List<Module> = listOf()

    fun init(modules: List<Module>, parent: Scope? = null): Scope {
        this.parent = parent
        this.modules = modules
        modules.forEach { it.scope = this }
        return this
    }

    fun <T : Any> get(name: String = "", type: KClass<*>): T =
        resolveInstance(resolveFactory(name, type), name, type)

    fun <T : Any> resolveInstance(factory: Factory<T>, name: String, type: KClass<*>): T =
        resolveInstanceInternal(factory, name, type) ?: throw NoFactoryFoundException(type)

    private fun <T : Any> resolveInstanceInternal(
        factory: Factory<T>,
        name: String,
        type: KClass<*>
    ): T? {
        val instance =
            modules.firstOrNull { it.hasDefinition(name, type) }
                ?.resolveInstance(factory, name, type)

        if (instance != null)
            return instance

        return parent?.resolveInstanceInternal(factory, name, type)
    }

    fun <T> resolveFactory(name: String, type: KClass<*>): Factory<T> =
        resolveFactoryInternal(name, type) ?: throw NoFactoryFoundException(type)

    private fun <T> resolveFactoryInternal(name: String, type: KClass<*>): Factory<T>? {
        val factory =
            modules.firstOrNull { it.hasDefinition(name, type) }?.resolveFactory<T>(name, type)

        if (factory != null)
            return factory

        return parent?.resolveFactory(name, type)
    }
}

data class Factory<T>(val isSingleton: Boolean, val definition: () -> T) {
    fun instance() = definition.invoke()
}

data class BeanDefinition(val name: String = "", val type: KClass<*>)

class NoFactoryFoundException(clazz: KClass<*>) : Exception("No factory registered for $clazz")
