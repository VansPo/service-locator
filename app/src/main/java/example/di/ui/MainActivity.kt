package example.di.ui

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import di.example.kodi.Component
import di.example.kodi.inject
import example.di.App
import example.di.R
import example.di.data.model.Image
import example.di.uiModule
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.recycler

class MainActivity : Activity() {

    private val activityComponent: Component = Component()
    private val presenter: MainPresenter by inject(activityComponent)

    private val adapter = ImageAdapter()

    private var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val appComponent = (application as App).appComponent
        activityComponent.init(appComponent, listOf(uiModule()))

        recycler.layoutManager = GridLayoutManager(this, 2)
        recycler.adapter = adapter

        disposable = presenter.getImages(true)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(::onResult, ::onError)
    }

    override fun onStop() {
        super.onStop()
        disposable?.dispose()
    }

    private fun onResult(result: List<Image>) {
        adapter.replaceAll(result)
    }

    private fun onError(e: Throwable) {
        Log.e("image", "fail", e)
    }
}
