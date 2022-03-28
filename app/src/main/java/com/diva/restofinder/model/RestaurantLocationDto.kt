package com.diva.restofinder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RestaurantLocationDto(
    val address: String,
    @SerializedName("locality_verbose")
    val locality: String,
    val city: String,
    @SerializedName("city_id")
    val cityId: Int,
    val latitude: Int,
    val longitude: Int,
    val zipcode: String? = null,
    @SerializedName("country_id")
    val countryId: Int
): Serializable
