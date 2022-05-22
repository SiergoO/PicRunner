package com.pricrunner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Photos(
    val photo: List<Photo>,
    val total: String
)