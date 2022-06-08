package com.picrunner.domain.usecase.search

import com.picrunner.domain.model.Photo
import com.picrunner.domain.repository.PhotoRepository
import com.picrunner.domain.usecase.Param
import com.picrunner.domain.usecase.UseCase

typealias GetNearestPhotoResult = Result<Photo>

class GetNearestPhotoUseCase(
    private val photoRepository: PhotoRepository,
) : UseCase<GetNearestPhotoParam, GetNearestPhotoResult> {

    companion object {
        private const val INITIAL_RADIUS = 0.1
        private const val RADIUS_INCREMENT = 0.1
    }

    override suspend fun execute(param: GetNearestPhotoParam): GetNearestPhotoResult {
        return try {
            val photosFromDb = photoRepository.getAllPhotosFromDatabase()
            var nonEmptyResult: Photo? = null
            var radius = INITIAL_RADIUS
            while (nonEmptyResult == null) {
                nonEmptyResult = photoRepository.searchPhotos(
                    param.latLon.first,
                    param.latLon.second,
                    radius
                ).getUnique(photosFromDb)
                radius += RADIUS_INCREMENT
            }
            photoRepository.insertPhotoToDB(nonEmptyResult)
            GetNearestPhotoResult.success(nonEmptyResult)
        } catch (t: Throwable) {
            GetNearestPhotoResult.failure(t)
        }
    }

    private fun List<Photo>.getUnique(photos: List<Photo>): Photo? =
         this.firstOrNull { photoFromServer ->
            photos.none { photoFromDB -> photoFromDB.id == photoFromServer.id }
        }
}

data class GetNearestPhotoParam(val latLon: Pair<Double, Double>) : Param

