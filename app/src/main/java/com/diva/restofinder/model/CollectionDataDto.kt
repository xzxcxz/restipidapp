package com.diva.restofinder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CollectionDataDto(
    @SerializedName("collection_id")
    val collectionId: Int,
    val description: String,
    @SerializedName("image_url")
    val imageUrl: String,
    @SerializedName("res_count")
    val resCount: Int,
    @SerializedName("share_url")
    val shareUrl: String,
    val title: String,
    val url: String
): Serializable