package com.example.seatsight.UI

import HotelViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.seatsight.data.repository.HotelRepository


//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//HomeScreen Design.
//here we pass the listMenu composable's required list of hoteldetails from the data class hotelDetails
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
val surfaceColor = Color(android.graphics.Color.parseColor("#E4E4E4"))
//@Composable
// fun homescreen(modifier: Modifier = Modifier,
//             navController: NavController ) {
//
//    val seatAndViewButtonColor = Color(android.graphics.Color.parseColor("#D9D9D9"))
//
//    val buttonTextColor = Color(android.graphics.Color.parseColor("#222222"))
//
//
//
//
//    Surface(
//        modifier = Modifier.fillMaxSize(),
//
//        color = surfaceColor
//
//
//    ) {
//
//
//        Column {
//// book seat button and view seats button
//            Surface(
//                modifier = Modifier
//                    .padding(vertical = 1.dp, horizontal = 10.dp),
//                color = surfaceColor
//
//
//
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 20.dp, horizontal = 0.dp)
//
//
//                ) {
//                    Button(
//                        onClick = {navController.navigate("availableHotelsForBookSeats")},
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(5.dp)
//                            .height(80.dp),
//                        shape = RoundedCornerShape(10.dp),
//                        colors = ButtonDefaults.buttonColors(seatAndViewButtonColor)
//
//                    ) {
//                        Text(
//                            text = "Book seats",
//                            color = buttonTextColor,
//                            fontSize = 20.sp
//                        )
//
//                    }
//
//                    Button(
//                        onClick = { navController.navigate("viewSeatAvailableSeatList") },
//                        modifier = Modifier
//                            .weight(1f)
//                            .padding(5.dp)
//                            .height(80.dp),
//                        shape = RoundedCornerShape(10.dp),
//                        colors = ButtonDefaults.buttonColors(seatAndViewButtonColor)
//                    ) {
//                        Text(text = "View seats",
//                            color = buttonTextColor,
//                            fontSize = 20.sp
//                        )
//                    }
//                }
//            }
//
//
//// available hotels options
//            Surface(
//                modifier = Modifier
//                    .padding(vertical = 0.dp, horizontal = 10.dp)
//                    .padding(bottom = 24.dp),
//                shape = RoundedCornerShape(5.dp),
//                color = surfaceColor
//
//            ) {
//                Column(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 16.dp)
//
//
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .padding(bottom = 0.dp)
//                            .fillMaxWidth(),
//
//
//                        ) {
//                        Column(
//                        ) {
//                            Text(
//                                text = "Available hotels",
//                                fontSize = 32.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                modifier = Modifier
//                                    .padding(bottom = 18.dp)
//                                    .padding(start = 10.dp)
//                            )
//
//                            displayListMenuForHome(hotelDetail = hotels)
//
//                        }
//
//                    }
//                }
//            }
//        }
//
//    }
//
//}
@Composable
fun homescreen(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    val seatAndViewButtonColor = Color(android.graphics.Color.parseColor("#D9D9D9"))
    val buttonTextColor = Color(android.graphics.Color.parseColor("#222222"))

    val repository = remember { HotelRepository() } // ✅ Create repository instance
    val viewModel: HotelViewModel = viewModel(factory = HotelViewModelFactory(repository)) // ✅ Use ViewModel Factory

    val hotels by viewModel.hotelList.collectAsState()

    LaunchedEffect(true) {
        viewModel.fetchHotels() // ✅ Fetch data once
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = surfaceColor
    ) {
        Column {
            // ✅ Book Seat & View Seats Buttons (Re-added)
            Surface(
                modifier = Modifier.padding(vertical = 1.dp, horizontal = 10.dp),
                color = surfaceColor
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 0.dp)
                ) {
                    Button(
                        onClick = { navController.navigate("availableHotelsForBookSeats") },
                        modifier = Modifier.weight(1f).padding(5.dp).height(80.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(seatAndViewButtonColor)
                    ) {
                        Text(text = "Book seats", color = buttonTextColor, fontSize = 20.sp)
                    }

                    Button(
                        onClick = { navController.navigate("viewSeatAvailableHotelList") },
                        modifier = Modifier.weight(1f).padding(5.dp).height(80.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(seatAndViewButtonColor)
                    ) {
                        Text(text = "View seats", color = buttonTextColor, fontSize = 20.sp)
                    }
                }
            }

            // ✅ Available Hotels Section
            Surface(
                modifier = Modifier.padding(vertical = 0.dp, horizontal = 10.dp).padding(bottom = 24.dp),
                shape = RoundedCornerShape(5.dp),
                color = surfaceColor
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    Text(
                        text = "Available hotels",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 18.dp).padding(start = 10.dp)
                    )

                    if (hotels.isNotEmpty()) {
                        displayListMenuForHome(hotelDetail = hotels) // ✅ Display hotels
                    } else {
                        Text(
                            text = "Loading hotels...",
                            modifier = Modifier.padding(16.dp),
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun home(){
    val navController = rememberNavController()
    homescreen(navController = navController)
}