package com.diva.restofinder.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class RestaurantLocationDto(
    val address: String?,
    @SerializedName("locality_verbose")
    val locality: String?,
    val city: String?,
    @SerializedName("city_id")
    val cityId: Int?,
    val latitude: String?,
    val longitude: String?,
    val zipcode: String?,
    @SerializedName("country_id")
    val countryId: Int?
): Parcelable
