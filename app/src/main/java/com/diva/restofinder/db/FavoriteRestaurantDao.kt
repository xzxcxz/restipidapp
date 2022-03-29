package com.diva.restofinder.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.diva.restofinder.model.CollectionDataDto
import com.diva.restofinder.model.RestaurantDataDto
import com.diva.restofinder.model.RestaurantDetailResponseDto
import com.diva.restofinder.model.RestaurantResponseDto

@Dao
interface FavoriteRestaurantDao {

    @Query("SELECT * FROM tbl_restaurants")
    suspend fun getAll(): List<RestaurantDataDto>

    @Query("SELECT * FROM tbl_restaurant_detail WHERE id = :id")
    suspend fun getRestaurantDetailById(id: String): RestaurantDetailResponseDto

    @Query("DELETE FROM tbl_restaurant_detail")
    suspend fun removeAllRestaurantDetails()

    @Query("SELECT COUNT(id) FROM tbl_restaurants WHERE id = :id")
    suspend fun isRestaurantExisting(id: String): Int

    @Insert
    suspend fun addToFavorites(restaurant: RestaurantDataDto)

    @Insert
    suspend fun addRestaurantDetail(restaurantDetailResponseDto: RestaurantDetailResponseDto)

    @Delete
    suspend fun removeRestaurantDetail(restaurantDetailResponseDto: RestaurantDetailResponseDto)

    @Delete
    suspend fun removeFromFavorites(restaurant: RestaurantDataDto)

    @Query("DELETE FROM tbl_restaurants")
    suspend fun removeAllItemFromFavorites()

}