package com.example.seatsight.UI


import HotelViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.UI.viewmodel.HotelViewModelFactory
import com.example.seatsight.data.HotelDetails
import com.example.seatsight.data.model.Seat
import com.example.seatsight.data.repository.HotelRepository


import com.example.seatsight.ui.theme.SeatsightTheme
@Composable
fun ViewSeatsScreen(
    hotelName: String,
    restaurantId: Int,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val repository = remember { HotelRepository() }
    val viewModel: HotelViewModel = viewModel(factory = HotelViewModelFactory(repository))

    val seats by viewModel.seatList.collectAsState() // ✅ Get seat data from ViewModel

    // ✅ Fetch seats when screen is opened
    LaunchedEffect(restaurantId) {
        viewModel.fetchSeats(restaurantId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .background(surfaceColor)
        ) {
            Text(
                text = "Seats for $hotelName",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // ✅ Show seats dynamically
//            DisplaySeatsForHotel(seats)
        }
    }
}

//@Composable
//fun DisplaySeatsForHotel(seats: List<Seat>) {
//    if (seats.isEmpty()) {
//        Text(
//            text = "No seat data available.",
//            fontSize = 18.sp,
//            fontWeight = FontWeight.Bold,
//            color = Color.Gray,
//            modifier = Modifier.padding(16.dp)
//        )
//    } else {
//        LazyColumn(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            items(seats) { seat ->
//                SeatItem(seat)
//            }
//        }
//    }
//}


@Composable
fun SeatItem(seat: Seat) {
    val seatColor = if (seat.isBooked) Color.Red else Color.Green

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .background(seatColor, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Seat ${seat.seatNumber}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = if (seat.isBooked) "Booked" else "Available",
            fontSize = 16.sp,
            color = Color.White
        )
    }
}

