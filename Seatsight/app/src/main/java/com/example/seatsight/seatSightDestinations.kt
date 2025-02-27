package com.example.seatsight

import BookingConfirmationScreen
import android.util.Log
import com.example.seatsight.UI.BookSeatWindow
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.UI.BookSeatScreen
import com.example.seatsight.UI.authentication.AuthHome
import com.example.seatsight.UI.authentication.LoginScreen
import com.example.seatsight.UI.authentication.RegisterScreen
import com.example.seatsight.UI.homescreen
import com.example.seatsight.UI.viewSeatWindow


interface dynamicseatSightDestinations {
    val route: String
    var screen: @Composable (Map<String, String>, NavController) -> Unit // Now accepts NavController
}



interface seatSightDestinations {


    val route: String
    var screen: @Composable () -> Unit
    val trueOrFalse: Boolean
        get() = true
}


//welcome route
object Welcome : seatSightDestinations{

    override val route: String = "welcome"
    override var screen : @Composable () -> Unit ={  }
}

//authentication route
object AuthScreen : seatSightDestinations {
    override val route: String = "AuthHome"
    override var screen: @Composable () -> Unit = {
        val navController = rememberNavController()
        AuthHome(navController = navController)}

}

object LoginScreen : seatSightDestinations {
    override val route: String = "LoginScreen"
    override var screen: @Composable () -> Unit = {
        val navController = rememberNavController()
        LoginScreen(navController = navController)}

}

object RegisterScreen : seatSightDestinations {
    override val route: String = "RegisterScreen"
    override var screen: @Composable () -> Unit = {
        val navController = rememberNavController()
        RegisterScreen(navController = navController)
    }

}


//homescreen route
object Home : seatSightDestinations{
    override val route: String = "homeScreen"
    override var screen : @Composable () -> Unit = {
        val navController = rememberNavController()
        homescreen(navController = navController)}
}


//seats viewing route
object ViewSeatAvailableHotelList: seatSightDestinations{
    override val route: String ="viewSeatAvailableSeatList"
    override var screen: @Composable () -> Unit = { viewSeatWindow() }

}


// book seats screen route
object AvailableHotelsForBookSeat : seatSightDestinations {
    override val route: String = "availableHotelsForBookSeats"
    override var screen: @Composable () -> Unit = {
        val navController = rememberNavController()
        BookSeatWindow(navController = navController) }

}

object bookSeatScreen : dynamicseatSightDestinations {
    override val route: String = "bookSeatScreen/{hotelName}/{restaurantId}" // ✅ Ensure parameter names match exactly

    override var screen: @Composable (Map<String, String>, NavController) -> Unit = { params, navController ->
        val hotelName = params["hotelName"] ?: ""
        val restaurantId = params["restaurantId"]?.toIntOrNull() ?: 0 // ✅ Extract properly

        Log.d("bookSeatScreen", "Raw restaurantId from params: ${params["restaurantId"]}") // ✅ Debug log
        Log.d("bookSeatScreen", "Parsed restaurantId: $restaurantId") // ✅ Debug log

        BookSeatScreen(
            hotelName = hotelName,
            restaurantId = restaurantId, // ✅ Pass correctly
            navController = navController
        )
    }
}







object bookingConfirmation : dynamicseatSightDestinations {
    override val route: String = "bookingConfirmation/{hotelName}/{selectedSeats}/{selectedMenu}"

    override var screen: @Composable (Map<String, String>, NavController) -> Unit = { params, navController ->
        val hotelName = params["hotelName"] ?: ""
        val selectedSeats = params["selectedSeats"]?.split(",")?.toSet() ?: emptySet()
        val selectedMenu = params["selectedMenu"]
            ?.split(",")
            ?.associate {
                val parts = it.split(" x") // Extract name and quantity
                parts[0] to (parts.getOrNull(1)?.toIntOrNull() ?: 0)
            } ?: emptyMap()

        BookingConfirmationScreen(
            hotelName = hotelName,
            selectedSeats = selectedSeats,
            selectedMenuItems = selectedMenu, // ✅ Pass selected menu items
            onConfirm = {
                navController.popBackStack()
            }
        )
    }
}



