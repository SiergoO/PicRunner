package com.pricrunner.data.repository

import com.picrunner.domain.model.Photo
import com.picrunner.domain.repository.PhotoRepository
import com.pricrunner.data.database.PhotoDao
import com.pricrunner.data.database.toDomainModel
import com.pricrunner.data.database.toEntityModel
import com.pricrunner.data.rest.FlickrApi
import com.pricrunner.data.rest.model.search.toDomainModule
import kotlinx.coroutines.runBlocking

class PhotoRepositoryImpl(
    private val api: FlickrApi,
    private val photoDao: PhotoDao
    ) : PhotoRepository {

    override fun searchPhotos(
        lat: Double,
        lon: Double,
        radius: Double
    ): List<Photo> = runBlocking {
        api.searchPhotos(lat = lat, lon = lon, radius = radius).photos.photoList
            .map { it.toDomainModule() }
    }

    override fun getAllPhotosFromDatabase(): List<Photo> = runBlocking {
        val photos = photoDao.selectAllPhotos()
        photos.map { photoEntity -> photoEntity.toDomainModel() }
    }

    override fun deleteAllPhotosFromDatabase() = runBlocking {
        photoDao.deletePhotos()
    }

    override fun insertPhotoToDB(photo: Photo) = runBlocking {
        photoDao.insert(photo.toEntityModel())
    }
}