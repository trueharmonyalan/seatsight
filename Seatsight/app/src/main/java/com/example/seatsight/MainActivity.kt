package com.example.seatsight

import BookingConfirmationScreen
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
        }else {
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
                            appPreferences.isWelcomeScreenCompleted=true
                            navController.navigate(AuthScreen.route){
                                popUpTo(Welcome.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }



                composable(route = AuthScreen.route){
                    AuthHome(navController = navController)
                }

                composable(route = LoginScreen.route){
                    LoginScreen(navController = navController) // Pass navController
                }

                composable(route = RegisterScreen.route){
                    RegisterScreen(navController = navController) // Pass navController
                }

                    composable(route = Home.route){
                        homescreen(navController = navController)
                }

                composable(route = ViewSeatAvailableHotelList.route) {
                    ViewSeatAvailableHotelList.screen()
                }

//                composable(route = AvailableHotelsForBookSeat.route){
//                    AvailableHotelsForBookSeat.screen()
//                }

                composable(
                    route = "viewSeatScreen/{hotelName}/{restaurantId}",
                    arguments = listOf(
                        navArgument("hotelName") { type = NavType.StringType },
                        navArgument("restaurantId") { type = NavType.IntType }
                    )
                ) { backStackEntry ->
                    val hotelName = Uri.decode(backStackEntry.arguments?.getString("hotelName") ?: "") // ✅ Decode here
                    val restaurantId = backStackEntry.arguments?.getInt("restaurantId") ?: 0

                    Log.d("NavHost", "Navigating to ViewSeatsScreen with hotel: $hotelName, id: $restaurantId") // ✅ Debug log

                    ViewSeatsScreen(
                        hotelName = hotelName,
                        restaurantId = restaurantId,
                        navController = navController
                    )
                }



























                composable(route = AvailableHotelsForBookSeat.route) {
                        BookSeatWindow(navController = navController)
                    }

                // Navigate to BookSeatScreen with dynamic hotel name
                composable(
                    route = "bookSeatScreen/{hotelName}/{restaurantId}", // ✅ Ensure both parameters are in route
                    arguments = listOf(
                        navArgument("hotelName") { type = NavType.StringType },
                        navArgument("restaurantId") { type = NavType.IntType } // ✅ Ensure restaurantId is extracted as an Int
                    )
                ) { backStackEntry ->
                    val hotelName = backStackEntry.arguments?.getString("hotelName") ?: ""
                    val restaurantId = backStackEntry.arguments?.getInt("restaurantId") ?: 0 // ✅ Extract restaurantId safely

                    Log.d("NavHost", "Extracted restaurantId: $restaurantId") // ✅ Debugging log

                    bookSeatScreen.screen(
                        mapOf(
                            "hotelName" to hotelName,
                            "restaurantId" to restaurantId.toString() // ✅ Pass restaurantId as a string
                        ),
                        navController
                    )
                }


                // Navigate to BookingConfirmationScreen with selected seats
                composable(route = bookingConfirmation.route) { backStackEntry ->
                    val hotelName = backStackEntry.arguments?.getString("hotelName") ?: ""
                    val selectedSeats = backStackEntry.arguments?.getString("selectedSeats") ?: ""

                    bookingConfirmation.screen(
                        mapOf(
                            "hotelName" to hotelName,
                            "selectedSeats" to selectedSeats
                        ),
                        navController
                    )
                }


            }

        }

    }


}




































@Composable
private fun ViewSeatDisplay(
    modifier: Modifier =Modifier,


    ){

}




//?????????????????????????????????????????????
// preview section
//........
//...
//..
//?????????????????????????????????????????????


//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//Preview section for homescreen composable
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

//@Preview
//@Composable
//fun homescreenPreview() {
//    SeatsightTheme {
//        homescreen(viewSeatButtonClicked = {}, bookSeatButtonClicked = {})
//    }
//
//}

////>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
////Preview section for welcomescreen composable
////>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//@Preview
//@Composable
//fun welcome(){
//    SeatsightTheme {
//        welcomeScreen()
//    }
//}



//@Preview
//@Composable
//fun com.example.seatsight.UI.bookSeatWindow(){
//    SeatsightTheme {
//        com.example.seatsight.UI.BookSeatWindow()
//    }
//}
