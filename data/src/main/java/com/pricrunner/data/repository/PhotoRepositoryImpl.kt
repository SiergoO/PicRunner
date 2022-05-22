package com.pricrunner.data.repository

import com.picrunner.domain.model.Photo
import com.picrunner.domain.repository.PhotoRepository
import com.pricrunner.data.rest.FlickrApi
import com.pricrunner.data.rest.model.search.toDomainModule
import kotlinx.coroutines.runBlocking

class PhotoRepositoryImpl(private val api: FlickrApi) : PhotoRepository {

    override fun searchPhotos(
        lat: Double,
        lon: Double,
        radius: Double
    ): List<Photo> = runBlocking {
        api.searchPhotos(lat = lat, lon = lon, radius = radius).photos.photoList
            .map { it.toDomainModule() }
    }
}