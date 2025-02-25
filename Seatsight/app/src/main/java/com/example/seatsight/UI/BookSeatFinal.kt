
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
import com.example.seatsight.UI.buttonColorBook
import com.example.seatsight.UI.surfaceColor

@Composable
fun BookingConfirmationScreen(
    hotelName: String,
    selectedSeats: Set<String>,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit = {}
) {
    val background = Color(android.graphics.Color.parseColor("#D9D9D9")) // Background color
    val containerColor = surfaceColor // Distinct container color
    val showAlert = remember { mutableStateOf(false) } // State to track alert visibility

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

            // **Header - Confirmation Message**
            Text(
                text = "Confirm Your Booking",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 40.dp),
                textAlign = TextAlign.Center
            )

            // **Container Only Wrapping Content**
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(16.dp)) // Rounded Corners
                    .background(containerColor),
                color = containerColor
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp), // Padding inside container
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Hotel: $hotelName",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Selected Seats: ${selectedSeats.joinToString(", ")}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // **Confirm Booking Button**
                    Button(
                        onClick = { showAlert.value = true },
                        colors =ButtonDefaults.buttonColors(buttonColorBook),
                        modifier = Modifier
                            .height(40.dp)
                            .width(160.dp)
                    ) {
                        Text(text = "Confirm", fontSize = 16.sp)
                    }
                }
            }

            // **Alert Dialog for Confirmation**
            if (showAlert.value) {
                AlertDialogComponent(
                    message = "Your seats have been confirmed. The amount will be added to your bill after dining.",
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
    BookingConfirmationScreen(selectedSeats = setOf("S1", "S3"), hotelName = "pp") // Mock selected seats
}
