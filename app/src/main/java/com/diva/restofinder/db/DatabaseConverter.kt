package com.diva.restofinder.db

import androidx.room.TypeConverter
import com.diva.restofinder.model.RatingDto
import com.diva.restofinder.model.RestaurantLocationDto
import com.google.gson.Gson

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

}