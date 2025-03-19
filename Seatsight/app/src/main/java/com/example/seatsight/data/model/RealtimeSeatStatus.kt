package com.example.seatsight.data.model

/**
 * Model for real-time seat status updates from the deep learning model.
 * This is separate from the main Seat model to avoid conflicts.
 */
data class RealtimeSeatStatus(
    val id: Int,
    val seatNumber: Int,
    val status: String, // "vacant" or "occupied"
    val isBooked: Boolean,
    val posX: Int,
    val posY: Int
) {
    /**
     * Check if the seat is currently occupied based on status
     */
    fun isOccupied(): Boolean {
        return status == "occupied" || isBooked
    }

    /**
     * Get the display status text
     */
    fun getStatusText(): String {
        return if (isOccupied()) "Occupied" else "Available"
    }

    /**
     * Get the appropriate status color
     */
    fun getStatusColor() = if (isOccupied()) android.graphics.Color.RED else android.graphics.Color.GREEN
}