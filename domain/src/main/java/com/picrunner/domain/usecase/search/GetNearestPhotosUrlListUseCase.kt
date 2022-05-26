package com.picrunner.domain.usecase.search

import com.picrunner.domain.model.Photo
import com.picrunner.domain.repository.PhotoRepository
import com.picrunner.domain.usecase.Param
import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.util.convertToStringUrl

typealias GetNearestPhotoUrlListResult = Result<List<String>>

class GetNearestPhotosUrlListUseCase(
    private val photoRepository: PhotoRepository
) : UseCase<GetNearestPhotoUrlListParam, GetNearestPhotoUrlListResult> {

    companion object {
        private const val INITIAL_RADIUS = 1.0
        private const val RADIUS_INCREMENT = 0.1
    }

    override suspend fun execute(param: GetNearestPhotoUrlListParam): GetNearestPhotoUrlListResult {
        return try {
            val photoUrlList = mutableListOf<String>()
            param.latLonList.forEach { latLon ->
                var nonEmptyResult: Photo? = null
                while (nonEmptyResult == null) {
                    var radius = INITIAL_RADIUS
                    nonEmptyResult = photoRepository.searchPhotos(
                        latLon.first,
                        latLon.second,
                        radius
                    ).firstOrNull()
                    radius += RADIUS_INCREMENT
                }
                photoUrlList.add(nonEmptyResult.convertToStringUrl())
            }

            GetNearestPhotoUrlListResult.success(photoUrlList)
        } catch (t: Throwable) {
            GetNearestPhotoUrlListResult.failure(t)
        }
    }
}

data class GetNearestPhotoUrlListParam(val latLonList: List<Pair<Double, Double>>) : Param

