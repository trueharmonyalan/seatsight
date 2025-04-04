//
//package com.example.seatsight.data.api
//
//import com.example.seatsight.data.model.SeatStreamResponse
//import com.example.seatsight.data.model.TrackingResponse
//import okhttp3.ResponseBody
//import retrofit2.Response
//import retrofit2.http.GET
//import retrofit2.http.POST
//import retrofit2.http.Path
//import retrofit2.http.Streaming
//
///**
// * Service interface for seat tracking API endpoints.
// * Provides methods to fetch real-time seat data.
// */
//interface SeatTrackingService {
//    /**
//     * Get current seats for a restaurant.
//     * Regular REST endpoint for standard API access.
//     */
//    @GET("api/seats/{restaurantId}")
//    suspend fun getSeats(@Path("restaurantId") restaurantId: Int): SeatStreamResponse
//
//    /**
//     * Stream real-time seat updates for a restaurant using Server-Sent Events.
//     * This is a streaming endpoint that keeps the connection open.
//     */
//    @Streaming
//    @GET("api/seats/stream/{restaurantId}")
//    suspend fun streamSeats(@Path("restaurantId") restaurantId: Int): ResponseBody
//
//    /**
//     * Start tracking a restaurant's seat availability on demand.
//     * This initiates seat processing on the server.
//     */
//    @POST("start-tracking/{restaurantId}")
//    suspend fun startTracking(@Path("restaurantId") restaurantId: Int): Response<TrackingResponse>
//
//    /**
//     * Stop tracking a restaurant's seat availability when no longer needed.
//     * This stops seat processing on the server if no other clients are viewing.
//     */
//    @POST("stop-tracking/{restaurantId}")
//    suspend fun stopTracking(@Path("restaurantId") restaurantId: Int): Response<TrackingResponse>
//}

package com.example.seatsight.data.api

import com.example.seatsight.data.model.SeatStreamResponse
import com.example.seatsight.data.model.TrackingResponse
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
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

    /**
     * Start tracking a restaurant's seat availability on demand.
     * This initiates seat processing on the server.
     */
    @POST("start-tracking/{restaurantId}")
    suspend fun startTracking(@Path("restaurantId") restaurantId: Int): Response<TrackingResponse>

    /**
     * Stop tracking a restaurant's seat availability when no longer needed.
     * This stops seat processing on the server if no other clients are viewing.
     */
    @POST("stop-tracking/{restaurantId}")
    suspend fun stopTracking(@Path("restaurantId") restaurantId: Int): Response<TrackingResponse>

    /**
     * Pause tracking a restaurant's seat availability without fully stopping.
     * This keeps resources allocated but suspends processing, saving resources.
     */
    @POST("api/tracking/pause/{restaurantId}")
    suspend fun pauseTracking(@Path("restaurantId") restaurantId: Int): Response<TrackingResponse>

    /**
     * Resume tracking for a previously paused restaurant.
     * This resumes processing without needing to reload resources.
     */
    @POST("api/tracking/resume/{restaurantId}")
    suspend fun resumeTracking(@Path("restaurantId") restaurantId: Int): Response<TrackingResponse>
}