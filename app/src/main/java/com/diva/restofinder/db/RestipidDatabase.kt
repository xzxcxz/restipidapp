package com.diva.restofinder.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.diva.restofinder.model.RestaurantResponseDto

@Database(
    entities = [RestaurantResponseDto::class],
    version = 1
)
@TypeConverters(DatabaseConverter::class)
abstract class RestipidDatabase : RoomDatabase() {

    abstract fun favoriteRestaurantDao(): FavoriteRestaurantDao

}