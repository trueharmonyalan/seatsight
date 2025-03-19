package com.example.seatsight.data.repository

import android.util.Log
import com.example.seatsight.data.model.RealtimeSeatStatus
import com.example.seatsight.data.network.SseEventSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.json.JSONObject

/**
 * Repository for accessing real-time seat tracking data from the deep model API.
 */
class RealtimeSeatRepository {
    private val TAG = "RealtimeSeatRepository"
    private val sseEventSource = SseEventSource()

    /**
     * Stream real-time seat updates for a restaurant.
     *
     * @param restaurantId The ID of the restaurant
     * @return Flow emitting seat lists that updates in real-time
     */
    fun streamSeats(restaurantId: Int): Flow<List<RealtimeSeatStatus>> {
        val url = "${BASE_URL}api/seats/stream/$restaurantId"

        return sseEventSource.connect(url)
            .map { eventData ->
                try {
                    // Parse the JSON data from the SSE event
                    val jsonObject = JSONObject(eventData)
                    val seatsArray = jsonObject.getJSONArray("seats")

                    val seats = mutableListOf<RealtimeSeatStatus>()
                    for (i in 0 until seatsArray.length()) {
                        val seatObject = seatsArray.getJSONObject(i)

                        // Extract all properties including status
                        val seatId = seatObject.getInt("id")
                        val seatNumber = seatObject.getInt("seatNumber")
                        val status = seatObject.getString("status")  // "vacant" or "occupied"
                        val isBooked = seatObject.optBoolean("isBooked", false)
                        val posX = seatObject.optInt("posX", 0)
                        val posY = seatObject.optInt("posY", 0)

                        // Create the RealtimeSeatStatus object
                        val seat = RealtimeSeatStatus(
                            id = seatId,
                            seatNumber = seatNumber,
                            status = status,
                            isBooked = isBooked,
                            posX = posX,
                            posY = posY
                        )

                        seats.add(seat)
                    }

                    seats
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing SSE event data", e)
                    emptyList()
                }
            }
            .flowOn(Dispatchers.IO)
    }

    companion object {
        // Update this with your actual deep model server URL
        // For emulator testing with localhost server, use 10.0.2.2 instead of localhost
        private const val BASE_URL = "http://192.168.1.11:3003"
    }
}