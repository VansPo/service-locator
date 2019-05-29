package di.example.kodi

fun module(
    dependsOn: Set<Module> = setOf(),
    init: Module.() -> Unit = { }
): Module = Module(dependsOn).apply(init)

inline fun <reified T : Any> inject(component: Component): Lazy<T> =
    injectNamed(component, "")

inline fun <reified T : Any> injectNamed(component: Component, name: String = "") =
    lazy { component.get<T>(name) }

inline fun <reified T : Any> Injectable.inject(): Lazy<T> = inject(component)

inline fun <reified T : Any> Injectable.injectNamed(name: String = ""): Lazy<T> =
    injectNamed(component, name)

interface Injectable {
    val component: Component
}
