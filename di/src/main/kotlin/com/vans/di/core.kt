package com.vans.di

import kotlin.reflect.KClass

private typealias Factory<T> = () -> T

class Module internal constructor(
    private val childModules: Set<Module> = setOf()
) {
    internal lateinit var moduleLookupProvider: (Definition) -> Any
    private val dependencyMap: MutableMap<Definition, Dependency<*>> = mutableMapOf()

    inline fun <reified T : Any> single(name: String = "", noinline creator: () -> T) =
        provideSingleton(name, T::class, creator)

    inline fun <reified T : Any> factory(name: String = "", noinline creator: () -> T) =
        provideTransient(name, T::class, creator)

    fun <T : Any> provideSingleton(name: String = "", type: KClass<T>, creator: () -> T) {
        dependencyMap[Definition(name, type)] = Dependency.Singleton(creator)
    }

    fun <T : Any> provideTransient(name: String = "", type: KClass<T>, creator: () -> T) {
        dependencyMap[Definition(name, type)] = Dependency.Transient(creator)
    }

    inline fun <reified T : Any> get(name: String = ""): T = get(name, T::class)

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(name: String = "", type: KClass<T>): T =
        moduleLookupProvider(Definition(name, type)) as? T
            ?: throw NoFactoryFoundException(type)

    @Suppress("UNCHECKED_CAST")
    internal fun <T> findDependency(definition: Definition): Dependency<T>? =
        dependencyDeepGet(definition) as? Dependency<T>

    private fun dependencyDeepGet(key: Definition): Dependency<*>? =
        dependencyMap[key] ?: childModules.map { it.dependencyMap[key] }.firstOrNull()

    override fun equals(other: Any?) = other is Module && dependencyMap == other.dependencyMap

    override fun hashCode() = dependencyMap.hashCode()
}

internal data class Definition(val name: String, val type: KClass<*>)

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
    private lateinit var moduleSet: Set<Module>

    private var initialized = false

    fun init(
        parentComponent: Component? = null,
        modules: Set<Module> = emptySet()
    ) {
        if (initialized) {
            throw AlreadyInitializedException()
        }
        parent = parentComponent
        moduleSet = modules.apply {
            forEach { module ->
                module.moduleLookupProvider =  { get(it) }
            }
        }
        initialized = true
    }

    inline fun <reified T : Any> get(name: String = ""): T = get(name, T::class)

    fun <T : Any> get(name: String = "", type: KClass<T>): T = get(Definition(name, type))

    private fun <T : Any> get(definition: Definition): T =
        findDependency<T>(definition)?.instance()
            ?: throw NoFactoryFoundException(definition.type)

    private fun <T> findDependency(definition: Definition): Dependency<T>? =
        moduleSet.mapNotNull { it.findDependency<T>(definition) }.firstOrNull()
            ?: parent?.findDependency(definition)

    class AlreadyInitializedException
        : Exception("Cannot call `init` on already initialized Component")
}

class NoFactoryFoundException(type: KClass<*>) : Exception("No factory found for $type")
