package com.example.seatsight.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seatsight.data.model.RealtimeSeatStatus
import com.example.seatsight.data.repository.RealtimeSeatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for real-time seat status updates.
 * This is separate from the existing ViewModels to avoid conflicts.
 */
class RealtimeSeatViewModel : ViewModel() {
    private val TAG = "RealtimeSeatViewModel"
    private val repository = RealtimeSeatRepository()

    // StateFlow to expose seat updates to the UI
    private val _seatStatusList = MutableStateFlow<List<RealtimeSeatStatus>>(emptyList())
    val seatStatusList: StateFlow<List<RealtimeSeatStatus>> = _seatStatusList.asStateFlow()

    // Track currently streaming restaurant to avoid duplicate streams
    private var currentRestaurantId: Int? = null

    /**
     * Start streaming real-time seat updates for a specific restaurant.
     *
     * @param restaurantId The ID of the restaurant to stream updates for
     */
    fun startRealtimeUpdates(restaurantId: Int) {
        // Don't restart if already streaming for this restaurant
        if (currentRestaurantId == restaurantId) {
            return
        }

        currentRestaurantId = restaurantId
        Log.d(TAG, "Starting real-time seat updates for restaurant: $restaurantId")

        viewModelScope.launch {
            repository.streamSeats(restaurantId).collect { updatedSeats ->
                if (updatedSeats.isNotEmpty()) {
                    _seatStatusList.value = updatedSeats
                    Log.d(TAG, "Updated seats: ${updatedSeats.size}")
                }
            }
        }
    }

    /**
     * Stop streaming updates.
     */
    fun stopRealtimeUpdates() {
        currentRestaurantId = null
    }
}

/**
 * Factory for creating RealtimeSeatViewModel instances.
 */
class RealtimeSeatViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RealtimeSeatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RealtimeSeatViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}