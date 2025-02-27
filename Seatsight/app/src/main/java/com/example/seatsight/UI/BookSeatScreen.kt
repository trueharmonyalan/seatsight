package com.example.seatsight.UI

import HotelViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.UI.viewmodel.HotelViewModelFactory
import com.example.seatsight.bookingConfirmation
import com.example.seatsight.data.model.MenuItem
import com.example.seatsight.data.model.Seat
import com.example.seatsight.data.repository.HotelRepository
import androidx.compose.foundation.lazy.items


val buttonColorBook = Color(android.graphics.Color.parseColor("#045F1F"))
val buttonColorBookSelected = Color(android.graphics.Color.parseColor("#41644A"))
val seatList = listOf("S1", "S2", "S3", "S4") // More seats for better UI
@Composable
fun BookSeatScreen(
    hotelName: String,
    restaurantId: Int,
    navController: NavController
) {
    val background = Color(android.graphics.Color.parseColor("#D9D9D9"))
    val containerColor = Color(android.graphics.Color.parseColor("#FFFFFF"))
    val selectedSeats = remember { mutableStateOf(setOf<Int>()) }
    val repository = remember { HotelRepository() }
    val viewModel: HotelViewModel = viewModel(factory = HotelViewModelFactory(repository))
    val seatList by viewModel.seatList.collectAsState()
    val menuList by viewModel.menuList.collectAsState()
    val selectedItems = remember { mutableStateMapOf<MenuItem, Int>() }

    LaunchedEffect(restaurantId) {
        Log.d("BookSeatScreen", "Fetching seats for restaurantId: $restaurantId")
        viewModel.fetchSeats(restaurantId)
        viewModel.fetchMenu(restaurantId)
    }

    Surface(
        modifier = Modifier.fillMaxSize().background(background),
        color = background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // **Fixed Hotel Name**
            Text(
                text = hotelName,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // **Make this part scrollable**
            LazyColumn(
                modifier = Modifier.weight(1f) // ✅ Allows scrolling while keeping Confirm Button fixed
            ) {
                item {
                    // **Seat Selection Container**
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
                            .background(containerColor)
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Select Your Seats",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (seatList.isEmpty()) {
                                Text("No seats available", fontSize = 16.sp, color = Color.Gray)
                            } else {
                                SeatSelector(seatList, selectedSeats)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // **Menu Selection Container**
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color.Gray, RoundedCornerShape(16.dp))
                            .background(containerColor) // ✅ Same color as seat selector
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Select Menu Items",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (menuList.isEmpty()) {
                                Text("No menu available", fontSize = 16.sp, color = Color.Gray)
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxHeight() // ✅ Scroll inside container
                                ) {
                                    items(menuList) { menuItem ->
                                        MenuItemCard(menuItem, selectedItems)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // **Fixed Confirm Button**
            ButtonForBookAndView(
                selectedSeats = selectedSeats.value,
                selectedItems = selectedItems,
                navController = navController,
                hotelName = hotelName
            )
        }
    }
}




// **Seat Selection Grid**
@Composable
fun SeatSelector(
    seatList: List<Seat>,
    selectedSeats: MutableState<Set<Int>>
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4), // ✅ 4x4 Matrix
        modifier = Modifier
            .height(250.dp)
            .fillMaxWidth()
    ) {
        items(seatList.size) { index ->
            val seat = seatList[index]
            val isSelected = selectedSeats.value.contains(seat.seatNumber)
            val isBooked = seat.isBooked

            Log.d("SeatSelection", "Seat ${seat.seatNumber}: isSelected = $isSelected")

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .size(70.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(color = Color.DarkGray, width = 1.dp, shape = RoundedCornerShape(12.dp))
                    .background(
                        color = when {
                            isBooked -> Color.Red
                            isSelected -> buttonColorBookSelected
                            else -> Color.LightGray
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                    .toggleable(
                        value = isSelected,
                        onValueChange = { selected ->
                            if (!isBooked) {
                                selectedSeats.value = if (selected) {
                                    selectedSeats.value + seat.seatNumber
                                } else {
                                    selectedSeats.value - seat.seatNumber
                                }
                            }
                        },
                        role = Role.Button
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = seat.seatNumber.toString(), // ✅ Display correct seat number
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun MenuSelector(
    menuList: List<MenuItem>,
    selectedItems: MutableMap<MenuItem, Int>
) {
    LazyColumn(
        modifier = Modifier.padding(10.dp)
    ) {
        items(menuList) { menuItem ->
            MenuItemCard(menuItem, selectedItems)
        }
    }
}

// **Menu Item UI**
@Composable
fun MenuItemCard(
    menuItem: MenuItem,
    selectedItems: MutableMap<MenuItem, Int>
) {
    val quantity = selectedItems[menuItem] ?: 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.LightGray
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = menuItem.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "₹${menuItem.price}",
                    fontSize = 16.sp
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { if (quantity > 0) selectedItems[menuItem] = quantity - 1 }) {
                    Text(text = "-", fontSize = 24.sp)
                }
                Text(text = quantity.toString(), fontSize = 18.sp)
                IconButton(onClick = { selectedItems[menuItem] = quantity + 1 }) {
                    Text(text = "+", fontSize = 24.sp)
                }
            }
        }
    }
}








// **Book Button**
@Composable
fun ButtonForBookAndView(
    selectedSeats: Set<Int>,
    selectedItems: MutableMap<MenuItem, Int>,
    navController: NavController,
    hotelName: String
) {
    val isButtonEnabled = selectedSeats.isNotEmpty() || selectedItems.isNotEmpty()

    Button(
        onClick = {
            val seatListString = selectedSeats.joinToString(",")
            val menuListString = selectedItems.entries.joinToString(",") { "${it.key.name} x${it.value}" }

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
    BookSeatScreen(hotelName = "Hotel Ettumanoor", navController = navController, restaurantId = 1)
}
