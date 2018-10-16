package example.di.data

import example.di.data.model.Image
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ImageService {

    @Headers("x-api-key: 17d94b92-754f-46eb-99a0-65be65b5d18f")
    @GET("images/")
    fun getImages(@Query("limit") limit: Int): Single<List<Image>>
}
