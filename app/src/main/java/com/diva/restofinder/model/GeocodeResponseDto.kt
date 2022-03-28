package com.diva.restofinder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GeocodeResponseDto(
    val link: String,
    val locationDto: LocationDto,
    @SerializedName("nearby_restaurants")
    val nearbyRestaurantResponses: List<RestaurantResponseDto>,
    val popularityDto: PopularityDto
): Serializable