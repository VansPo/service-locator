package example.di

import android.util.Log
import com.vans.di.Module
import com.vans.di.module
import example.di.data.ImageInteractor
import example.di.data.ImageRepository
import example.di.data.ImageService
import example.di.data.ImageStorage
import example.di.ui.MainPresenter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

fun constantsModule() = module {
    factory("endpoint") {
        Log.d("KODI", "endpoint factory called")
        "https://api.thecatapi.com/v1/"
    }
    single("pageSize") {
        Log.d("KODI", "pageSize factory called")
        20
    }
}

fun dataModule(): Module =
    module(dependsOn = setOf(constantsModule())) {
        single { ImageStorage(get("endpoint"), get("pageSize")) }
        single { provideImageService(get("endpoint")) }
    }

fun domainModule(): Module =
    module(dependsOn = setOf(constantsModule())) {
        single { ImageInteractor(get()) }
        single { ImageRepository(get(), get(), get("pageSize")) }
    }

fun uiModule(): Module = module {
    single { MainPresenter(get()) }
}

fun provideImageService(endpoint: String): ImageService {
    val retrofit = Retrofit.Builder()
        .baseUrl(endpoint)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create<ImageService>(ImageService::class.java)
}
