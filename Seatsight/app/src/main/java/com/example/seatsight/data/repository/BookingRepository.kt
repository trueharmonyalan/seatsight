package com.example.seatsight.data.repository



import com.example.seatsight.data.api.BookingRequest
import com.example.seatsight.data.api.BookingService
import com.example.seatsight.data.model.BookingResponse
import com.example.seatsight.data.network.RetrofitClient
import retrofit2.Call

class BookingRepository {
    private val bookingService = RetrofitClient.instance.create(BookingService::class.java)

    fun bookSeat(restaurantId: Int, seatId: Int, customerId: Int): Call<BookingResponse> {
        return bookingService.bookSeat(BookingRequest(restaurantId, seatId, customerId))
    }
}
