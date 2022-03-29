package com.diva.restofinder.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RestaurantResponseDto(
    var restaurant: RestaurantDataDto
): Parcelable