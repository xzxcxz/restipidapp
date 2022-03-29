package com.diva.restofinder.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
data class RatingDto(
    @SerializedName("aggregate_rating")
    val ratingValue: Double?,
    @SerializedName("rating_text")
    val ratingText: String?
): Parcelable
