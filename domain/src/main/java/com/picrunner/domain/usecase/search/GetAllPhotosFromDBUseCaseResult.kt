package com.picrunner.domain.usecase.search

import com.picrunner.domain.model.Photo
import com.picrunner.domain.repository.PhotoRepository
import com.picrunner.domain.usecase.Param
import com.picrunner.domain.usecase.UseCase

typealias GetAllPhotosFromDBUseCaseResult = Result<List<Photo>>

class GetAllPhotosFromDBUseCase(
    private val photoRepository: PhotoRepository
) : UseCase<GetAllPhotosFromDBUseCaseParam, GetAllPhotosFromDBUseCaseResult> {

    override suspend fun execute(param: GetAllPhotosFromDBUseCaseParam): GetAllPhotosFromDBUseCaseResult {
        return try {
            val photos = photoRepository.getAllPhotosFromDatabase()
            GetAllPhotosFromDBUseCaseResult.success(photos)
        } catch (t: Throwable) {
            GetAllPhotosFromDBUseCaseResult.failure(t)
        }
    }
}

object GetAllPhotosFromDBUseCaseParam : Param
