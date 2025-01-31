package com.example.seatsight.data

//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//data for hotel name and hotel description is saved on this data class
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
data class HotelDetails(val name: String, val description: String)
var hotels = listOf(
    HotelDetails(name = "Hotel Kottayam", description = "Kerala cuisine"),
    HotelDetails(name = "Hotel Ettumanoor", description = "arabic cuisine"),

)

