package di

fun module(init: Module.() -> Unit): Module = Module().apply(init)

inline fun <reified T : Any> inject(scope: Scope = GlobalScope): Lazy<T> =
    lazy { scope.get<T>(T::class) }

object GlobalScope : Scope()
