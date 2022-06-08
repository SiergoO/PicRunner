package com.pricrunner.data.rest.model.search

import com.picrunner.domain.model.Photo
import kotlinx.serialization.Serializable

@Serializable
data class NetPhoto (
    val id : String,
    val secret : String,
    val server : String,
    val title : String
)

fun NetPhoto.toDomainModule(): Photo = Photo(id, secret, server, title)
