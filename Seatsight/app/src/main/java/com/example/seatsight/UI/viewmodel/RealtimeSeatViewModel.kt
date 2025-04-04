//
//package com.example.seatsight.ui.viewmodel
//
//import android.util.Log
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.ViewModelProvider
//import androidx.lifecycle.viewModelScope
//import com.example.seatsight.data.model.RealtimeSeatStatus
//import com.example.seatsight.data.model.TrackingResponse
//import com.example.seatsight.data.repository.RealtimeSeatRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.SharingStarted
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.stateIn
//import kotlinx.coroutines.launch
//
///**
// * ViewModel for real-time seat status updates.
// * This is separate from the existing ViewModels to avoid conflicts.
// */
//class RealtimeSeatViewModel : ViewModel() {
//    private val TAG = "RealtimeSeatViewModel"
//    private val repository = RealtimeSeatRepository()
//
//    // StateFlow to expose seat updates to the UI
//    private val _seatStatusList = MutableStateFlow<List<RealtimeSeatStatus>>(emptyList())
//    val seatStatusList: StateFlow<List<RealtimeSeatStatus>> = _seatStatusList.asStateFlow()
//
//    // StateFlow for loading state
//    private val _isLoading = MutableStateFlow(true)
//    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
//
//    // StateFlow for error messages
//    private val _error = MutableStateFlow<String?>(null)
//    val error: StateFlow<String?> = _error.asStateFlow()
//
//    // StateFlow for connection status
//    private val _connectionStatus = MutableStateFlow("Initializing...")
//    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()
//
//    // Track currently streaming restaurant to avoid duplicate streams
//    private var currentRestaurantId: Int? = null
//
//    /**
//     * Start tracking a restaurant's seat availability on demand.
//     *
//     * @param restaurantId The ID of the restaurant to track
//     */
//    fun startTracking(restaurantId: Int) {
//        _isLoading.value = true
//        _connectionStatus.value = "Starting seat tracking..."
//
//        viewModelScope.launch {
//            try {
//                // Make the API call to start tracking
//                val result = repository.startTracking(restaurantId)
//
//                result.fold(
//                    onSuccess = { response ->
//                        Log.d(TAG, "Tracking started with ${response.viewers} viewers")
//                        _connectionStatus.value = "Tracking started successfully, waiting for initial data..."
//
//                        // Now start streaming real-time updates
//                        startRealtimeUpdates(restaurantId)
//                    },
//                    onFailure = { throwable ->
//                        _error.value = "Failed to start tracking: ${throwable.message}"
//                        _connectionStatus.value = "Failed to start tracking"
//                        _isLoading.value = false
//                    }
//                )
//            } catch (e: Exception) {
//                Log.e(TAG, "Error starting tracking", e)
//                _error.value = "Error starting tracking: ${e.message}"
//                _connectionStatus.value = "Error starting tracking"
//                _isLoading.value = false
//            }
//        }
//    }
//
//    /**
//     * Stop tracking a restaurant's seat availability when no longer needed.
//     *
//     *
//     * @param restaurantId The ID of the restaurant to stop tracking
//     */
//    fun stopTracking(restaurantId: Int) {
//        // First stop the real-time updates to prevent data flow
//        stopRealtimeUpdates()
//
//        viewModelScope.launch {
//            try {
//                // Make the API call to stop tracking
//                val result = repository.stopTracking(restaurantId)
//
//                result.fold(
//                    onSuccess = { response ->
//                        Log.d(TAG, "Tracking stopped: ${response.message}")
//                    },
//                    onFailure = { throwable ->
//                        // Just log the error but don't propagate it to the UI
//                        Log.e(TAG, "Failed to stop tracking, but proceeded anyway", throwable)
//                    }
//                )
//            } catch (e: Exception) {
//                // Just log the error but don't propagate it to the UI
//                Log.e(TAG, "Error stopping tracking, but proceeded anyway", e)
//            }
//        }
//    }
//    /**
//     * Start streaming real-time seat updates for a specific restaurant.
//     * This method handles both starting the tracking and streaming data.
//     *
//     * @param restaurantId The ID of the restaurant to stream updates for
//     */
//    fun startRealtimeUpdates(restaurantId: Int) {
//        // Don't restart if already streaming for this restaurant
//        if (currentRestaurantId == restaurantId) {
//            Log.d(TAG, "Already streaming for restaurant $restaurantId")
//            return
//        }
//
//        currentRestaurantId = restaurantId
//        _connectionStatus.value = "Starting real-time tracking..."
//        _isLoading.value = true
//
//        // Start tracking for the given restaurant
//        viewModelScope.launch {
//            try {
//                // First call the API to start tracking
//                val result = repository.startTracking(restaurantId)
//
//                result.fold(
//                    onSuccess = { response ->
//                        Log.d(TAG, "Successfully started tracking for restaurant $restaurantId")
//                        _connectionStatus.value = "Tracking started, connecting to stream..."
//
//                        // After successfully starting tracking, initiate the SSE connection
//                        connectToStream(restaurantId)
//                    },
//                    onFailure = { throwable ->
//                        Log.e(TAG, "Failed to start tracking", throwable)
//                        _error.value = "Could not start tracking: ${throwable.message}"
//                        _connectionStatus.value = "Failed to start tracking"
//                        _isLoading.value = false
//                    }
//                )
//            } catch (e: Exception) {
//                Log.e(TAG, "Error in startRealtimeUpdates", e)
//                _error.value = "Error starting real-time updates: ${e.message}"
//                _connectionStatus.value = "Error starting updates"
//                _isLoading.value = false
//            }
//        }
//    }
//
//    /**
//     * Connect to the SSE stream for real-time updates.
//     */
//    private fun connectToStream(restaurantId: Int) {
//        viewModelScope.launch {
//            try {
//                _connectionStatus.value = "Connecting to seat data stream..."
//
//                // Start collecting from the SSE stream
//                repository.streamSeats(restaurantId)
//                    .collect { updatedSeats ->
//                        // Only process non-empty updates
//                        if (updatedSeats.isNotEmpty()) {
//                            Log.d(TAG, "Received ${updatedSeats.size} seats in real-time update")
//                            _seatStatusList.value = updatedSeats
//                            _isLoading.value = false
//                            _connectionStatus.value = "Connected"
//                            _error.value = null
//                        }
//                    }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error while collecting seat updates", e)
//                _error.value = "Error in data stream: ${e.message}"
//                _isLoading.value = false
//            }
//        }
//    }
//    /**
//     * Stop streaming updates.
//     */
//    /**
//     * Stop streaming updates.
//     * Closes connections without throwing exceptions.
//     */
//    fun stopRealtimeUpdates() {
//        try {
//            val previousId = currentRestaurantId
//            currentRestaurantId = null
//
//            if (previousId != null) {
//                Log.d(TAG, "Stopping real-time updates for restaurant: $previousId")
//            }
//
//            repository.closeConnections()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error while stopping real-time updates", e)
//        }
//    }
//    /**
//     * Clean up resources when the ViewModel is cleared.
//     */
//    override fun onCleared() {
//        super.onCleared()
//        stopRealtimeUpdates()
//    }
//}
//
///**
// * Factory for creating RealtimeSeatViewModel instances.
// */
//class RealtimeSeatViewModelFactory : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(RealtimeSeatViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return RealtimeSeatViewModel() as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}
package com.example.seatsight.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.seatsight.data.model.RealtimeSeatStatus
import com.example.seatsight.data.model.TrackingResponse
import com.example.seatsight.data.repository.RealtimeSeatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
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

    // StateFlow for loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // StateFlow for error messages
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // StateFlow for connection status
    private val _connectionStatus = MutableStateFlow("Initializing...")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    // StateFlow for tracking paused state
    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    // Track currently streaming restaurant to avoid duplicate streams
    private var currentRestaurantId: Int? = null

    // Flag to prevent multiple simultaneous pause/stop operations
    private var isShuttingDown = false

    /**
     * Start tracking a restaurant's seat availability on demand.
     * This will check if the restaurant is already being tracked first.
     *
     * @param restaurantId The ID of the restaurant to track
     */
    fun startTracking(restaurantId: Int) {
        // Check if we're already tracking this restaurant
        if (currentRestaurantId == restaurantId) {
            // If the restaurant is the same but paused, resume it
            if (_isPaused.value) {
                Log.d(TAG, "Restaurant $restaurantId is paused, resuming instead of starting new")
                resumeTracking(restaurantId)
                return
            }

            Log.d(TAG, "Already tracking restaurant $restaurantId")
            return
        }

        // Reset shutdown flag
        isShuttingDown = false

        _isLoading.value = true
        _connectionStatus.value = "Starting seat tracking..."
        _isPaused.value = false

        viewModelScope.launch {
            try {
                // Make the API call to start tracking
                val result = repository.startTracking(restaurantId)

                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Tracking started with ${response.viewers} viewers")
                        _connectionStatus.value = "Tracking started successfully, waiting for initial data..."

                        // Now start streaming real-time updates
                        startRealtimeUpdates(restaurantId)
                    },
                    onFailure = { throwable ->
                        _error.value = "Failed to start tracking: ${throwable.message}"
                        _connectionStatus.value = "Failed to start tracking"
                        _isLoading.value = false
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error starting tracking", e)
                _error.value = "Error starting tracking: ${e.message}"
                _connectionStatus.value = "Error starting tracking"
                _isLoading.value = false
            }
        }
    }

    /**
     * IMPORTANT: This method is used when completely shutting down tracking
     * Only use when the app is completely done with this restaurant data
     * For temporary navigation away, use pauseTracking instead
     */
    fun stopTracking(restaurantId: Int) {
        // Prevent duplicate calls
        if (isShuttingDown) {
            Log.d(TAG, "Stop operation already in progress, ignoring duplicate request")
            return
        }

        isShuttingDown = true

        // First stop the real-time updates to prevent data flow
        stopRealtimeUpdates()

        // Reset paused state
        _isPaused.value = false

        viewModelScope.launch {
            try {
                // Make the API call to stop tracking
                val result = repository.stopTracking(restaurantId)

                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Tracking stopped: ${response.message}")
                    },
                    onFailure = { throwable ->
                        // Just log the error but don't propagate it to the UI
                        Log.e(TAG, "Failed to stop tracking, but proceeded anyway", throwable)
                    }
                )
            } catch (e: Exception) {
                // Just log the error but don't propagate it to the UI
                Log.e(TAG, "Error stopping tracking, but proceeded anyway", e)
            } finally {
                isShuttingDown = false
            }
        }
    }

    /**
     * Pause tracking for a restaurant without fully stopping it.
     * This keeps resources allocated but suspends processing.
     *
     * Use this for temporary navigation away from the seat view
     *
     * @param restaurantId The ID of the restaurant to pause tracking for
     */
    fun pauseTracking(restaurantId: Int) {
        // Prevent operations if we're already shutting down
        if (isShuttingDown) {
            Log.d(TAG, "Shutdown in progress, ignoring pause request")
            return
        }

        // Only pause if we actually have something to pause
        if (currentRestaurantId != restaurantId) {
            Log.w(TAG, "Not currently tracking restaurant $restaurantId, cannot pause")
            return
        }

        _connectionStatus.value = "Pausing tracking..."

        // Set paused state immediately for UI responsiveness
        _isPaused.value = true

        viewModelScope.launch {
            try {
                // We're keeping the SSE connection open to maintain the session
                // Just call the pause API without closing connections
                Log.d(TAG, "Pausing tracking for restaurant $restaurantId")
                val result = repository.pauseTracking(restaurantId)

                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Tracking paused: ${response.message}")
                        _connectionStatus.value = "Tracking paused"
                    },
                    onFailure = { throwable ->
                        Log.e(TAG, "Failed to pause tracking", throwable)
                        _error.value = "Failed to pause tracking: ${throwable.message}"
                        _connectionStatus.value = "Failed to pause tracking"
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error pausing tracking", e)
                _error.value = "Error pausing tracking: ${e.message}"
            }
        }
    }

    /**
     * Resume tracking for a previously paused restaurant.
     *
     * @param restaurantId The ID of the restaurant to resume tracking for
     */
    fun resumeTracking(restaurantId: Int) {
        // Prevent operations if we're already shutting down
        if (isShuttingDown) {
            Log.d(TAG, "Shutdown in progress, ignoring resume request")
            return
        }

        // If not paused, nothing to do
        if (!_isPaused.value) {
            Log.d(TAG, "Tracking for restaurant $restaurantId is not paused, nothing to resume")
            return
        }

        _connectionStatus.value = "Resuming tracking..."

        viewModelScope.launch {
            try {
                // Call resume API
                val result = repository.resumeTracking(restaurantId)

                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "Tracking resumed: ${response.message}")
                        _isPaused.value = false
                        _connectionStatus.value = "Tracking resumed"

                        // If we don't have a current restaurant ID, we need to start streaming updates
                        if (currentRestaurantId == null) {
                            startRealtimeUpdates(restaurantId)
                        }
                    },
                    onFailure = { throwable ->
                        Log.e(TAG, "Failed to resume tracking", throwable)
                        _error.value = "Failed to resume tracking: ${throwable.message}"
                        _connectionStatus.value = "Failed to resume tracking"
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error resuming tracking", e)
                _error.value = "Error resuming tracking: ${e.message}"
            }
        }
    }

    /**
     * Start streaming real-time seat updates for a specific restaurant.
     * This method handles both starting the tracking and streaming data.
     *
     * @param restaurantId The ID of the restaurant to stream updates for
     */
    fun startRealtimeUpdates(restaurantId: Int) {
        // Don't restart if already streaming for this restaurant
        if (currentRestaurantId == restaurantId) {
            Log.d(TAG, "Already streaming for restaurant $restaurantId")
            return
        }

        currentRestaurantId = restaurantId
        _connectionStatus.value = "Starting real-time tracking..."
        _isLoading.value = true

        // Connect to the stream - we already called startTracking before this
        connectToStream(restaurantId)
    }

    /**
     * Connect to the SSE stream for real-time updates.
     */
    private fun connectToStream(restaurantId: Int) {
        viewModelScope.launch {
            try {
                _connectionStatus.value = "Connecting to seat data stream..."

                // Start collecting from the SSE stream
                repository.streamSeats(restaurantId)
                    .collect { updatedSeats ->
                        // Only process non-empty updates
                        if (updatedSeats.isNotEmpty()) {
                            Log.d(TAG, "Received ${updatedSeats.size} seats in real-time update")
                            _seatStatusList.value = updatedSeats
                            _isLoading.value = false
                            _connectionStatus.value = if (_isPaused.value) "Paused" else "Connected"
                            _error.value = null
                        }
                    }
            } catch (e: Exception) {
                // Only log the error if we're not shutting down
                if (!isShuttingDown) {
                    Log.e(TAG, "Error while collecting seat updates", e)
                    _error.value = "Error in data stream: ${e.message}"
                    _isLoading.value = false
                }
            }
        }
    }
    /**
     * Stop streaming updates.
     * Closes connections without throwing exceptions.
     */
    fun stopRealtimeUpdates() {
        try {
            val previousId = currentRestaurantId
            currentRestaurantId = null

            if (previousId != null) {
                Log.d(TAG, "Stopping real-time updates for restaurant: $previousId")
            }

            repository.closeConnections()
        } catch (e: Exception) {
            Log.e(TAG, "Error while stopping real-time updates", e)
        }
    }

    /**
     * Clean up resources when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()

        // Make sure to track that we're shutting down
        isShuttingDown = true

        // Important: When ViewModel is cleared, we should truly stop everything
        currentRestaurantId?.let { restaurantId ->
            // First close connections to prevent race conditions
            stopRealtimeUpdates()

            // Then stop tracking properly via API
            viewModelScope.launch {
                try {
                    repository.stopTracking(restaurantId)
                    Log.d(TAG, "Successfully stopped tracking on ViewModel clear")
                } catch (e: Exception) {
                    Log.e(TAG, "Error stopping tracking during ViewModel clear", e)
                }
            }
        }
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