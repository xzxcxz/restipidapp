package com.diva.restofinder.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.diva.restofinder.model.RestaurantDetailResponseDto
import com.diva.restofinder.model.RestaurantResponseDto

@Dao
interface FavoriteRestaurantDao {

    @Query("SELECT * FROM tbl_restaurants")
    fun getAll(): List<RestaurantResponseDto>

    @Query("SELECT * FROM tbl_restaurant_detail WHERE id = :id")
    fun getRestaurantDetailById(id: Int): RestaurantDetailResponseDto

    @Query("DELETE FROM tbl_restaurant_detail WHERE id = :id")
    fun removeRestaurantDetailById(id: Int)

    @Query("DELETE FROM tbl_restaurant_detail")
    fun removeAllRestaurantDetails()

    @Insert
    fun addToFavorites(restaurantResponseDto: RestaurantResponseDto)

    @Delete
    fun removeFromFavorites(restaurantResponseDto: RestaurantResponseDto)

    @Query("DELETE FROM tbl_restaurants")
    fun removeAllItemFromFavorites()

}