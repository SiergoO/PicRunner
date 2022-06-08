package com.picrunner.di

import com.picrunner.domain.repository.PhotoRepository
import com.pricrunner.data.database.PhotoDao
import com.pricrunner.data.repository.PhotoRepositoryImpl
import com.pricrunner.data.rest.FlickrApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun providePhotoRepository(
        api: FlickrApi,
        photoDao: PhotoDao
    ): PhotoRepository = PhotoRepositoryImpl(api, photoDao)
}