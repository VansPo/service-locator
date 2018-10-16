package example.di.data

import example.di.data.model.Image
import io.reactivex.Single

class ImageRepository(
    private val imageService: ImageService,
    private val imageStorage: ImageStorage,
    private val pageSize: Int
) {

    fun getImageService(forceRefresh: Boolean): Single<List<Image>> {
        return imageService.getImages(pageSize)
    }
}
