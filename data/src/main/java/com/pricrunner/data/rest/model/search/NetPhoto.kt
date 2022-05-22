package com.pricrunner.data.rest.model.search

import kotlinx.serialization.Serializable

@Serializable
data class NetPhoto(
    val id: String,
    val secret: String,
    val server: String,
    val title: String
)