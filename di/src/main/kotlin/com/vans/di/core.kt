package com.vans.di

import kotlin.reflect.KClass

private typealias Factory<T> = () -> T

class Module internal constructor(
    private val childModules: Set<Module> = setOf(),
    private var init: Module.() -> Unit
) {
    internal lateinit var moduleLookupProvider: (Definition) -> Any
    internal fun initialize(
        lookupProvider: (Definition) -> Any
    ) {
        moduleLookupProvider = lookupProvider
        childModules.forEach { it.initialize(lookupProvider) }
        init.invoke(this)
    }

    private val dependencyMap: MutableMap<Definition, Dependency<*>> = mutableMapOf()
    inline fun <reified T : Any> single(qualifier: Qualifier? = null, noinline creator: () -> T) =
        provideSingleton(qualifier, T::class, creator)

    inline fun <reified T : Any> factory(qualifier: Qualifier? = null, noinline creator: () -> T) =
        provideTransient(qualifier, T::class, creator)

    fun <T : Any> provideSingleton(qualifier: Qualifier? = null, type: KClass<T>, creator: () -> T) {
        dependencyMap[Definition(qualifier, type)] = Dependency.Singleton(creator)
    }

    fun <T : Any> provideTransient(qualifier: Qualifier? = null, type: KClass<T>, creator: () -> T) {
        dependencyMap[Definition(qualifier, type)] = Dependency.Transient(creator)
    }

    inline fun <reified T : Any> get(qualifier: Qualifier? = null): T = get(qualifier, T::class)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(qualifier: Qualifier? = null, type: KClass<T>): T =
        moduleLookupProvider(Definition(qualifier, type)) as? T
            ?: throw NoFactoryFoundException(type)

    @Suppress("UNCHECKED_CAST")
    internal fun <T> findDependency(definition: Definition): Dependency<T>? =
        dependencyDeepGet(definition) as? Dependency<T>

    private fun dependencyDeepGet(key: Definition): Dependency<*>? =
        dependencyMap[key] ?: childModules.map { it.dependencyMap[key] }.firstOrNull()
}

internal data class Definition(val qualifier: Qualifier?, val type: KClass<*>)
internal sealed class Dependency<T>(val factory: Factory<T>) {
    class Transient<T>(factory: Factory<T>) : Dependency<T>(factory) {
        override fun instance(): T? = factory()
    }

    class Singleton<T>(factory: Factory<T>) : Dependency<T>(factory) {
        private var instance: T? = null
        override fun instance(): T? {
            if (instance == null) {
                instance = factory()
            }

            return instance
        }
    }

    abstract fun instance(): T?
}

class Component {
    private var parent: Component? = null
    private lateinit var modules: Set<Module>

    private var initialized = false
    fun init(
        parentComponent: Component? = null,
        moduleSet: Set<Module> = emptySet()
    ) {
        if (initialized) {
            throw AlreadyInitializedException()
        }
        parent = parentComponent

        modules = moduleSet
        modules.forEach { module ->
            module.initialize { get(it) }
        }
        initialized = true
    }

    inline fun <reified T : Any> get(qualifier: Qualifier? = null): T = get(qualifier, T::class)
    fun <T : Any> get(qualifier: Qualifier? = null, type: KClass<T>): T = get(Definition(qualifier, type))
    private fun <T : Any> get(definition: Definition): T =
        findDependency<T>(definition)?.instance()
            ?: throw NoFactoryFoundException(definition.type)

    private fun <T> findDependency(definition: Definition): Dependency<T>? =
        modules.mapNotNull { it.findDependency<T>(definition) }.firstOrNull()
            ?: parent?.findDependency(definition)

    class AlreadyInitializedException
        : Exception("Cannot call `init` on already initialized Component")
}

class NoFactoryFoundException(type: KClass<*>) : Exception("No factory found for $type")
