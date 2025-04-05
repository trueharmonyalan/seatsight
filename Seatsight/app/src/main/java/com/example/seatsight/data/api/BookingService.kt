package com.example.seatsight.data.api

import com.example.seatsight.data.model.BookingRequest
import com.example.seatsight.data.model.BookingResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface BookingService {
    @POST("api/bookings/")
    suspend fun createBooking(@Body request: BookingRequest): Response<BookingResponse>
}