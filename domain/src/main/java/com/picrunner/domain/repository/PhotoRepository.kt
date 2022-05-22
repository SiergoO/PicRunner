package com.picrunner.domain.repository

import com.picrunner.domain.model.Photo

interface PhotoRepository {

    fun searchPhotos(lat: Double, lon: Double, radius: Double): List<Photo>
}