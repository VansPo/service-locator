package example.di

import android.app.Application
import di.example.kodi.Component

class App : Application() {

    lateinit var appComponent: Component

    override fun onCreate() {
        super.onCreate()
        appComponent = Component()
            .init(modules = listOf(dataModule(), domainModule()))
    }
}
