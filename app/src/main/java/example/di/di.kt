package example.di

import android.content.Context
import di.Module
import di.Scope
import di.module
import java.util.Random

fun mainModule(context: Context): Module = module {
    bean { StringRepository(context, get()) as Repository }
    bean { Random() }
}

fun mainScreenModule() = module {
    bean { FirstPresenter(get()) }
}

fun secondScreenModule() = module {
    factory { FirstPresenter(get()) }
}

class MainActivityScope: Scope()

class SecondActivityScope: Scope()
