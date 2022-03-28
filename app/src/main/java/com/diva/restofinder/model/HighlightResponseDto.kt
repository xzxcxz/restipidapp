package com.diva.restofinder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class HighlightResponseDto(
    val highlights: List<String>,
    @SerializedName("average_cost_for_two")
    val averageCostForTwo: Double,
    @SerializedName("price_range")
    val priceRange: Double,
    val currency: String,
    @SerializedName("timings")
    val schedule: String,
    val location: RestaurantLocationDto,
    @SerializedName("phone_numbers")
    val phoneNumber: String,
    val url: String
): Serializable
