package com.example.seatsight.data.repository

import com.example.seatsight.data.api.HotelService
import com.example.seatsight.data.model.HotelResponse
import com.example.seatsight.data.network.RetrofitClient
import retrofit2.Response

class HotelRepository {
    private val hotelService = RetrofitClient.instance.create(HotelService::class.java)

    suspend fun fetchHotels(): Response<List<HotelResponse>> {
        return hotelService.getHotels()
    }

}
