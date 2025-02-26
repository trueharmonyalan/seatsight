package com.example.seatsight.data.api

import com.example.seatsight.data.model.HotelResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface HotelService {
    @GET("/api/restaurants/android/hotels-menu") // ✅ Get list of available hotels
    suspend fun getHotels(): Response<List<HotelResponse>>
}
