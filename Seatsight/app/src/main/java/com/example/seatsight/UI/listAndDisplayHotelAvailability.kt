package com.example.seatsight.UI

import HotelViewModel
import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.seatsight.data.HotelDetails
import com.example.seatsight.data.model.HotelResponse

//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//hotel's name and description is maintained here
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
@Composable
fun listofAvailableHotels(
    hotel: HotelDetails,
    onHotelSelected: (String, Int) -> Unit
) {
    val buttonColor = Color(android.graphics.Color.parseColor("#BB0000"))
    val textColor = Color(android.graphics.Color.parseColor("#EFEFEF"))
    val containerColor = Color(android.graphics.Color.parseColor("#F0EBEB"))

    Surface(
        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .background(containerColor)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(0.7f)
                ) {
                    Text(
                        text = hotel.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = hotel.description,
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = {
                        Log.d("listofAvailableHotels", "Clicked hotel: ${hotel.name}, ID: ${hotel.restaurantId}") // ✅ Debugging log
                        onHotelSelected(hotel.name, hotel.restaurantId) // ✅ Ensure `restaurantId` is passed
                    },
                    colors = ButtonDefaults.buttonColors(buttonColor)
                ) {
                    Text(text = "Book Seat", color = textColor)
                }
            }
        }
    }
}





//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//this composable is responsible for the display of scrollable hotel details with each hotel details
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
@Composable
fun DisplaylistofAvailableHotels(
    hotelDetail: List<HotelDetails>,
    onHotelSelected: (String, Int) -> Unit // ✅ Pass restaurantId too
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 0.dp)
    ) {
        items(items = hotelDetail) { hotel ->
            listofAvailableHotels(hotel = hotel, onHotelSelected = onHotelSelected)
        }
    }
}



@Composable
fun listMenuForHome(
    modifier: Modifier = Modifier,
    hotel: HotelResponse,
) {
    val menuButtonColor = Color(android.graphics.Color.parseColor("#BB0000"))
    val menuTextColor = Color(android.graphics.Color.parseColor("#EFEFEF"))
    val menuContainerColor = Color(android.graphics.Color.parseColor("#F0EBEB"))

    var expandMenuButton by remember { mutableStateOf(false) }
    val expandMenuPadding by animateDpAsState(targetValue = if (expandMenuButton) 50.dp else 0.dp)

    Surface(
        modifier = modifier.padding(vertical = 10.dp, horizontal = 10.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .background(menuContainerColor)
                .padding(bottom = expandMenuPadding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text(
                        text = hotel.hotel_name, // ✅ Correct property name
                        modifier = Modifier
                            .padding(vertical = 5.dp, horizontal = 0.dp)
                            .padding(start = 8.dp),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Button(
                    onClick = { expandMenuButton = !expandMenuButton },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(menuButtonColor)
                ) {
                    Text(
                        text = if (expandMenuButton) "Close Menu" else "Menu",
                        color = menuTextColor
                    )
                }
            }

            if (expandMenuButton) {
                Column(modifier = Modifier.padding(start = 8.dp, top = 8.dp)) {
                    hotel.menu.forEach { menuItem ->
                        Text(
                            text = "• ${menuItem.name} - ₹${menuItem.price}",
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}



//@Composable
//fun displayListMenuForHome(
//    modifier: Modifier = Modifier,
//    hotelDetail: List<HotelDetails>
//) {
//    LazyColumn(
//        modifier = modifier.padding(horizontal = 0.dp)
//    ) {
//        items(items = hotelDetail) { hotel ->
//            listMenuForHome(hotel = hotel) // Uses the version without seat booking
//        }
//    }
//}
@Composable
fun displayListMenuForHome(
    modifier: Modifier = Modifier,
    hotelDetail: List<HotelResponse> // ✅ Properly receives hotel list
) {
    LazyColumn(
        modifier = Modifier.padding(horizontal = 0.dp)
    ) {
        items(hotelDetail) { hotel ->
            listMenuForHome(hotel = hotel)
        }
    }
}

// for view seats button
//@Composable
//fun listofHotelsForViewing(
//    hotel: HotelDetails,
//    onHotelSelected: (String, Int) -> Unit
//) {
//    val buttonColor = Color(android.graphics.Color.parseColor("#005BBB")) // Different color for view seats
//    val textColor = Color(android.graphics.Color.parseColor("#EFEFEF"))
//    val containerColor = Color(android.graphics.Color.parseColor("#F0EBEB"))
//
//    Surface(
//        modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
//        shape = RoundedCornerShape(12.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .background(containerColor)
//                .padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(80.dp),
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Column(
//                    modifier = Modifier.weight(0.7f)
//                ) {
//                    Text(
//                        text = hotel.name,
//                        fontSize = 20.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Text(
//                        text = hotel.description,
//                        fontSize = 16.sp
//                    )
//                }
//
//                Button(
//                    onClick = {
//                        Log.d("listofHotelsForViewing", "Viewing seats for hotel: ${hotel.name}, ID: ${hotel.restaurantId}")
//                        onHotelSelected(hotel.name, hotel.restaurantId) // Navigate to view seats
//                    },
//                    colors = ButtonDefaults.buttonColors(buttonColor)
//                ) {
//                    Text(text = "View Seats", color = textColor)
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun DisplaylistofHotelsForViewing(
//    hotelDetail: List<HotelDetails>,
//    onHotelSelected: (String, Int) -> Unit
//) {
//    LazyColumn(
//        modifier = Modifier.padding(horizontal = 0.dp)
//    ) {
//        items(items = hotelDetail) { hotel ->
//            listofHotelsForViewing(hotel = hotel, onHotelSelected = onHotelSelected)
//        }
//    }
//}
@Composable
fun ListOfHotelsForViewing(
    hotel: HotelDetails,
    onHotelSelected: (String, Int) -> Unit
) {
    // Define colors
    val buttonColor = Color(0xFF005BBB)
    val textColor = Color(0xFFEFEFEF)
    val containerColor = Color(0xFFF0EBEB)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .background(containerColor)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(0.7f)
                ) {
                    Text(
                        text = hotel.name,
                        fontSize = 20.sp
                    )
                    Text(
                        text = hotel.description,
                        fontSize = 16.sp
                    )
                }
                Button(
                    onClick = { onHotelSelected(hotel.name, hotel.restaurantId) },
                    colors = ButtonDefaults.buttonColors(buttonColor)
                ) {
                    Text(text = "View Seats", color = textColor)
                }
            }
        }
    }
}

@Composable
fun DisplaylistofHotelsForViewing(
    hotelDetail: List<HotelDetails>,
    onHotelSelected: (String, Int) -> Unit
) {
    LazyColumn {
        items(hotelDetail) { hotel ->
            ListOfHotelsForViewing(hotel = hotel, onHotelSelected = onHotelSelected)
        }
    }
}