package com.picrunner.di

import com.picrunner.service.LocationService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @Provides
    fun provideServiceModule(): LocationService {
        return LocationService()
    }
}