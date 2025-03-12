package com.example.seatsight.data.repository

import com.example.seatsight.data.api.ViewSeatService
import com.example.seatsight.data.model.ViewSeatResponse
import com.example.seatsight.data.network.RetrofitClient
import retrofit2.Call

class ViewSeatRepository {
    private val viewSeatService = RetrofitClient.instance.create(ViewSeatService::class.java)

    fun getRestaurantId(restaurantId: Int): Call<ViewSeatResponse> {
        return viewSeatService.getRestaurantId(restaurantId)
    }
}