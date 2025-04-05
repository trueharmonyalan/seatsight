package com.example.seatsight.data.repository

import android.util.Log
import com.example.seatsight.data.api.BookingService
import com.example.seatsight.data.model.BookingRequest
import com.example.seatsight.data.model.BookingResponse
import com.example.seatsight.data.network.RetrofitClient
import retrofit2.Response

class BookingRepository {
    private val bookingService = RetrofitClient.instance.create(BookingService::class.java)

    suspend fun createBooking(
        customerId: Int,
        restaurantId: Int,
        selectedSeats: Set<String>,
        selectedMenu: Map<String, Int>,
        startTime: String,
        endTime: String
    ): Response<BookingResponse> {
        val menuItems = selectedMenu.filter { it.value > 0 }
            .map { (item, quantity) -> mapOf("name" to item, "quantity" to quantity) }

        val bookingRequest = BookingRequest(
            customerId = customerId,
            restaurantId = restaurantId,
            selectedSeats = selectedSeats.toList(),  // Changed field name
            selectedMenu = selectedMenu.filter { it.value > 0 },  // Changed field name and format
            startTime = startTime,
            endTime = endTime
        )

        Log.d("BookingRepository", "Creating booking: $bookingRequest")
        return bookingService.createBooking(bookingRequest)
    }
}