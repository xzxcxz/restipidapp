package com.diva.restofinder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PhotoDto(
    val id: String,
    val url: String,
    @SerializedName("thumb_url")
    val thumb: String,
    @SerializedName("friendly_time")
    val dateCreated: String,
    val width: Int,
    val height: Int
): Serializable
