
package com.example.seatsight


import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.seatsight.UI.BookSeatScreen
import com.example.seatsight.UI.BookSeatWindow
import com.example.seatsight.UI.ViewSeatsScreen
import com.example.seatsight.UI.authentication.AuthHome
import com.example.seatsight.UI.authentication.LoginScreen
import com.example.seatsight.UI.authentication.RegisterScreen
import com.example.seatsight.UI.homescreen
import com.example.seatsight.UI.welcomeScreen
import com.example.seatsight.ui.theme.SeatsightTheme
import com.example.seatsight.ViewSeatAvailableHotelList

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeatSightApp()
        }
    }
}

@Composable
fun SeatSightApp(){
    SeatsightTheme {
        val navController = rememberNavController()
        val context = LocalContext.current
        val appPreferences = remember { SharedPreferenceOnboardingProcess(context) }
        val startDestination = if(appPreferences.isWelcomeScreenCompleted){
            AuthScreen.route
        } else {
            Welcome.route
        }
        Scaffold { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ){
                composable(route = Welcome.route){
                    welcomeScreen(
                        onClickContinue = {
                            appPreferences.isWelcomeScreenCompleted = true
                            navController.navigate(AuthScreen.route){
                                popUpTo(Welcome.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(route = AuthScreen.route){
                    AuthHome(navController = navController)
                }
                composable(route = LoginScreen.route){
                    LoginScreen(navController = navController)
                }
                composable(route = RegisterScreen.route){
                    RegisterScreen(navController = navController)
                }
                composable(route = Home.route){
                    homescreen(navController = navController)
                }
                // Updated navigation for ViewSeatAvailableHotelList:
                composable(route = ViewSeatAvailableHotelList.route) {
                    // Call dynamic destination screen with an empty parameter map and pass the navController.
                    ViewSeatAvailableHotelList.screen(emptyMap(), navController)
                }


                composable(
                    route = "viewSeatScreen/{hotelName}/{restaurantId}",
                    arguments = listOf(
                        navArgument("hotelName") { type = NavType.StringType },
                        navArgument("restaurantId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val hotelName = Uri.decode(backStackEntry.arguments?.getString("hotelName") ?: "")
                    val restaurantId = backStackEntry.arguments?.getInt("restaurantId") ?: 0
                    Log.d("NavHost", "Navigating to ViewSeatsScreen with hotel: $hotelName, id: $restaurantId")
                    ViewSeatsScreen(
                        hotelName = hotelName,
                        restaurantId = restaurantId,
                        navController = navController
                    )
                }


                composable(route = AvailableHotelsForBookSeat.route){
                    BookSeatWindow(navController = navController)
                }


                composable(
                    route = "bookSeatScreen/{hotelName}/{restaurantId}",
                    arguments = listOf(
                        navArgument("hotelName") { type = NavType.StringType },
                        navArgument("restaurantId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val hotelName = backStackEntry.arguments?.getString("hotelName") ?: ""
                    val restaurantId = backStackEntry.arguments?.getInt("restaurantId") ?: 0
                    Log.d("NavHost", "Extracted restaurantId: $restaurantId")
                    bookSeatScreen.screen(
                        mapOf(
                            "hotelName" to hotelName,
                            "restaurantId" to restaurantId.toString()
                        ),
                        navController
                    )
                }
                composable(
                    route = bookingConfirmation.route,
                    arguments = listOf(
                        navArgument("hotelName") { type = NavType.StringType },
                        navArgument("selectedSeats") { type = NavType.StringType },
                        navArgument("selectedMenu") { type = NavType.StringType },
                        navArgument("restaurantId") { type = NavType.StringType }
                    )
                ) { backStackEntry ->
                    val hotelName = backStackEntry.arguments?.getString("hotelName") ?: ""
                    val selectedSeats = backStackEntry.arguments?.getString("selectedSeats") ?: ""
                    val selectedMenu = backStackEntry.arguments?.getString("selectedMenu") ?: ""
                    val restaurantId = backStackEntry.arguments?.getString("restaurantId") ?: "0"

                    Log.d("MainActivity", "Navigating with: hotelName=$hotelName, selectedSeats=$selectedSeats, selectedMenu=$selectedMenu, restaurantId=$restaurantId")

                    bookingConfirmation.screen(
                        mapOf(
                            "hotelName" to hotelName,
                            "selectedSeats" to selectedSeats,
                            "selectedMenu" to selectedMenu,
                            "restaurantId" to restaurantId
                        ),
                        navController
                    )
                }
            }
        }
    }
}