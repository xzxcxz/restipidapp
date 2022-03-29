package com.diva.restofinder.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CollectionResponseDto(
    @SerializedName("collections")
    val collectionEntities: List<CollectionEntity>,
    @SerializedName("display_text")
    val displayText: String,
    @SerializedName("has_more")
    val hasMore: Int,
    @SerializedName("has_total")
    val hasTotal: Int,
    @SerializedName("share_url")
    val shareUrl: String,
    @SerializedName("user_has_addresses")
    val userHasAddresses: Boolean
): Serializable