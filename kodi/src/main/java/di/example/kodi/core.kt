package di.example.kodi

import java.lang.Exception
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class Module internal constructor(
    private val childModules: Set<Module> = setOf()
) {
    internal var component: Component = Component()
        set(value) {
            field = value
            value.addModules(childModules)
        }
    private val instanceMap: MutableMap<Definition, Any> = mutableMapOf()
    private val factoryMap: MutableMap<Definition, Factory<*>> = mutableMapOf()

    inline fun <reified T : Any> singleton(name: String = "", noinline creator: () -> T) =
        provide(true, name, T::class, creator)

    inline fun <reified T : Any> factory(name: String = "", noinline creator: () -> T) =
        provide(false, name, T::class, creator)

    fun <T : Any> provide(
        isSingleton: Boolean,
        name: String = "",
        type: KClass<T>,
        creator: () -> T
    ) {
        val definition = Definition(name, type)
        factoryMap[definition] = Factory(isSingleton, creator)
    }

    inline fun <reified T : Any> get(name: String = ""): T = get(name, T::class)

    fun <T : Any> get(name: String = "", type: KClass<T>): T {
        return component.get(name, type)
    }

    internal fun <T> resolveFactory(definition: Definition): Factory<T> =
        factoryMap[definition] as Factory<T>?
            ?: throw NoFactoryFoundException(definition.type)

    internal fun <T : Any> resolveInstance(factory: Factory<T>, definition: Definition): T =
        if (factory.isSingleton) {
            instanceMap.getOrPut(definition) { factory.instance() } as T
        } else {
            factory.instance()
        }

    internal fun hasDefinition(definition: Definition) = factoryMap.contains(definition)

    override fun equals(other: Any?) = factoryMap == other
}

internal data class Factory<T>(val isSingleton: Boolean, val creator: () -> T) {
    fun instance(): T = creator.invoke()
}

internal data class Definition(val name: String, val type: KClass<*>)

class Component {

    private var parent: Component? = null
    private val modules: MutableSet<Module> = mutableSetOf()

    fun init(parent: Component? = null, modules: Set<Module>): Component = apply {
        this.parent = parent
        addModules(modules)
    }

    inline fun <reified T : Any> get(name: String = ""): T = get(name, T::class)

    fun <T : Any> get(name: String = "", type: KClass<T>): T {
        val definition = Definition(name, type)
        return resolveInstance(resolveFactory(definition), definition)
    }

    internal fun addModules(modules: Set<Module>) {
        modules.forEach { it.component = this }
        this.modules += modules
    }

    private fun <T : Any> resolveFactory(definition: Definition): Factory<T> =
        resolveFactoryInternal(definition) ?: throw NoFactoryFoundException(definition.type)

    private fun <T : Any> resolveInstance(factory: Factory<T>, definition: Definition): T =
        resolveInstanceInternal(factory, definition)
            ?: throw NoFactoryFoundException(definition.type)

    private fun <T : Any> resolveFactoryInternal(definition: Definition): Factory<T>? {
        val factory = modules
            .firstOrNull { it.hasDefinition(definition) }
            ?.resolveFactory<T>(definition)

        if (factory != null) {
            return factory
        }

        return parent?.resolveFactoryInternal(definition)
    }

    private fun <T : Any> resolveInstanceInternal(factory: Factory<T>, definition: Definition): T? {
        val instance = modules
            .firstOrNull { it.hasDefinition(definition) }
            ?.resolveInstance(factory, definition)

        if (instance != null) {
            return instance
        }

        return parent?.resolveInstanceInternal(factory, definition)
    }

}

class NoFactoryFoundException(type: KClass<*>) : Exception("No factory registered for $type")
