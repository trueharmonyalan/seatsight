package com.example.seatsight.data.model

data class BookingRequest(
    val customerId: Int,
    val restaurantId: Int,
    val selectedSeats: List<String>,  // Changed from "seats"
    val selectedMenu: Map<String, Int>,  // Changed from "menuItems"
    val startTime: String,
    val endTime: String
)

data class BookingResponse(
    val id: Int,
    val message: String,
    val bookingDetails: BookingDetails
)

data class BookingDetails(
    val id: Int,
    val customerId: Int,
    val restaurantId: Int,
    val seats: List<String>,
    val menuItems: List<Map<String, Any>>,
    val startTime: String,
    val endTime: String,
    val status: String,
    val createdAt: String
)