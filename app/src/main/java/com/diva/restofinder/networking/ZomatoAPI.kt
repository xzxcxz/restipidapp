package com.diva.restofinder.networking

import com.diva.restofinder.model.*
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface ZomatoAPI {

    @Headers("user-key: b47b1abf3c3436d473570116cd8a2621")
    @GET(ApiEndpoint.Collection)
    suspend fun getCollection(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String
    ): Response<CollectionResponseDto>

    @Headers("user-key: b47b1abf3c3436d473570116cd8a2621")
    @GET(ApiEndpoint.Geocode)
    suspend fun getGeocode(
        @Query("lat") latitude: String,
        @Query("lon") longitude: String
    ): GeocodeResponseDto

    @Headers("user-key: b47b1abf3c3436d473570116cd8a2621")
    @GET(ApiEndpoint.DetailRestaurant)
    suspend fun getRestaurantDetail(
        @Query("res_id") restaurantId: String
    ): Response<RestaurantDetailResponseDto>

    @Headers("user-key: b47b1abf3c3436d473570116cd8a2621")
    @GET(ApiEndpoint.SearchEndpoint)
    suspend fun searchRestaurant(
        @Query("q") query: String,
        @Query("lat") latitude: String,
        @Query("lon") longitude: String,
        @Query("radius") radius: Int = 20000,
        @Query("sort") sort: String = "cost",
        @Query("order") order: String = "asc"
    ): Response<SearchResponseDto>

}