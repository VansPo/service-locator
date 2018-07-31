package example.di

import android.util.Log

class FirstPresenter(private val repository: Repository) {

    init {
        Log.d("DI", "${FirstPresenter::class} init was called!")
    }

    fun getMessage() = repository.provideHelloWorldString()
}
