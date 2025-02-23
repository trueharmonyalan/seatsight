package com.example.seatsight.UI


import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.isTraceInProgress
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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

import com.example.seatsight.data.hotels
import com.example.seatsight.ui.theme.SeatsightTheme

val buttonColorBook = Color(android.graphics.Color.parseColor("#045F1F"))
val buttonColorBookSelected = Color(android.graphics.Color.parseColor("#41644A"))
val seatList = listOf("S1", "S2", "S3", "S4") // Example seats

@Composable
fun BookSeatScreen(
    modifier: Modifier = Modifier,
) {
    val background = Color(android.graphics.Color.parseColor("#E4E4E4")) // Background color
    val containerColor = Color(android.graphics.Color.parseColor("#D9D9D9")) // Distinct color for container
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

            // **Hotel Name - Placed at the Top & Centered Horizontally**
            Text(
                text = "(fetch) from datastore",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 40.dp),
                textAlign = TextAlign.Center
            )

            // **Rounded Corner Container for Seat Selector & Book Button**
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp)) // Rounded Corners
                    .background(containerColor)
                    .weight(1f), // Makes the container take available space
                color = containerColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center, // Centering inside container
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SeatSelector(selectedSeats) // Seat Selection Inside Rounded Box
                    Spacer(modifier = Modifier.height(16.dp)) // Space Between Seat Selector & Button
                    ButtonForBookAndView(selectedSeats.value) // Book Button Inside Rounded Box
                }
            }
        }
    }
}


@Composable
fun SeatSelector(selectedSeats: MutableState<Set<String>>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // 2 seats per row for better layout
        modifier = Modifier.padding(10.dp)
    ) {
        items(seatList.size) { index ->
            val seat = seatList[index]
            val isSelected = seat in selectedSeats.value

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(60.dp)
                    .border(color = Color.Black, width = 1.dp, shape = RoundedCornerShape(12))
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
                Text(text = seat, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}




@Composable
fun ButtonForBookAndView(selectedSeats: Set<String>, modifier: Modifier = Modifier) {
    val isButtonEnabled = selectedSeats.isNotEmpty() // Enable when at least one seat is selected

    Column {
        Button(
            onClick = {
                Log.d("SeatSelection", "Selected seats: $selectedSeats")
            },
            modifier = Modifier
                .height(40.dp)
                .width(160.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isButtonEnabled) buttonColorBook else Color.Gray
            ),
            enabled = isButtonEnabled // Disable button when no seats are selected
        ) {
            Text(
                text = "Book",
                fontSize = 16.sp
            )
        }
    }
}


@Composable
fun AlertDialogComponent(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("No Seats Selected") },
        text = { Text("Please select at least one seat before booking.") },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}





@Preview
@Composable
fun bookseat(){
    BookSeatScreen()
}

@Preview
@Composable
fun seatlayout() {
    val selectedSeats = remember { mutableStateOf(setOf<String>()) }
    SeatSelector(selectedSeats)
}