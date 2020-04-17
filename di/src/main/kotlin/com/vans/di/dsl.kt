package com.vans.di

fun module(
    dependsOn: Set<Module> = setOf(),
    init: Module.() -> Unit = { }
): Module = Module(dependsOn, init)

inline fun <reified T : Any> inject(component: Component): Lazy<T> =
    injectNamed(component, null)

inline fun <reified T : Any> injectNamed(component: Component, qualifier: Qualifier?) =
    lazy { component.get<T>(qualifier) }

inline fun <reified T : Any> Injectable.inject(): Lazy<T> = inject(component)

inline fun <reified T : Any> Injectable.injectNamed(qualifier: Qualifier?): Lazy<T> =
    injectNamed(component, qualifier)

interface Injectable {
    val component: Component
}
