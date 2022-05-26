package com.picrunner.di

import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.usecase.search.GetNearestPhotoUrlListParam
import com.picrunner.domain.usecase.search.GetNearestPhotoUrlListResult
import com.picrunner.screen.main.WalkViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {

    @Provides
    fun provideMainViewModel(
        searchPhotosUseCase: UseCase<GetNearestPhotoUrlListParam, GetNearestPhotoUrlListResult>
    ): WalkViewModel {
        return WalkViewModel(searchPhotosUseCase)
    }
}