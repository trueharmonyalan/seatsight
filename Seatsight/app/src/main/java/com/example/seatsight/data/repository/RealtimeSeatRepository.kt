//package com.example.seatsight.data.repository
//
//import android.util.Log
//import com.example.seatsight.data.model.RealtimeSeatStatus
//import com.example.seatsight.data.network.SseEventSource
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.flow.flowOn
//import kotlinx.coroutines.flow.map
//import org.json.JSONObject
//
///**
// * Repository for accessing real-time seat tracking data from the deep model API.
// */
//class RealtimeSeatRepository {
//    private val TAG = "RealtimeSeatRepository"
//    private val sseEventSource = SseEventSource()
//
//    /**
//     * Stream real-time seat updates for a restaurant.
//     *
//     * @param restaurantId The ID of the restaurant
//     * @return Flow emitting seat lists that updates in real-time
//     */
//    fun streamSeats(restaurantId: Int): Flow<List<RealtimeSeatStatus>> {
//        val url = "${BASE_URL}api/seats/stream/$restaurantId"
//
//        return sseEventSource.connect(url)
//            .map { eventData ->
//                try {
//                    // Parse the JSON data from the SSE event
//                    val jsonObject = JSONObject(eventData)
//                    val seatsArray = jsonObject.getJSONArray("seats")
//
//                    val seats = mutableListOf<RealtimeSeatStatus>()
//                    for (i in 0 until seatsArray.length()) {
//                        val seatObject = seatsArray.getJSONObject(i)
//
//                        // Extract all properties including status
//                        val seatId = seatObject.getInt("id")
//                        val seatNumber = seatObject.getInt("seatNumber")
//                        val status = seatObject.getString("status")  // "vacant" or "occupied"
//                        val isBooked = seatObject.optBoolean("isBooked", false)
//                        val posX = seatObject.optInt("posX", 0)
//                        val posY = seatObject.optInt("posY", 0)
//
//                        // Create the RealtimeSeatStatus object
//                        val seat = RealtimeSeatStatus(
//                            id = seatId,
//                            seatNumber = seatNumber,
//                            status = status,
//                            isBooked = isBooked,
//                            posX = posX,
//                            posY = posY
//                        )
//
//                        seats.add(seat)
//                    }
//
//                    seats
//                } catch (e: Exception) {
//                    Log.e(TAG, "Error parsing SSE event data", e)
//                    emptyList()
//                }
//            }
//            .flowOn(Dispatchers.IO)
//    }
//
//    companion object {
//        // Update this with your actual deep model server URL
//        // For emulator testing with localhost server, use 10.0.2.2 instead of localhost
//        private const val BASE_URL = "http://192.168.1.11:3003"
//    }
//}
package com.example.seatsight.data.repository

import android.util.Log
import com.example.seatsight.data.api.SeatTrackingService
import com.example.seatsight.data.model.RealtimeSeatStatus
import com.example.seatsight.data.model.TrackingResponse
import com.example.seatsight.data.network.SseEventSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Repository for accessing real-time seat tracking data from the deep model API.
 */
class RealtimeSeatRepository {
    private val TAG = "RealtimeSeatRepository"
    private val sseEventSource = SseEventSource()

    // Create a dedicated Retrofit instance for this repository
    private val apiService: SeatTrackingService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(SeatTrackingService::class.java)

    /**
     * Start tracking a restaurant's seat availability on demand.
     * This needs to be called before connecting to the SSE stream.
     *
     * @param restaurantId The ID of the restaurant to track
     * @return Result containing success or failure information
     */
    suspend fun startTracking(restaurantId: Int): Result<TrackingResponse> {
        return try {
            Log.d(TAG, "Starting tracking for restaurant: $restaurantId")
            val response = apiService.startTracking(restaurantId)

            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!
                Log.d(TAG, "Successfully started tracking: ${result.message}")
                Result.success(result)
            } else {
                Log.e(TAG, "Failed to start tracking: ${response.errorBody()?.string()}")
                Result.failure(Exception("Failed to start tracking: ${response.code()} ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting tracking", e)
            Result.failure(e)
        }
    }

    /**
     * Stop tracking a restaurant's seat availability when no longer needed.
     *
     * @param restaurantId The ID of the restaurant to stop tracking
     * @return Result containing success or failure information
     */
    suspend fun stopTracking(restaurantId: Int): Result<TrackingResponse> {
        // First close any SSE connections
        closeConnections()

        // Then call the API to stop tracking
        return try {
            Log.d(TAG, "Stopping tracking for restaurant: $restaurantId")
            val response = apiService.stopTracking(restaurantId)

            if (response.isSuccessful && response.body() != null) {
                val result = response.body()!!
                Log.d(TAG, "Successfully stopped tracking: ${result.message}")
                Result.success(result)
            } else {
                Log.w(TAG, "Server returned error when stopping tracking: ${response.errorBody()?.string()}")
                val fallbackResponse = TrackingResponse(
                    status = "warning",
                    message = "Server communication issue, but tracking stopped locally",
                    remainingViewers = null
                )
                Result.success(fallbackResponse)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping tracking", e)
            val fallbackResponse = TrackingResponse(
                status = "warning",
                message = "Could not contact server, but tracking stopped locally",
                remainingViewers = null
            )
            Result.success(fallbackResponse)
        }
    }

    /**
     * Stream real-time seat updates for a restaurant.
     *
     * @param restaurantId The ID of the restaurant
     * @return Flow emitting seat lists that updates in real-time
     */
    fun streamSeats(restaurantId: Int): Flow<List<RealtimeSeatStatus>> {
        val url = "${BASE_URL}api/seats/stream/$restaurantId"

        return sseEventSource.connect(url)
            .onEach { data ->
                // Log each incoming event to help with debugging
                if (data != "CONNECTION_ESTABLISHED" && data != "CONNECTION_ERROR" && data != "CONNECTION_CLOSED") {
                    Log.d(TAG, "Raw SSE data: ${data.take(100)}...")
                }
            }
            .map { eventData ->
                try {
                    // Handle special connection status messages
                    if (eventData == "CONNECTION_ESTABLISHED" ||
                        eventData == "CONNECTION_ERROR" ||
                        eventData == "CONNECTION_CLOSED") {
                        Log.d(TAG, "SSE status message: $eventData for restaurant $restaurantId")
                        return@map emptyList<RealtimeSeatStatus>()
                    }

                    // Parse the JSON data
                    val jsonObject = JSONObject(eventData)

                    // Skip heartbeat messages
                    if (jsonObject.has("heartbeat")) {
                        Log.d(TAG, "Received heartbeat from server")
                        return@map emptyList<RealtimeSeatStatus>()
                    }

                    // Handle error messages
                    if (jsonObject.has("error")) {
                        Log.e(TAG, "Server reported error: ${jsonObject.getString("error")}")
                        return@map emptyList<RealtimeSeatStatus>()
                    }

                    // Process seat data if available
                    val seatsArray = jsonObject.optJSONArray("seats")
                    if (seatsArray == null) {
                        Log.d(TAG, "No seats data in the event")
                        return@map emptyList<RealtimeSeatStatus>()
                    }

                    Log.d(TAG, "Processing ${seatsArray.length()} seats from SSE update")

                    val seats = mutableListOf<RealtimeSeatStatus>()
                    for (i in 0 until seatsArray.length()) {
                        val seatObject = seatsArray.getJSONObject(i)
                        val seatId = seatObject.getInt("id")
                        val seatNumber = seatObject.getInt("seatNumber")
                        val status = seatObject.getString("status")
                        val isBooked = seatObject.optBoolean("isBooked", false)
                        val posX = seatObject.optInt("posX", 0)
                        val posY = seatObject.optInt("posY", 0)

                        seats.add(RealtimeSeatStatus(
                            id = seatId,
                            seatNumber = seatNumber,
                            status = status,
                            isBooked = isBooked,
                            posX = posX,
                            posY = posY
                        ))
                    }

                    Log.d(TAG, "Successfully parsed ${seats.size} seats from update")
                    seats
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing SSE event data", e)
                    emptyList()
                }
            }
            .retry(3) { e ->
                // Retry only network errors, not parsing errors
                Log.w(TAG, "SSE connection error, retrying", e)
                delay(1000) // Wait before retry
                e is java.io.IOException
            }
            .catch { e ->
                Log.e(TAG, "Unrecoverable error in seat stream", e)
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Close any open SSE connections.
     */
    fun closeConnections() {
        Log.d(TAG, "Closing all SSE connections")
        sseEventSource.closeConnection()
    }

    companion object {
        // Update this with your actual deep model server URL
        private const val BASE_URL = "http://192.168.1.11:3003/"
    }
}