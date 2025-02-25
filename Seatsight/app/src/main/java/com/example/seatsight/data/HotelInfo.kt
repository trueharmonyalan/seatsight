package com.example.seatsight.data

//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//data for hotel name and hotel description is saved on this data class
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
// Prepare HotelDetails data class
data class HotelDetails(
    val name: String,
    val description: String,
    val menuItems: List<String> // New field for menu items
)

// Example Data
var hotels = listOf(
    HotelDetails(
        name = "Hotel Kottayam",
        description = "Kerala cuisine",
        menuItems = listOf("Masala Dosa", "Idli Sambar", "Kerala Parotta")
    ),
    HotelDetails(
        name = "Hotel Ettumanoor",
        description = "Arabic cuisine",
        menuItems = listOf("Shawarma", "Hummus", "Falafel")
    ),
    HotelDetails(
        name = "Hotel Ettumanoor",
        description = "Arabic cuisine",
        menuItems = listOf("Shawarma", "Hummus", "Falafel")
    ),
    HotelDetails(
        name = "Hotel Ettumanoor",
        description = "Arabic cuisine",
        menuItems = listOf("Shawarma", "Hummus", "Falafel")
    )

)
