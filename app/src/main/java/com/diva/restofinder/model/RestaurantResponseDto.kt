package com.diva.restofinder.model

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "tbl_restaurants")
data class RestaurantResponseDto(
    val id: String? = null,
    val name: String? = null,
    @SerializedName("thumb")
    val thumbRestaurant: String? = null,
    val url: String? = null,
    @SerializedName("location")
    val restaurantLocation: RestaurantLocationDto,
    @SerializedName("user_rating")
    val rating: RatingDto
) : Serializable