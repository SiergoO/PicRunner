package com.picrunner.di

import com.picrunner.domain.repository.PhotoRepository
import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.usecase.search.DeleteAllPhotosFromDBUseCase
import com.picrunner.domain.usecase.search.DeleteAllPhotosFromDBUseCaseParam
import com.picrunner.domain.usecase.search.DeleteAllPhotosFromDBUseCaseResult
import com.picrunner.domain.usecase.search.GetAllPhotosFromDBUseCase
import com.picrunner.domain.usecase.search.GetAllPhotosFromDBUseCaseParam
import com.picrunner.domain.usecase.search.GetAllPhotosFromDBUseCaseResult
import com.picrunner.domain.usecase.search.GetNearestPhotoParam
import com.picrunner.domain.usecase.search.GetNearestPhotoResult
import com.picrunner.domain.usecase.search.GetNearestPhotoUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    fun provideSearchPhotosUseCase(
        photoRepository: PhotoRepository
    ): UseCase<GetNearestPhotoParam, GetNearestPhotoResult> =
        GetNearestPhotoUseCase(photoRepository)

    @Provides
    fun provideGetAllPhotosFromDBUseCase(
        photoRepository: PhotoRepository
    ): UseCase<GetAllPhotosFromDBUseCaseParam, GetAllPhotosFromDBUseCaseResult> =
        GetAllPhotosFromDBUseCase(photoRepository)

    @Provides
    fun provideDeleteAllPhotosFromDBUseCase(
        photoRepository: PhotoRepository
    ): UseCase<DeleteAllPhotosFromDBUseCaseParam, DeleteAllPhotosFromDBUseCaseResult> =
        DeleteAllPhotosFromDBUseCase(photoRepository)
}