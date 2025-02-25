package com.example.seatsight.UI

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.bookingConfirmation

val buttonColorBook = Color(android.graphics.Color.parseColor("#045F1F"))
val buttonColorBookSelected = Color(android.graphics.Color.parseColor("#41644A"))
val seatList = listOf("S1", "S2", "S3", "S4") // More seats for better UI

@Composable
fun BookSeatScreen(
    hotelName: String,
    navController: NavController
) {
    val background = Color(android.graphics.Color.parseColor("#D9D9D9")) // Light background
    val containerColor = Color(android.graphics.Color.parseColor("#FFFFFF")) // White container
    val selectedSeats = remember { mutableStateOf(setOf<String>()) } // Track selected seats

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        color = background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // **Hotel Name - Title**
            Text(
                text = hotelName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            // **Seat Selection UI**
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp)) // Rounded Corners
                    .border(1.dp, Color.Gray, RoundedCornerShape(16.dp)) // Border for better UI
                    .background(containerColor),
                color = containerColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Your Seats",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    SeatSelector(selectedSeats) // Seat Selection Grid
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // Space before button

            // **Book Button**
            ButtonForBookAndView(
                selectedSeats = selectedSeats.value,
                navController = navController,
                hotelName = hotelName
            )
        }
    }
}

// **Seat Selection Grid**
@Composable
fun SeatSelector(selectedSeats: MutableState<Set<String>>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3), // 3 seats per row for better layout
        modifier = Modifier.padding(10.dp)
    ) {
        items(seatList.size) { index ->
            val seat = seatList[index]
            val isSelected = seat in selectedSeats.value

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(color = Color.DarkGray, width = 1.dp, shape = RoundedCornerShape(12.dp))
                    .background(
                        color = if (isSelected) buttonColorBookSelected else Color.LightGray,
                        shape = RoundedCornerShape(10.dp),
                    )
                    .toggleable(
                        value = isSelected,
                        onValueChange = { selected ->
                            selectedSeats.value = if (selected) {
                                selectedSeats.value + seat
                            } else {
                                selectedSeats.value - seat
                            }
                        },
                        role = Role.Button
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = seat,
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// **Book Button**
@Composable
fun ButtonForBookAndView(
    selectedSeats: Set<String>,
    navController: NavController,
    hotelName: String
) {
    val isButtonEnabled = selectedSeats.isNotEmpty()

    Button(
        onClick = {
            val seatListString = selectedSeats.joinToString(",")
            val formattedRoute = bookingConfirmation.route
                .replace("{hotelName}", hotelName)
                .replace("{selectedSeats}", seatListString)

            navController.navigate(formattedRoute)
        },
        modifier = Modifier
            .height(50.dp)
            .width(200.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isButtonEnabled) buttonColorBook else Color.Gray
        ),
        enabled = isButtonEnabled
    ) {
        Text(
            text = "Confirm Booking",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// **Preview for Testing**
@Preview
@Composable
fun PreviewBookSeatScreen() {
    val navController = rememberNavController()
    BookSeatScreen(hotelName = "Hotel Ettumanoor", navController = navController)
}
