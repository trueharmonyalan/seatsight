package com.example.seatsight.data.api


import com.example.seatsight.data.model.SeatStreamResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Streaming

/**
 * Service interface for seat tracking API endpoints.
 * Provides methods to fetch real-time seat data.
 */
interface SeatTrackingService {
    /**
     * Get current seats for a restaurant.
     * Regular REST endpoint for standard API access.
     */
    @GET("api/seats/{restaurantId}")
    suspend fun getSeats(@Path("restaurantId") restaurantId: Int): SeatStreamResponse

    /**
     * Stream real-time seat updates for a restaurant using Server-Sent Events.
     * This is a streaming endpoint that keeps the connection open.
     */
    @Streaming
    @GET("api/seats/stream/{restaurantId}")
    suspend fun streamSeats(@Path("restaurantId") restaurantId: Int): ResponseBody
}