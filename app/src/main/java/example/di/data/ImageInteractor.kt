package example.di.data

import example.di.data.model.Image
import io.reactivex.Single

class ImageInteractor(private val imageRepository: ImageRepository) {

    fun getImages(forceRefresh: Boolean): Single<List<Image>> {
        return imageRepository.getImageService(forceRefresh)
    }

}
