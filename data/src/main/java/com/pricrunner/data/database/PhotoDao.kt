package com.pricrunner.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(photo: PhotoEntity)

    @Query("SELECT * FROM photos ORDER BY photoId DESC")
    suspend fun selectAllPhotos(): Array<PhotoEntity>

    @Query("DELETE FROM photos")
    suspend fun deletePhotos()
}