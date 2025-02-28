
import android.app.TimePickerDialog
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.seatsight.UI.buttonColorBook
import com.example.seatsight.UI.surfaceColor
import com.example.seatsight.bookingConfirmation
import com.example.seatsight.data.model.MenuItem
import java.util.Calendar
@Composable
fun BookingConfirmationScreen(
    hotelName: String,
    selectedSeats: Set<String>,
    selectedMenu: Map<String, Int>,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit = {}
) {
    val background = Color(android.graphics.Color.parseColor("#D9D9D9"))
    val containerColor = surfaceColor
    val showAlert = remember { mutableStateOf(false) }

    // ✅ Remember state for time selection
    val startTime = remember { mutableStateOf("Select Start Time") }
    val endTime = remember { mutableStateOf("Select End Time") }

    Log.d("BookingConfirmation", "Hotel: $hotelName")
    Log.d("BookingConfirmation", "Selected Seats: ${selectedSeats.joinToString(", ")}")
    Log.d("BookingConfirmation", "Selected Menu: $selectedMenu")

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
            // **Header**
            Text(
                text = "Confirm Your Booking",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 20.dp),
                textAlign = TextAlign.Center
            )

            // **Booking Details Container (Same Style for All Sections)**
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(containerColor),
                color = containerColor
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // **Hotel Name**
                    Text(
                        text = "Hotel: $hotelName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // **Selected Seats**
                    BookingInfoSection(title = "Selected Seats") {
                        if (selectedSeats.isEmpty()) {
                            Text("No seats selected", fontSize = 16.sp, color = androidx.compose.ui.graphics.Color.Gray)
                        } else {
                            Text(selectedSeats.joinToString(", "), fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // **Ordered Items**
                    BookingInfoSection(title = "Ordered Items") {
                        val filteredMenu = selectedMenu.filterValues { it > 0 }
                        if (filteredMenu.isEmpty()) {
                            Text("Nothing is selected to order", fontSize = 16.sp, color = androidx.compose.ui.graphics.Color.Gray)
                        } else {
                            Column {
                                filteredMenu.forEach { (menuItem, quantity) ->
                                    Text("$menuItem x$quantity", fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // **Booking Time Selection**
                    BookingInfoSection(title = "Booking Time") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            TimePickerButton(label = startTime.value) {
                                startTime.value = it
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            TimePickerButton(label = endTime.value) {
                                endTime.value = it
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // **Confirm Booking Button**
            Button(
                onClick = { showAlert.value = true },
                colors = ButtonDefaults.buttonColors(buttonColorBook),
                modifier = Modifier
                    .height(40.dp)
                    .width(160.dp)
            ) {
                Text(text = "Confirm", fontSize = 16.sp)
            }

            if (showAlert.value) {
                AlertDialogComponent(
                    message = "Your booking is confirmed! The total amount will be added to your bill.",
                    onDismiss = { showAlert.value = false },
                    onConfirm = {
                        showAlert.value = false
                        onConfirm()
                    }
                )
            }
        }
    }
}

@Composable
fun BookingInfoSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}


@Composable
fun TimePickerButton(label: String, onTimeSelected: (String) -> Unit) {
    val context = LocalContext.current
    val timePicker = remember { mutableStateOf(label) }

    Button(
        onClick = {
            val cal = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val formattedTime = String.format("%02d:%02d", hour, minute)
                    timePicker.value = formattedTime
                    onTimeSelected(formattedTime)
                },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                true
            ).show()
        },
        colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    ) {
        Text(text = timePicker.value, color = androidx.compose.ui.graphics.Color.Black, fontSize = 16.sp)
    }
}






// **Reusable Alert Dialog Component**
@Composable
fun AlertDialogComponent(message: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {
            Button(onClick = { onConfirm() }) {
                Text("OK")
            }
        },
        title = { Text(text = "Booking Confirmed") },
        text = { Text(text = message) }
    )
}

// **Preview Composable**
@Preview(showBackground = true)
@Composable
fun PreviewBookingConfirmationScreen() {
    BookingConfirmationScreen(
        hotelName = "Hotel Example",
        selectedSeats = setOf("S1", "S2"),
        selectedMenu = mapOf()
    )
}