package example.di

import android.app.Application
import di.example.kodi.Component
import di.example.kodi.Injectable

class App : Application(), Injectable {

    override val component: Component = Component()

    override fun onCreate() {
        super.onCreate()
        component.init(modules = listOf(dataModule(), domainModule()))
    }
}
