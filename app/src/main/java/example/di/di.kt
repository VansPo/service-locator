package example.di

import android.content.Context
import android.util.Log
import di.Module
import di.Scope
import di.module
import java.util.Random

fun mainModule(context: Context): Module = module {
    bean { StringRepository(context, get(), get("second")) as Repository }
    bean { Random() }
    factory("first") {
        Log.d("DI", "first string factory called")
        "First header"
    }
    bean("second") {
        Log.d("DI", "second string factory called")
        "Second header"
    }
}

fun mainScreenModule() = module {
    bean { FirstPresenter(get()) }
}

class MainActivityScope : Scope()
