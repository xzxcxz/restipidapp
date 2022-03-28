package com.diva.restofinder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PopularityDto(
    val message: String,
    @SerializedName("nightlife_index")
    val nightlifeIndex: Int,
    val popularity: Int,
    val status: String
): Serializable