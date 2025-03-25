package com.example.seatsight.data.model

import com.google.gson.annotations.SerializedName

/**
 * Response model for tracking control endpoints.
 */
data class TrackingResponse(
    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String,

    @SerializedName("viewers")
    val viewers: Int? = null,

    @SerializedName("remaining_viewers")
    val remainingViewers: Int? = null
)