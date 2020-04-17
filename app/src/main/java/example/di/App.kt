package example.di

import android.app.Application
import com.vans.di.Component
import com.vans.di.Injectable

class App : Application(), Injectable {

    override val component: Component =
        Component()

    override fun onCreate() {
        super.onCreate()
        component.init(moduleSet = setOf(dataModule(), domainModule()))
    }
}
