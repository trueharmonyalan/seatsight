
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.seatsight.UI.buttonColorBook
import com.example.seatsight.UI.surfaceColor
import com.example.seatsight.bookingConfirmation

@Composable
fun BookingConfirmationScreen(
    hotelName: String,
    selectedSeats: Set<String>,
    selectedMenuItems: Map<String, Int>, // ✅ Include menu selections
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit = {}
) {
    val background = Color(android.graphics.Color.parseColor("#D9D9D9"))
    val containerColor = surfaceColor
    val showAlert = remember { mutableStateOf(false) }

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
                    .padding(top = 20.dp, bottom = 40.dp),
                textAlign = TextAlign.Center
            )

            // **Confirmation Details**
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(containerColor),
                color = containerColor
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hotel: $hotelName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // **Display Selected Seats**
                    Text(
                        text = "Selected Seats: ${selectedSeats.joinToString(", ")}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // **Display Selected Menu Items**
                    if (selectedMenuItems.isNotEmpty()) {
                        Text(
                            text = "Selected Menu Items:",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        selectedMenuItems.forEach { (item, quantity) ->
                            Text(
                                text = "$item x$quantity",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    } else {
                        Text(
                            text = "No menu items selected",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = androidx.compose.ui.graphics.Color.Gray
                        )
                    }

                    // **Confirm Button**
                    Button(
                        onClick = { showAlert.value = true },
                        colors = ButtonDefaults.buttonColors(buttonColorBook),
                        modifier = Modifier
                            .height(40.dp)
                            .width(160.dp)
                    ) {
                        Text(text = "Confirm", fontSize = 16.sp)
                    }
                }
            }

            // **Alert Dialog**
            if (showAlert.value) {
                AlertDialogComponent(
                    message = "Your booking is confirmed. The amount will be added to your bill after dining.",
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
fun ButtonForBookAndView(
    selectedSeats: Set<String>,
    selectedItems: Map<String, Int>,
    navController: NavController,
    hotelName: String
) {
    val isButtonEnabled = selectedSeats.isNotEmpty() || selectedItems.isNotEmpty()

    Button(
        onClick = {
            val seatListString = selectedSeats.joinToString(",")
            val menuListString = selectedItems.entries.joinToString(",") { "${it.key} x${it.value}" }

            val formattedRoute = bookingConfirmation.route
                .replace("{hotelName}", hotelName)
                .replace("{selectedSeats}", seatListString)
                .replace("{selectedMenu}", menuListString)

            navController.navigate(formattedRoute)
        },
        modifier = Modifier
            .height(50.dp)
            .width(200.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isButtonEnabled) buttonColorBook else androidx.compose.ui.graphics.Color.Gray
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
        selectedMenuItems = mapOf("Pasta" to 2, "Pizza" to 1)
    )
}