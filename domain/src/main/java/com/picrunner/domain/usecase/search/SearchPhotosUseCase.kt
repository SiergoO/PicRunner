package com.picrunner.domain.usecase.search

import com.picrunner.domain.model.Photo
import com.picrunner.domain.repository.PhotoRepository
import com.picrunner.domain.usecase.Param
import com.picrunner.domain.usecase.UseCase

typealias SearchPhotosResult = Result<List<Photo>>

class SearchPhotosUseCase(
    private val photoRepository: PhotoRepository
) : UseCase<SearchPhotosParam, SearchPhotosResult> {

    override suspend fun execute(param: SearchPhotosParam): SearchPhotosResult {
        return try {
            val photoList = photoRepository.searchPhotos(param.lat, param.lon, param.radius)
            SearchPhotosResult.success(photoList)
        } catch (t: Throwable) {
            SearchPhotosResult.failure(t)
        }
    }
}

data class SearchPhotosParam(val lat: Double, val lon: Double, val radius: Double) : Param

