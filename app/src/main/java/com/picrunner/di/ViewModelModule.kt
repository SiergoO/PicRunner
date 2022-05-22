package com.picrunner.di

import com.picrunner.screen.MainViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ViewModelModule {

    @Provides
    fun provideMainViewModel(): MainViewModel {
        return MainViewModel()
    }
}