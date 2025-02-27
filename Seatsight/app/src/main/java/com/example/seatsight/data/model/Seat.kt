package com.example.seatsight.data.model

import com.google.gson.annotations.SerializedName

data class Seat(
    @SerializedName("seatid") val seatId: Int,         // ✅ Matches API field "seatid"
    @SerializedName("seatnumber") val seatNumber: Int, // ✅ Matches API field "seatnumber"
    @SerializedName("is_booked") val isBooked: Boolean // ✅ Matches API field "is_booked"
)
