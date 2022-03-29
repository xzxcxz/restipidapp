package com.diva.restofinder.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class GeocodeResponseDto(
    @SerializedName("nearby_restaurants")
    val nearbyRestaurant: List<RestaurantResponseDto>
): Parcelable