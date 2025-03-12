package com.example.seatsight.data.api

import com.example.seatsight.data.model.ViewSeatResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ViewSeatService {
    @GET("api/restaurants/get-restaurant-id/{restaurant_id}")
    fun getRestaurantId(@Path("restaurant_id") restaurantId: Int): Call<ViewSeatResponse>
}