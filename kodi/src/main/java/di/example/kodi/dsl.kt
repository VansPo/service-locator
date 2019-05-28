package di.example.kodi

fun module(init: Module.() -> Unit): Module = Module().apply(init)

inline fun <reified T : Any> injectNamed(name: String = "", component: Component) = lazy {
    component.get<T>(Definition(name, T::class))
}

inline fun <reified T : Any> inject(component: Component): Lazy<T> =
    injectNamed("", component)
