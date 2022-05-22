package com.pricrunner.data.rest.model.search

import kotlinx.serialization.Serializable

@Serializable
data class NetPhotoSearchResponse(
    val photos: NetPhotos,
    val stat: String
)