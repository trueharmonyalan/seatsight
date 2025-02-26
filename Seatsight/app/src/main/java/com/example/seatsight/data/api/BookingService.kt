package com.example.seatsight.data.api



import com.example.seatsight.data.model.BookingResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class BookingRequest(val restaurant_id: Int, val seat_id: Int, val customer_id: Int)

interface BookingService {
    @POST("bookings/create")
    fun bookSeat(@Body request: BookingRequest): Call<BookingResponse>
}
