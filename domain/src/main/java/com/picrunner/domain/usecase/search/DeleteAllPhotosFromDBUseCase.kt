package com.picrunner.domain.usecase.search

import com.picrunner.domain.repository.PhotoRepository
import com.picrunner.domain.usecase.Param
import com.picrunner.domain.usecase.UseCase

typealias DeleteAllPhotosFromDBUseCaseResult = Result<Unit>

class DeleteAllPhotosFromDBUseCase(
    private val photoRepository: PhotoRepository
) : UseCase<DeleteAllPhotosFromDBUseCaseParam, DeleteAllPhotosFromDBUseCaseResult> {

    override suspend fun execute(param: DeleteAllPhotosFromDBUseCaseParam): DeleteAllPhotosFromDBUseCaseResult {
        return try {
            photoRepository.deleteAllPhotosFromDatabase()
            DeleteAllPhotosFromDBUseCaseResult.success(Unit)
        } catch (t: Throwable) {
            DeleteAllPhotosFromDBUseCaseResult.failure(t)
        }
    }

}

object DeleteAllPhotosFromDBUseCaseParam : Param
