package example.di

import di.example.kodi.Module
import di.example.kodi.module
import example.di.data.ImageInteractor
import example.di.data.ImageRepository
import example.di.data.ImageService
import example.di.data.ImageStorage
import example.di.ui.MainPresenter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

val constantsModule = module {
    factory("endpoint") { "https://api.thecatapi.com/v1/" }
    factory("pageSize") { 20 }
}

fun dataModule(): Module = module(dependsOn = setOf(constantsModule)) {
    singleton { ImageStorage() }
    singleton { provideImageService(get("endpoint")) }
}

fun provideImageService(endpoint: String): ImageService {
    val retrofit = Retrofit.Builder()
        .baseUrl(endpoint)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create<ImageService>(ImageService::class.java)
}

fun domainModule(): Module = module(dependsOn = setOf(constantsModule)) {
    singleton { ImageInteractor(get()) }
    singleton { ImageRepository(get(), get(), get("pageSize")) }
}

fun uiModule(): Module = module {
    singleton { MainPresenter(get()) }
}
