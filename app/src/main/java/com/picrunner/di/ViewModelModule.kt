package com.picrunner.di

import com.picrunner.domain.usecase.UseCase
import com.picrunner.domain.usecase.search.SearchPhotosParam
import com.picrunner.domain.usecase.search.SearchPhotosResult
import com.picrunner.screen.main.MainViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {

    @Provides
    fun provideMainViewModel(
        searchPhotosUseCase: UseCase<SearchPhotosParam, SearchPhotosResult>
    ): MainViewModel {
        return MainViewModel(searchPhotosUseCase)
    }
}