package di

fun module(init: Module.() -> Unit): Module = Module().apply(init)

inline fun <reified T : Any> injectByName(name: String, scope: Scope = GlobalScope): Lazy<T> =
    lazy { scope.get<T>(name, T::class) }

inline fun <reified T : Any> inject(scope: Scope = GlobalScope): Lazy<T> =
    lazy { scope.get<T>(type = T::class) }

object GlobalScope : Scope()
