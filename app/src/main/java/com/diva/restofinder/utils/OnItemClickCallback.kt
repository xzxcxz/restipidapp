package com.diva.restofinder.utils

import com.diva.restofinder.model.RestaurantResponseDto

interface OnItemClickCallback {
    fun onItemMainClicked(restaurantResponseDtoHorizontal: RestaurantResponseDto?)
}