package com.diva.restofinder.model

import androidx.room.Entity
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Entity(tableName = "tbl_restaurant_detail")
data class RestaurantDetailResponseDto(
    @SerializedName("all_reviews_count")
    val allReviewsCount: Int,
    @SerializedName("average_cost_for_two")
    val averageCostForTwo: Int,
    val cuisines: String,
    val currency: String,
    val deeplink: String,
    val establishment: List<String>,
    val highlights: List<String>,
    val id: String,
    val location: RestaurantLocationDto,
    @SerializedName("menu_url")
    val menuUrl: String,
    val name: String,
    @SerializedName("phone_numbers")
    val phoneNumbers: String,
    @SerializedName("photo_count")
    val photoCount: Int,
//    val photos: List<Photo>,
    @SerializedName("photos_url")
    val photosUrl: String,
    @SerializedName("price_range")
    val priceRange: Int,
    @SerializedName("store_type")
    val storeType: String,
    @SerializedName("switch_to_order_menu")
    val switchToOrderMenu: Int,
    val thumb: String,
    val timings: String,
    val url: String,
): Serializable