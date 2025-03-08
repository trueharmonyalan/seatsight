package com.example.seatsight.data

//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//data for hotel name and hotel description is saved on this data class
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// Prepare HotelDetails data class
data class HotelDetails(
    val name: String,
    val description: String,
    val menuItems: List<String>,
    val restaurantId: Int // ✅ Added `restaurantId`
)

