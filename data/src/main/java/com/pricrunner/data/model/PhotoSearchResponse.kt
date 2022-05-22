package com.pricrunner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PhotoSearchResponse(
    val photos: Photos,
    val stat: String
)