package com.diva.restofinder.model

import android.graphics.Bitmap
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

@Parcelize
@Entity(tableName = "tbl_restaurants")
data class RestaurantDataDto(
    @PrimaryKey
    var roomId: String,
    var id: String,
    var name: String?,
    @SerializedName("thumb")
    var thumbRestaurant: String?,
    var thumbRestaurantBitmap: Bitmap? = null,
    var isFavorite: Boolean = false,
    var url: String?,
    @SerializedName("location")
    var restaurantLocation: RestaurantLocationDto,
    @SerializedName("user_rating")
    var rating: RatingDto
) : Parcelable
