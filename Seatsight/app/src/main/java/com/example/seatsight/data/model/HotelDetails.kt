package com.example.seatsight.data.model





data class HotelResponse(
    val restaurant_id: Int, // ✅ Matches API response
    val hotel_name: String,   // ✅ Matches API response
    val hotel_description: String,
    val menu: List<MenuItem>  // ✅ Stores menu items as a list of objects
)

data class MenuItem(
    val id: Int,
    val name: String,
    val price: Double
)

