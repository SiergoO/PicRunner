package com.pricrunner.data.rest.model.search

import kotlinx.serialization.Serializable

@Serializable
data class NetPhotos(
    val photo: List<NetPhoto>,
    val total: String
)