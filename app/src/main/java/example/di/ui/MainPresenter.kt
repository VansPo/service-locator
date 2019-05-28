package example.di.ui

import android.util.Log
import example.di.data.ImageInteractor
import example.di.data.model.Image
import io.reactivex.Single

class MainPresenter(private val interactor: ImageInteractor) {

    init {
        Log.d("DI", "${MainPresenter::class} init was called!")
    }

    fun getImages(forceRefresh: Boolean): Single<List<Image>> =
        interactor.getImages(forceRefresh)

}
