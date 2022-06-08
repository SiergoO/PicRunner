package com.pricrunner.data.rest.model.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NetPhotos(
    @SerialName("photo")
    val photoList: ArrayList<NetPhoto>
)