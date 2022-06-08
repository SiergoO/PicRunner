package com.picrunner.di

import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.usecase.search.GetAllPhotosFromDBUseCaseParam
import com.picrunner.domain.usecase.search.GetAllPhotosFromDBUseCaseResult
import com.picrunner.screen.walk.WalkViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {

    @Provides
    fun provideMainViewModel(
        getAllPhotosFromDBUseCase: UseCase<GetAllPhotosFromDBUseCaseParam, GetAllPhotosFromDBUseCaseResult>
    ): WalkViewModel =
        WalkViewModel(getAllPhotosFromDBUseCase)

}