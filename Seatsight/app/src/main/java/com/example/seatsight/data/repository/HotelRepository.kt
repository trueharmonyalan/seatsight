package com.example.seatsight.data.repository

import com.example.seatsight.data.api.HotelService
import com.example.seatsight.data.model.HotelResponse
import com.example.seatsight.data.model.MenuItem
import com.example.seatsight.data.model.Seat
import com.example.seatsight.data.network.RetrofitClient
import retrofit2.Response

class HotelRepository {
    private val hotelService = RetrofitClient.instance.create(HotelService::class.java)

    suspend fun fetchHotels(): Response<List<HotelResponse>> {
        return hotelService.getHotels()
    }

    suspend fun fetchSeats(restaurantId: Int): Response<List<Seat>> { // ✅ Use correct API route
        return hotelService.getSeats(restaurantId)
    }

    suspend fun fetchMenu(restaurantId: Int): Response<List<MenuItem>> {
        return hotelService.getMenu(restaurantId)
    }

}
