package com.pricrunner.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PhotoEntity::class], version = 2 )
abstract class PhotoDatabase : RoomDatabase() {

    abstract fun photoDao(): PhotoDao
}