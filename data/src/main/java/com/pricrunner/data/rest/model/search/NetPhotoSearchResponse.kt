package com.pricrunner.data.rest.model.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetPhotoSearchResponse(
    @SerialName("photos")
    val photos: NetPhotos
)