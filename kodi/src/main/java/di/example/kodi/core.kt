package di.example.kodi

import java.lang.Exception
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class Module {

    lateinit var component: Component
    private val instanceMap: MutableMap<Definition, Any> = mutableMapOf()
    private val factoryMap: MutableMap<Definition, Factory<*>> = mutableMapOf()

    inline fun <reified T : Any> singleton(name: String = "", noinline creator: () -> T) =
        provide(true, Definition(name, T::class), creator)

    inline fun <reified T : Any> factory(name: String = "", noinline creator: () -> T) =
        provide(false, Definition(name, T::class), creator)

    fun <T> provide(
        isSingleton: Boolean,
        definition: Definition,
        creator: () -> T
    ) {
        factoryMap[definition] = Factory(isSingleton, creator)
    }

    inline fun <reified T : Any> get(name: String = ""): T {
        val definition = Definition(name, T::class)
        val factory = component.resolveFactory<T>(definition)
        return component.resolveInstance(factory, definition)
    }

    fun <T> resolveFactory(definition: Definition): Factory<T> =
        factoryMap[definition] as Factory<T>?
            ?: throw NoFactoryFoundException(definition.type)

    fun <T : Any> resolveInstance(factory: Factory<T>, definition: Definition): T =
        if (factory.isSingleton) {
            instanceMap.getOrPut(definition) { factory.instance() } as T
        } else {
            factory.instance()
        }

    fun hasDefinition(definition: Definition) = factoryMap.contains(definition)
}

data class Factory<T>(val isSingleton: Boolean, val creator: () -> T) {
    fun instance(): T = creator.invoke()
}

data class Definition(val name: String, val type: KClass<*>)

class Component {

    private var parent: Component? = null
    private val modules: MutableList<Module> = mutableListOf()

    fun init(parent: Component? = null, modules: List<Module>): Component = apply {
        this.parent = parent
        this.modules += modules
        this.modules.forEach { it.component = this }
    }

    fun <T : Any> get(definition: Definition): T =
        resolveInstance(resolveFactory(definition), definition)

    fun <T : Any> resolveFactory(definition: Definition): Factory<T> =
        resolveFactoryInternal(definition) ?: throw NoFactoryFoundException(definition.type)

    fun <T : Any> resolveInstance(factory: Factory<T>, definition: Definition): T =
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
