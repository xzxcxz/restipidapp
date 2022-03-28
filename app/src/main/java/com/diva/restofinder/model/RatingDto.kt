package com.diva.restofinder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RatingDto(
    @SerializedName("aggregate_rating")
    val ratingValue: Double,
    @SerializedName("rating_text")
    val ratingText: String
): Serializable
