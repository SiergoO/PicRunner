package com.pricrunner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Photo(
    val id: String,
    val secret: String,
    val server: String,
    val title: String
)