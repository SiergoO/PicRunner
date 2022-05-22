package com.pricrunner.data.rest

import com.pricrunner.data.rest.model.search.NetPhotoSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {

    @GET("")
    suspend fun searchPhotos(
        @Query("method") method: String = "flickr.photos.search",
        @Query("sort") sort: String = "relevance",
        @Query("accuracy") accuracy: Int = 16,
        @Query("geo_context") geoContext: Int = 2,
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("radius") radius: Double,
        @Query("radius_units") radiusUnits: String = "km"
    ): NetPhotoSearchResponse
}