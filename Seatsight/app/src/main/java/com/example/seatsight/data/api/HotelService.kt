package com.example.seatsight.data.api

import com.example.seatsight.data.model.HotelResponse
import com.example.seatsight.data.model.MenuItem
import com.example.seatsight.data.model.Seat
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface HotelService {
    @GET("/api/restaurants/android/hotels-menu") // ✅ Get list of available hotels
    suspend fun getHotels(): Response<List<HotelResponse>>

    @GET("/api/seats/{restaurant_id}") // ✅ Ensure correct API path
    suspend fun getSeats(@Path("restaurant_id") restaurantId: Int): Response<List<Seat>>

    @GET("/api/menu/{restaurant_id}") // ✅ Updated menu API path
    suspend fun getMenu(@Path("restaurant_id") restaurantId: Int): Response<List<MenuItem>>

}
