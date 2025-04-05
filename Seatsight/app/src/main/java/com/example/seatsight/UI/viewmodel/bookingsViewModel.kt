package com.example.seatsight.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seatsight.data.repository.BookingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BookingViewModel(private val bookingRepository: BookingRepository) : ViewModel() {

    private val _bookingResult = MutableStateFlow<Result<String>?>(null)
    val bookingResult: StateFlow<Result<String>?> = _bookingResult

    fun createBooking(
        customerId: Int,
        restaurantId: Int,
        selectedSeats: Set<String>,
        selectedMenu: Map<String, Int>,
        startTime: String,
        endTime: String
    ) {
        viewModelScope.launch {
            try {
                val response = bookingRepository.createBooking(
                    customerId, restaurantId, selectedSeats, selectedMenu, startTime, endTime
                )

                if (response.isSuccessful) {
                    _bookingResult.value = Result.success("Booking created successfully!")
                } else {
                    _bookingResult.value = Result.failure(
                        Exception("Failed to create booking: ${response.errorBody()?.string()}")
                    )
                }
            } catch (e: Exception) {
                Log.e("BookingViewModel", "Error creating booking", e)
                _bookingResult.value = Result.failure(e)
            }
        }
    }
}