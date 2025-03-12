package com.example.seatsight.data.api


import com.example.seatsight.data.model.Restaurant
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RestaurantService {
    @GET("restaurants/owner/{owner_id}")
    fun getRestaurant(@Path("owner_id") ownerId: Int): Call<Restaurant>


}
