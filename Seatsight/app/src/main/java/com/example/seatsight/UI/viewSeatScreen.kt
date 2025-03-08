//package com.example.seatsight.UI
//
//
//import HotelViewModel
//import android.util.Log
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.MaterialTheme
//
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import androidx.navigation.compose.rememberNavController
//import com.example.seatsight.UI.viewmodel.HotelViewModelFactory
//import com.example.seatsight.data.HotelDetails
//import com.example.seatsight.data.model.Seat
//import com.example.seatsight.data.repository.HotelRepository
//
//
//import com.example.seatsight.ui.theme.SeatsightTheme
//@Composable
//fun ViewSeatsScreen(
//    hotelName: String,
//    restaurantId: Int,
//    navController: NavController
//) {
//    val repository = remember { HotelRepository() }
//    val viewModel: HotelViewModel = viewModel(factory = HotelViewModelFactory(repository))
//
//    val seats by viewModel.seatList.collectAsState()
//
//    LaunchedEffect(restaurantId) {
//        viewModel.fetchSeats(restaurantId)
//    }
//
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = surfaceColor
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize().padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "Seats in $hotelName",
//                fontSize = 26.sp,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center,
//                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
//            )
//
//            if (seats.isEmpty()) {
//                Text(text = "No seats available", fontSize = 18.sp, color = Color.Gray)
//            } else {
//                DisplaySeatsForHotel(seats)
//            }
//        }
//    }
//}
//
//
//@Composable
//fun DisplaySeatsForHotel(seats: List<Seat>) {
//    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxSize()) {
//        items(seats.size) { index ->
//            val seat = seats[index]
//            val seatStatus = if (seat.isBooked) "Booked" else "Available"
//            val statusColor = if (seat.isBooked) Color.Red else Color.Green
//
//            Surface(
//                modifier = Modifier
//                    .padding(8.dp)
//                    .size(70.dp),
//                shape = MaterialTheme.shapes.medium,
//                color = statusColor.copy(alpha = 0.3f)
//            ) {
//                Column(
//                    modifier = Modifier.padding(16.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text(text = "Seat ${seat.seatNumber}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
//                    Text(text = seatStatus, fontSize = 16.sp, color = statusColor)
//                }
//            }
//        }
//    }
//}
//
////@Composable
////fun DisplaySeatsForHotel(seats: List<Seat>) {
////    if (seats.isEmpty()) {
////        Text(
////            text = "No seat data available.",
////            fontSize = 18.sp,
////            fontWeight = FontWeight.Bold,
////            color = Color.Gray,
////            modifier = Modifier.padding(16.dp)
////        )
////    } else {
////        LazyColumn(
////            modifier = Modifier.padding(16.dp)
////        ) {
////            items(seats) { seat ->
////                SeatItem(seat)
////            }
////        }
////    }
////}
//
//@Preview
//@Composable
//fun PreviewViewSeatsScreen() {
//    val navController = rememberNavController()
//    ViewSeatsScreen(hotelName = "Test Hotel", restaurantId = 1, navController = navController)
//}

package com.example.seatsight.UI

import HotelViewModel
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.seatsight.UI.viewmodel.HotelViewModelFactory
import com.example.seatsight.data.model.Seat
import com.example.seatsight.data.repository.HotelRepository

@Composable
fun ViewSeatsScreen(
    hotelName: String,
    restaurantId: Int,
    navController: NavController
) {
    // Create repository instance and obtain ViewModel.
    val repository = remember { HotelRepository() }
    val viewModel = viewModel<HotelViewModel>(factory = HotelViewModelFactory(repository))

    // Collect the latest seats state (could be from a real-time detection flow).
    val seats by viewModel.seatList.collectAsState()

    // Log the seat count to verify that we're receiving all seats
    LaunchedEffect(seats) {
        Log.d("ViewSeatsScreen", "Number of seats fetched: ${seats.size}")
    }

    // Fetch seats associated with this restaurant.
    LaunchedEffect(restaurantId) {
        Log.d("ViewSeatsScreen", "Fetching seats for restaurantId: $restaurantId")
        viewModel.fetchSeats(restaurantId)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Real-Time Seats in ${Uri.decode(hotelName)}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
            if (seats.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    // Iterate directly over each seat to ensure all are displayed
                    items(seats) { seat ->
                        SeatStatusCard(seat = seat)
                    }
                }
            }
        }
    }
}

@Composable
fun SeatStatusCard(seat: Seat) {
    // Determine UI color and status text based on the current seat occupancy state.
    val backgroundColor = if (seat.isBooked) Color.Red.copy(alpha = 0.3f) else Color.Green.copy(alpha = 0.3f)
    val statusText = if (seat.isBooked) "Occupied" else "Vacant"

    Surface(
        modifier = Modifier
            .padding(8.dp)
            .size(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "S${seat.seatNumber}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                fontSize = 12.sp,
                color = if (seat.isBooked) Color.Red else Color.Green,
                textAlign = TextAlign.Center
            )
        }
    }
}