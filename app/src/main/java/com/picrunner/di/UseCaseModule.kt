package com.picrunner.di

import com.picrunner.domain.repository.PhotoRepository
import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.usecase.search.GetNearestPhotoUrlListParam
import com.picrunner.domain.usecase.search.GetNearestPhotoUrlListResult
import com.picrunner.domain.usecase.search.GetNearestPhotosUrlListUseCase
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
    ): UseCase<GetNearestPhotoUrlListParam, GetNearestPhotoUrlListResult> =
        GetNearestPhotosUrlListUseCase(photoRepository)
}