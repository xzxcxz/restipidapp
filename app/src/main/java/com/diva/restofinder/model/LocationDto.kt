package com.diva.restofinder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LocationDto(
    @SerializedName("city_id")
    val cityId: Int,
    @SerializedName("city_name")
    val cityName: String,
    @SerializedName("country_id")
    val countryId: Int,
    @SerializedName("country_name")
    val countryName: String,
    @SerializedName("entity_id")
    val entityId: Int,
    @SerializedName("entity_type")
    val entityType: String,
    val latitude: String,
    val longitude: String,
    val title: String
): Serializable