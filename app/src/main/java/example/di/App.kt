package example.di

import android.app.Application
import di.GlobalScope

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        GlobalScope.init(listOf(mainModule(this)))
    }

}
