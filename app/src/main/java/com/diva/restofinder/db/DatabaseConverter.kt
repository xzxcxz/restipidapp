package com.diva.restofinder.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import com.diva.restofinder.model.RatingDto
import com.diva.restofinder.model.RestaurantLocationDto
import com.google.gson.Gson
import java.io.ByteArrayOutputStream

object DatabaseConverter {

    const val separator = ","

    @TypeConverter
    fun convertRestaurantLocToJson(restaurantLocationDto: RestaurantLocationDto): String {
        return Gson().toJson(restaurantLocationDto)
    }

    @TypeConverter
    fun convertJsonToRestaurantLoc(json: String): RestaurantLocationDto {
        return Gson().fromJson(json, RestaurantLocationDto::class.java)
    }

    @TypeConverter
    fun convertRatingDtoToJson(ratingDto: RatingDto): String {
        return Gson().toJson(ratingDto)
    }

    @TypeConverter
    fun convertJsonToRatingDto(json: String): RatingDto {
        return Gson().fromJson(json, RatingDto::class.java)
    }

    @TypeConverter
    fun convertListOfStringToString(list: List<String>): String {
        return list.joinToString(separator = separator)
    }

    @TypeConverter
    fun convertStringToListOfString(s: String): List<String> {
        return s.split(separator)
    }

    @TypeConverter
    fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    @TypeConverter
    fun convertByteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }

}