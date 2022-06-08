package com.pricrunner.data.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.picrunner.domain.model.Photo

@Entity(tableName = "photos")
class PhotoEntity(
    @PrimaryKey(autoGenerate = true) var photoId: Int = 0,
    @ColumnInfo(name = "id") var id: String = "",
    @ColumnInfo(name = "secret") var secret: String = "",
    @ColumnInfo(name = "server") var server: String = "",
    @ColumnInfo(name = "title") var title: String = ""
)

fun PhotoEntity.toDomainModel() = Photo(
    id = id,
    secret = secret,
    server = server,
    title = title
)

fun Photo.toEntityModel() = PhotoEntity(
    id = id,
    secret = secret,
    server = server,
    title = title
)