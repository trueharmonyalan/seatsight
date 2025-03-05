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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme

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
import androidx.compose.ui.text.style.TextAlign
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
    navController: NavController
) {
    val repository = remember { HotelRepository() }
    val viewModel: HotelViewModel = viewModel(factory = HotelViewModelFactory(repository))

    val seats by viewModel.seatList.collectAsState()

    LaunchedEffect(restaurantId) {
        viewModel.fetchSeats(restaurantId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Seats in $hotelName",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            )

            if (seats.isEmpty()) {
                Text(text = "No seats available", fontSize = 18.sp, color = Color.Gray)
            } else {
                DisplaySeatsForHotel(seats)
            }
        }
    }
}


@Composable
fun DisplaySeatsForHotel(seats: List<Seat>) {
    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxSize()) {
        items(seats.size) { index ->
            val seat = seats[index]
            val seatStatus = if (seat.isBooked) "Booked" else "Available"
            val statusColor = if (seat.isBooked) Color.Red else Color.Green

            Surface(
                modifier = Modifier
                    .padding(8.dp)
                    .size(70.dp),
                shape = MaterialTheme.shapes.medium,
                color = statusColor.copy(alpha = 0.3f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Seat ${seat.seatNumber}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text(text = seatStatus, fontSize = 16.sp, color = statusColor)
                }
            }
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

@Preview
@Composable
fun PreviewViewSeatsScreen() {
    val navController = rememberNavController()
    ViewSeatsScreen(hotelName = "Test Hotel", restaurantId = 1, navController = navController)
}
