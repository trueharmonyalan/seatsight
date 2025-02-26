package com.example.seatsight.data.repository


import com.example.seatsight.data.api.RestaurantService
import com.example.seatsight.data.model.Restaurant
import com.example.seatsight.data.network.RetrofitClient
import retrofit2.Call

class RestaurantRepository {
    private val restaurantService = RetrofitClient.instance.create(RestaurantService::class.java)

    fun getRestaurant(ownerId: Int): Call<Restaurant> {
        return restaurantService.getRestaurant(ownerId)
    }
}
