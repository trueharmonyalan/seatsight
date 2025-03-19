////package com.example.seatsight.UI
////
////
////import HotelViewModel
////import android.util.Log
////import androidx.compose.foundation.background
////import androidx.compose.foundation.layout.Column
////import androidx.compose.foundation.layout.Row
////import androidx.compose.foundation.layout.Spacer
////
////import androidx.compose.foundation.layout.fillMaxHeight
////import androidx.compose.foundation.layout.fillMaxSize
////import androidx.compose.foundation.layout.fillMaxWidth
////
////import androidx.compose.foundation.layout.padding
////import androidx.compose.foundation.layout.size
////import androidx.compose.foundation.lazy.LazyColumn
////import androidx.compose.foundation.lazy.grid.GridCells
////import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
////import androidx.compose.foundation.lazy.items
////import androidx.compose.foundation.shape.RoundedCornerShape
////import androidx.compose.material3.MaterialTheme
////
////import androidx.compose.material3.Surface
////import androidx.compose.material3.Text
////import androidx.compose.runtime.Composable
////import androidx.compose.runtime.LaunchedEffect
////import androidx.compose.runtime.collectAsState
////import androidx.compose.runtime.getValue
////import androidx.compose.runtime.remember
////import androidx.compose.ui.Alignment
////
////import androidx.compose.ui.Modifier
////import androidx.compose.ui.graphics.Color
////import androidx.compose.ui.text.font.FontWeight
////import androidx.compose.ui.text.style.TextAlign
////import androidx.compose.ui.tooling.preview.Preview
////import androidx.compose.ui.unit.dp
////import androidx.compose.ui.unit.sp
////import androidx.lifecycle.viewmodel.compose.viewModel
////import androidx.navigation.NavController
////import androidx.navigation.compose.rememberNavController
////import com.example.seatsight.UI.viewmodel.HotelViewModelFactory
////import com.example.seatsight.data.HotelDetails
////import com.example.seatsight.data.model.Seat
////import com.example.seatsight.data.repository.HotelRepository
////
////
////import com.example.seatsight.ui.theme.SeatsightTheme
////@Composable
////fun ViewSeatsScreen(
////    hotelName: String,
////    restaurantId: Int,
////    navController: NavController
////) {
////    val repository = remember { HotelRepository() }
////    val viewModel: HotelViewModel = viewModel(factory = HotelViewModelFactory(repository))
////
////    val seats by viewModel.seatList.collectAsState()
////
////    LaunchedEffect(restaurantId) {
////        viewModel.fetchSeats(restaurantId)
////    }
////
////    Surface(
////        modifier = Modifier.fillMaxSize(),
////        color = surfaceColor
////    ) {
////        Column(
////            modifier = Modifier.fillMaxSize().padding(16.dp),
////            horizontalAlignment = Alignment.CenterHorizontally
////        ) {
////            Text(
////                text = "Seats in $hotelName",
////                fontSize = 26.sp,
////                fontWeight = FontWeight.Bold,
////                textAlign = TextAlign.Center,
////                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
////            )
////
////            if (seats.isEmpty()) {
////                Text(text = "No seats available", fontSize = 18.sp, color = Color.Gray)
////            } else {
////                DisplaySeatsForHotel(seats)
////            }
////        }
////    }
////}
////
////
////@Composable
////fun DisplaySeatsForHotel(seats: List<Seat>) {
////    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.fillMaxSize()) {
////        items(seats.size) { index ->
////            val seat = seats[index]
////            val seatStatus = if (seat.isBooked) "Booked" else "Available"
////            val statusColor = if (seat.isBooked) Color.Red else Color.Green
////
////            Surface(
////                modifier = Modifier
////                    .padding(8.dp)
////                    .size(70.dp),
////                shape = MaterialTheme.shapes.medium,
////                color = statusColor.copy(alpha = 0.3f)
////            ) {
////                Column(
////                    modifier = Modifier.padding(16.dp),
////                    horizontalAlignment = Alignment.CenterHorizontally
////                ) {
////                    Text(text = "Seat ${seat.seatNumber}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
////                    Text(text = seatStatus, fontSize = 16.sp, color = statusColor)
////                }
////            }
////        }
////    }
////}
////
//////@Composable
//////fun DisplaySeatsForHotel(seats: List<Seat>) {
//////    if (seats.isEmpty()) {
//////        Text(
//////            text = "No seat data available.",
//////            fontSize = 18.sp,
//////            fontWeight = FontWeight.Bold,
//////            color = Color.Gray,
//////            modifier = Modifier.padding(16.dp)
//////        )
//////    } else {
//////        LazyColumn(
//////            modifier = Modifier.padding(16.dp)
//////        ) {
//////            items(seats) { seat ->
//////                SeatItem(seat)
//////            }
//////        }
//////    }
//////}
////
////@Preview
////@Composable
////fun PreviewViewSeatsScreen() {
////    val navController = rememberNavController()
////    ViewSeatsScreen(hotelName = "Test Hotel", restaurantId = 1, navController = navController)
////}
//
//package com.example.seatsight.UI
//
//import HotelViewModel
//import android.net.Uri
//import android.util.Log
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.grid.GridCells
//import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
//import androidx.compose.foundation.lazy.grid.items
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.*
//import androidx.compose.runtime.collectAsState
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import com.example.seatsight.UI.viewmodel.HotelViewModelFactory
//import com.example.seatsight.data.model.Seat
//import com.example.seatsight.data.repository.HotelRepository
//
//@Composable
//fun ViewSeatsScreen(
//    hotelName: String,
//    restaurantId: Int,
//    navController: NavController
//) {
//    // Create repository instance and obtain ViewModel.
//    val repository = remember { HotelRepository() }
//    val viewModel = viewModel<HotelViewModel>(factory = HotelViewModelFactory(repository))
//
//    // Collect the latest seats state (could be from a real-time detection flow).
//    val seats by viewModel.seatList.collectAsState()
//
//    // Log the seat count to verify that we're receiving all seats
//    LaunchedEffect(seats) {
//        Log.d("ViewSeatsScreen", "Number of seats fetched: ${seats.size}")
//    }
//
//    // Fetch seats associated with this restaurant.
//    LaunchedEffect(restaurantId) {
//        Log.d("ViewSeatsScreen", "Fetching seats for restaurantId: $restaurantId")
//        viewModel.fetchSeats(restaurantId)
//    }
//
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//        color = surfaceColor
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(16.dp)
//        ) {
//            Text(
//                text = "Real-Time Seats in ${Uri.decode(hotelName)}",
//                fontSize = 26.sp,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(bottom = 16.dp)
//            )
//            if (seats.isEmpty()) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            } else {
//                LazyVerticalGrid(
//                    columns = GridCells.Fixed(4),
//                    modifier = Modifier
//                        .fillMaxSize(),
//                    contentPadding = PaddingValues(8.dp)
//                ) {
//                    // Iterate directly over each seat to ensure all are displayed
//                    items(seats) { seat ->
//                        SeatStatusCard(seat = seat)
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun SeatStatusCard(seat: Seat) {
//    // Determine UI color and status text based on the current seat occupancy state.
//    val backgroundColor = if (seat.isBooked) Color.Red.copy(alpha = 0.3f) else Color.Green.copy(alpha = 0.3f)
//    val statusText = if (seat.isBooked) "Occupied" else "Vacant"
//
//    Surface(
//        modifier = Modifier
//            .padding(8.dp)
//            .size(70.dp)
//            .clip(RoundedCornerShape(12.dp))
//            .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
//            .background(backgroundColor)
//    ) {
//        Column(
//            modifier = Modifier
//                .padding(8.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            Text(
//                text = "S${seat.seatNumber}",
//                fontSize = 14.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.Black,
//                textAlign = TextAlign.Center
//            )
//            Spacer(modifier = Modifier.height(4.dp))
//            Text(
//                text = statusText,
//                fontSize = 12.sp,
//                color = if (seat.isBooked) Color.Red else Color.Green,
//                textAlign = TextAlign.Center
//            )
//        }
//    }
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.UI.viewmodel.HotelViewModelFactory
import com.example.seatsight.data.model.RealtimeSeatStatus
import com.example.seatsight.data.model.Seat
import com.example.seatsight.data.network.SseEventSource
import com.example.seatsight.data.repository.HotelRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import org.json.JSONObject
import java.util.UUID

// Define surface color for consistent UI


/**
 * Screen for displaying real-time seat availability using Server-Sent Events.
 *
 * @param hotelName The name of the hotel/restaurant to display
 * @param restaurantId The ID of the restaurant to fetch seats for
 * @param navController Navigation controller for screen navigation
 */
@Composable
fun ViewSeatsScreen(
    hotelName: String,
    restaurantId: Int,
    navController: NavController
) {
    // Create unique identifier for this screen instance to control lifecycle
    val screenInstanceId = remember { UUID.randomUUID().toString() }
    Log.d("ViewSeatsScreen", "Screen created with ID: $screenInstanceId for restaurant: $restaurantId")

    // Create repository instance and obtain ViewModel for regular seats
    val repository = remember { HotelRepository() }
    val viewModel = viewModel<HotelViewModel>(factory = HotelViewModelFactory(repository))

    // State for regular seats from database
    val databaseSeats by viewModel.seatList.collectAsState()

    // State for real-time seat updates
    val realTimeSeats = remember { mutableStateOf<List<RealtimeSeatStatus>>(emptyList()) }

    // Flag to determine if we're using real-time data
    val usingRealTimeData = remember { mutableStateOf(false) }

    // Create SSE event source for real-time updates
    val sseEventSource = remember { SseEventSource() }

    // Display status for debugging
    val connectionStatus = remember { mutableStateOf("Initializing...") }

    // Connection attempt tracking
    val connectionAttempted = remember { mutableStateOf(false) }

    // First, fetch regular seats as a fallback
    LaunchedEffect(restaurantId) {
        Log.d("ViewSeatsScreen", "Fetching regular seats for restaurant: $restaurantId")
        viewModel.fetchSeats(restaurantId)
    }

    // Important: Clean up connections when leaving the screen
    DisposableEffect(screenInstanceId) {
        onDispose {
            Log.d("ViewSeatsScreen", "Screen $screenInstanceId disposed, closing SSE connection")
            try {
                // Explicitly close the connection to prevent resource leaks
                sseEventSource.closeConnection()

                // Reset state flags
                usingRealTimeData.value = false
                connectionAttempted.value = false
            } catch (e: Exception) {
                Log.e("ViewSeatsScreen", "Error during cleanup", e)
            }
        }
    }

    // Establish real-time connection
    LaunchedEffect(screenInstanceId, restaurantId) {
        // Prevent multiple connection attempts
        if (connectionAttempted.value) {
            Log.d("ViewSeatsScreen", "Connection already attempted, skipping")
            return@LaunchedEffect
        }

        connectionAttempted.value = true
        connectionStatus.value = "Connecting to real-time updates..."

        try {
            // First ensure any existing connection is closed
            sseEventSource.closeConnection()

            // Small delay to ensure server processes the previous connection close
            delay(500)

            // Connect to the real-time API
            val serverUrl = "http://192.168.1.11:3003" // ⚠️ Replace with your actual server IP/hostname
            val url = "$serverUrl/api/seats/stream/$restaurantId"

            Log.d("ViewSeatsScreen", "Connecting to SSE endpoint: $url")

            sseEventSource.connect(url)
                .catch { e ->
                    Log.e("ViewSeatsScreen", "Error in SSE connection", e)
                    connectionStatus.value = "Connection error: ${e.message ?: "Unknown error"}"
                    usingRealTimeData.value = false
                }
                .collectLatest { eventData ->
                    try {
                        // Parse the JSON data from the SSE event
                        val jsonObject = JSONObject(eventData)

                        // Check for error messages
                        if (jsonObject.has("error")) {
                            val errorMessage = jsonObject.getString("error")
                            Log.e("ViewSeatsScreen", "Error from server: $errorMessage")
                            connectionStatus.value = "Server error: $errorMessage"
                            return@collectLatest
                        }

                        // Check for heartbeat messages
                        if (jsonObject.has("heartbeat")) {
                            Log.d("ViewSeatsScreen", "Heartbeat received from server")
                            return@collectLatest
                        }

                        // Process seat data
                        if (jsonObject.has("seats")) {
                            val seatsArray = jsonObject.getJSONArray("seats")
                            val updatedSeats = mutableListOf<RealtimeSeatStatus>()

                            for (i in 0 until seatsArray.length()) {
                                val seatObject = seatsArray.getJSONObject(i)

                                // Extract seat properties
                                val seatId = seatObject.getInt("id")
                                val seatNumber = seatObject.getInt("seatNumber")
                                val status = seatObject.getString("status")
                                val isBooked = seatObject.optBoolean("isBooked", false)
                                val posX = seatObject.optInt("posX", 0)
                                val posY = seatObject.optInt("posY", 0)

                                // Create the RealtimeSeatStatus object
                                val seatStatus = RealtimeSeatStatus(
                                    id = seatId,
                                    seatNumber = seatNumber,
                                    status = status,
                                    isBooked = isBooked,
                                    posX = posX,
                                    posY = posY
                                )

                                updatedSeats.add(seatStatus)
                            }

                            if (updatedSeats.isNotEmpty()) {
                                Log.d("ViewSeatsScreen", "Received update with ${updatedSeats.size} seats")
                                realTimeSeats.value = updatedSeats
                                usingRealTimeData.value = true
                                connectionStatus.value = "Connected to real-time updates"
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ViewSeatsScreen", "Error parsing SSE data", e)
                        connectionStatus.value = "Error processing data: ${e.message}"
                    }
                }
        } catch (e: Exception) {
            Log.e("ViewSeatsScreen", "Failed to connect to real-time updates", e)
            connectionStatus.value = "Could not connect to real-time updates: ${e.message}"
            usingRealTimeData.value = false
        }
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

            // Show real-time indicator when connected
            if (usingRealTimeData.value) {
                Text(
                    text = "LIVE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }

            // Display seats based on data source (real-time or database)
            if (usingRealTimeData.value) {
                // Real-time seats display
                if (realTimeSeats.value.isEmpty()) {
                    // Show loading indicator if no real-time data yet
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = connectionStatus.value,
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Display real-time seats

                    // Status summary for real-time seats
                    SeatStatusSummary(realTimeSeats.value)

                    // Seat grid for real-time seats
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(realTimeSeats.value) { seatStatus ->
                            RealtimeSeatStatusCard(seatStatus = seatStatus)
                        }
                    }
                }
            } else {
                // Database seats display
                if (databaseSeats.isEmpty()) {
                    // Show loading indicator if no database data yet
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading seats from database...",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Status summary for database seats
                    SeatStatusSummary(databaseSeats)

                    // Seat grid for database seats
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(databaseSeats) { seat ->
                            SeatStatusCard(seat = seat)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shows a summary of seat availability status
 */
@Composable
fun <T> SeatStatusSummary(seats: List<T>) where T : Any {
    // Calculate counts based on type
    val (occupiedCount, availableCount) = when {
        seats.isEmpty() -> Pair(0, 0)
        seats.first() is RealtimeSeatStatus -> {
            val occupied = (seats as List<RealtimeSeatStatus>).count { it.isOccupied() }
            Pair(occupied, seats.size - occupied)
        }
        seats.first() is Seat -> {
            val occupied = (seats as List<Seat>).count { it.isBooked }
            Pair(occupied, seats.size - occupied)
        }
        else -> Pair(0, 0)
    }

    // Display the summary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Available indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(end = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Green.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Available: $availableCount",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }

        // Occupied indicator
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(Color.Red.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Occupied: $occupiedCount",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Card to display a regular Seat from the database
 */
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

/**
 * Card to display a RealtimeSeatStatus from the SSE API
 */
@Composable
fun RealtimeSeatStatusCard(seatStatus: RealtimeSeatStatus) {
    // Use the helper functions from RealtimeSeatStatus
    val isOccupied = seatStatus.isOccupied()
    val backgroundColor = if (isOccupied) Color.Red.copy(alpha = 0.3f) else Color.Green.copy(alpha = 0.3f)
    val statusText = seatStatus.getStatusText()

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
                text = "S${seatStatus.seatNumber}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = statusText,
                fontSize = 12.sp,
                color = if (isOccupied) Color.Red else Color.Green,
                textAlign = TextAlign.Center
            )
        }
    }
}