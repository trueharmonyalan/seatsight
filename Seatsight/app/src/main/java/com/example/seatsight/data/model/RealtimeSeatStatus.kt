package com.example.seatsight.data.model

import androidx.compose.ui.graphics.Color

data class RealtimeSeatStatus(
    val id: Int,
    val seatNumber: Int,
    val status: String,
    val isBooked: Boolean,
    val posX: Int = 0,
    val posY: Int = 0
) {
    // Check if the seat is physically occupied based on status field
    fun isOccupied(): Boolean = status == "occupied"

    // Check if the seat is reserved through booking system
    fun isReserved(): Boolean = isBooked && status != "occupied"

    // Get status text for display
    fun getStatusText(): String = when {
        isBooked && status == "occupied" -> "Occupied"
        isBooked -> "Reserved"
        status == "occupied" -> "Occupied"
        else -> "Vacant"
    }

    // Get color based on status
    fun getColor(): Color = when {
        isBooked && status == "occupied" -> Color.Red
        isBooked -> Color.Blue
        status == "occupied" -> Color.Red
        else -> Color.Green
    }
}