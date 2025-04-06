
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.UI.viewmodel.HotelViewModelFactory
import com.example.seatsight.data.model.RealtimeSeatStatus
import com.example.seatsight.data.model.Seat
import com.example.seatsight.data.network.SseEventSource
import com.example.seatsight.data.repository.HotelRepository
import com.example.seatsight.ui.viewmodel.RealtimeSeatViewModel
import com.example.seatsight.ui.viewmodel.RealtimeSeatViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

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
    val hotelViewModel = viewModel<HotelViewModel>(factory = HotelViewModelFactory(repository))

    // Create and obtain the RealtimeSeatViewModel for real-time updates
    val realtimeSeatViewModel = viewModel<RealtimeSeatViewModel>(factory = RealtimeSeatViewModelFactory())

    // State for regular seats from database
    val databaseSeats by hotelViewModel.seatList.collectAsState()

    // State for real-time seat updates
    val realTimeSeats by realtimeSeatViewModel.seatStatusList.collectAsState()

    // Loading state
    val isLoading by realtimeSeatViewModel.isLoading.collectAsState()

    // Error state
    val error by realtimeSeatViewModel.error.collectAsState()

    // Connection status
    val connectionStatus by realtimeSeatViewModel.connectionStatus.collectAsState()

    // Paused state
    val isPaused by realtimeSeatViewModel.isPaused.collectAsState()

    // Flag to determine if we're using real-time data
    val usingRealTimeData = realTimeSeats.isNotEmpty()

    // First, fetch regular seats as a fallback
    LaunchedEffect(restaurantId) {
        Log.d("ViewSeatsScreen", "Fetching regular seats for restaurant: $restaurantId")
        hotelViewModel.fetchSeats(restaurantId)
    }

    // Start tracking when the screen is displayed
    // Check if we should resume or start fresh
    LaunchedEffect(screenInstanceId, restaurantId) {
        Log.d("ViewSeatsScreen", "Checking whether to start or resume tracking for restaurant: $restaurantId")

        if (isPaused) {
            Log.d("ViewSeatsScreen", "Resuming tracking for restaurant: $restaurantId")
            realtimeSeatViewModel.resumeTracking(restaurantId)
        } else {
            Log.d("ViewSeatsScreen", "Starting fresh tracking for restaurant: $restaurantId")
            realtimeSeatViewModel.startTracking(restaurantId)
        }
    }

    // Critical change: Instead of stopping tracking, we pause it when navigating away
    DisposableEffect(screenInstanceId) {
        onDispose {
            Log.d("ViewSeatsScreen", "Screen $screenInstanceId disposed, pausing tracking instead of stopping")

            // Use a custom suspend function to safely pause tracking
            // This approach avoids cancellation issues during navigation
            val restaurantToPause = restaurantId
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                kotlinx.coroutines.MainScope().launch {
                    try {
                        // IMPORTANT: We pause instead of stopping the tracking
                        realtimeSeatViewModel.pauseTracking(restaurantToPause)
                        Log.d("ViewSeatsScreen", "Successfully requested pause for tracking: $restaurantToPause")
                    } catch (e: Exception) {
                        Log.e("ViewSeatsScreen", "Error pausing tracking", e)
                    }
                }
            }
        }
    }

    // Listen for lifecycle events - important for app-level pause/resume
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    // User is navigating away or app going to background
                    Log.d("ViewSeatsScreen", "Lifecycle ON_PAUSE - pausing tracking")
                    realtimeSeatViewModel.pauseTracking(restaurantId)
                }
                Lifecycle.Event.ON_RESUME -> {
                    // User navigated back to the app from background
                    if (isPaused) {
                        Log.d("ViewSeatsScreen", "Lifecycle ON_RESUME - resuming tracking")
                        realtimeSeatViewModel.resumeTracking(restaurantId)
                    }
                }
                Lifecycle.Event.ON_DESTROY -> {
                    // Final cleanup - this is important for fully stopping tracking
                    // when the ViewModel is about to be destroyed (app exit or process death)
                    Log.d("ViewSeatsScreen", "Lifecycle ON_DESTROY - stopping tracking")
                    realtimeSeatViewModel.stopTracking(restaurantId)
                }
                else -> { /* Ignore other events */ }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Rest of your UI code remains the same...
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
            if (usingRealTimeData) {
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

            // Display loading state
            if (isLoading) {
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
                            text = connectionStatus,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        // Display error if any
                        error?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = it,
                                fontSize = 14.sp,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                // Display seats based on data source (real-time or database)
                if (usingRealTimeData) {
                    // Status summary for real-time seats
                    SeatStatusSummary(realTimeSeats)

                    // Seat grid for real-time seats
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(realTimeSeats) { seatStatus ->
                            RealtimeSeatStatusCard(seatStatus = seatStatus)
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
}


@Composable
fun <T> SeatStatusSummary(seats: List<T>) where T : Any {
    // Calculate counts based on type
    val (occupiedCount, reservedCount, availableCount) = when {
        seats.isEmpty() -> Triple(0, 0, 0)
        seats.first() is RealtimeSeatStatus -> {
            val seatsList = seats as List<RealtimeSeatStatus>
            val occupied = seatsList.count { it.isOccupied() }
            val reserved = seatsList.count { it.isReserved() }
            Triple(occupied, reserved, seats.size - occupied - reserved)
        }
        seats.first() is Seat -> {
            val seatsList = seats as List<Seat>
            val reserved = seatsList.count { it.isBooked }
            Triple(0, reserved, seats.size - reserved)
        }
        else -> Triple(0, 0, 0)
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
        StatusIndicator(color = Color.Green.copy(alpha = 0.7f), text = "Available: $availableCount")

        // Reserved indicator
        StatusIndicator(color = Color.Blue.copy(alpha = 0.7f), text = "Reserved: $reservedCount")

        // Occupied indicator
        StatusIndicator(color = Color.Red.copy(alpha = 0.7f), text = "Occupied: $occupiedCount")
    }
}

@Composable
private fun StatusIndicator(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun SeatStatusCard(seat: Seat) {
    // Determine UI color and status text based on the current seat occupancy state.
    val backgroundColor = if (seat.isBooked) Color.Blue.copy(alpha = 0.3f) else Color.Green.copy(alpha = 0.3f)
    val statusText = if (seat.isBooked) "Reserved" else "Vacant"
    val textColor = if (seat.isBooked) Color.Blue else Color.Green

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
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}


@Composable
fun RealtimeSeatStatusCard(seatStatus: RealtimeSeatStatus) {
    // Use the helper functions from RealtimeSeatStatus
    val isOccupied = seatStatus.isOccupied()
    val isReserved = seatStatus.isReserved()

    // Determine background color based on status
    val backgroundColor = when {
        isOccupied -> Color.Red.copy(alpha = 0.3f)
        isReserved -> Color.Blue.copy(alpha = 0.3f)
        else -> Color.Green.copy(alpha = 0.3f)
    }

    val statusText = seatStatus.getStatusText()
    // Define text color directly here instead of using the method
    val textColor = when {
        isOccupied -> Color.Red
        isReserved -> Color.Blue
        else -> Color.Green
    }

    Surface(
        modifier = Modifier
            .padding(8.dp)
            .size(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(12.dp))
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
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
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}