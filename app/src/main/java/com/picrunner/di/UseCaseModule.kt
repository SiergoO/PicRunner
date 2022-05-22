package com.picrunner.di

import com.picrunner.domain.repository.PhotoRepository
import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.usecase.search.SearchPhotosParam
import com.picrunner.domain.usecase.search.SearchPhotosResult
import com.picrunner.domain.usecase.search.SearchPhotosUseCase
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
    ): UseCase<SearchPhotosParam, SearchPhotosResult> =
        SearchPhotosUseCase(photoRepository)
}