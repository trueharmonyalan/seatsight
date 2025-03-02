package com.example.seatsight.UI


import HotelViewModel
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.layout.padding

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.UI.viewmodel.HotelViewModelFactory
import com.example.seatsight.data.HotelDetails
import com.example.seatsight.data.repository.HotelRepository


import com.example.seatsight.ui.theme.SeatsightTheme

@Composable
fun ViewSeatsWindow(
    navController: NavController, // Ensure NavController is passed
    modifier: Modifier = Modifier
) {
    val repository = remember { HotelRepository() } // ✅ Create repository instance
    val viewModel: HotelViewModel = viewModel(factory = HotelViewModelFactory(repository)) // ✅ Use ViewModel Factory

    val hotels by viewModel.hotelList.collectAsState() // ✅ Fetch dynamic hotel list

    LaunchedEffect(true) {
        viewModel.fetchHotels() // ✅ Fetch real hotels
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = surfaceColor
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 1.dp, horizontal = 10.dp)
                .background(surfaceColor)
        ) {
            Surface(
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(surfaceColor)
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {
                    Surface {
                        Column(
                            modifier = Modifier.background(surfaceColor)
                        ) {
                            Text(
                                text = "Available Hotels",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(bottom = 18.dp)
                                    .padding(start = 10.dp)
                            )

                            // ✅ Pass real hotels from API for Viewing Seats
                            DisplaylistofHotelsForViewing(
                                hotelDetail = hotels.map { apiHotel ->
                                    Log.d("ViewSeatsWindow", "Mapping hotel: ${apiHotel.hotel_name}, ID: ${apiHotel.restaurant_id}") // ✅ Debug log

                                    HotelDetails(
                                        name = apiHotel.hotel_name,
                                        description = "Description Not Available",
                                        menuItems = emptyList(),
                                        restaurantId = apiHotel.restaurant_id // ✅ Ensure restaurantId is mapped
                                    )
                                },
                                onHotelSelected = { hotelName, restaurantId ->
                                    Log.d("ViewSeatsWindow", "Navigating to ViewSeatsScreen with ID: $restaurantId") // ✅ Debug log
                                    val formattedRoute = "viewSeatScreen/${hotelName}/${restaurantId}" // ✅ Ensure proper string formatting

                                    Log.d("Navigation", "Navigating to: $formattedRoute") // ✅ Debug log
                                    navController.navigate(formattedRoute)
                                }
                            )

                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun viewSeatWindow(){
    SeatsightTheme {
        val navController = rememberNavController()
        ViewSeatsWindow(navController = navController)
    }
}
