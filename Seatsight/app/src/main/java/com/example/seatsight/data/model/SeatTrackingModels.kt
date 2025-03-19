package com.example.seatsight.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for seat streaming API's standard REST endpoint.
 * This is used for the non-streaming endpoint that returns a snapshot of seat data.
 */
data class SeatStreamResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("restaurantId")
    val restaurantId: Int,

    @SerializedName("seats")
    val seats: List<RealtimeSeatDto>,

    @SerializedName("timestamp")
    val timestamp: Long? = null
)

/**
 * Data Transfer Object for seat information from the API.
 * This is used to deserialize the JSON response.
 */
data class RealtimeSeatDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("seatNumber")
    val seatNumber: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("isBooked")
    val isBooked: Boolean,

    @SerializedName("posX")
    val posX: Int? = null,

    @SerializedName("posY")
    val posY: Int? = null
) {
    /**
     * Convert DTO to domain model
     */
    fun toRealtimeSeatStatus(): RealtimeSeatStatus {
        return RealtimeSeatStatus(
            id = id,
            seatNumber = seatNumber,
            status = status,
            isBooked = isBooked,
            posX = posX ?: 0,
            posY = posY ?: 0
        )
    }
}