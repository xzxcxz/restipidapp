package com.diva.restofinder.utils

import com.diva.restofinder.model.RestaurantDataDto
import com.diva.restofinder.model.RestaurantResponseDto

interface OnMainAdapterCallback {
    fun onItemMainClicked(restaurantResponseDto: RestaurantResponseDto)
    fun onItemMainClicked(restaurant: RestaurantDataDto)
    fun onFavoriteClicked(restaurantResponseDto: RestaurantResponseDto, isFavorite: Boolean)
    fun onFavoriteClicked(restaurant: RestaurantDataDto, isFavorite: Boolean)
}