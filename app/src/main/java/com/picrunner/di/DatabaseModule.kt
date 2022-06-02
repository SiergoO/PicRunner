package com.picrunner.di

import android.content.Context
import androidx.room.Room
import com.pricrunner.data.database.PhotoDao
import com.pricrunner.data.database.PhotoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideRoomDatabase(@ApplicationContext context: Context): PhotoDatabase =
        Room.databaseBuilder(context, PhotoDatabase::class.java, "photo_database")
            .build()

    @Provides
    fun providePhotoDao(photoDatabase: PhotoDatabase): PhotoDao = photoDatabase.photoDao()
}