package com.example.seatsight

import BookSeatWindow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.UI.authentication.AuthHome
import com.example.seatsight.UI.authentication.LoginScreen
import com.example.seatsight.UI.authentication.RegisterScreen
import com.example.seatsight.UI.welcomeScreen
import com.example.seatsight.UI.homescreen
import com.example.seatsight.UI.viewSeatWindow


interface seatSightDestinations {
    val route: String
    var screen: @Composable () -> Unit
    val trueOrFalse: Boolean
        get() = true
}





object Welcome : seatSightDestinations{

    override val route: String = "welcome"
    override var screen : @Composable () -> Unit ={  }
}

object Home : seatSightDestinations{
    override val route: String = "homeScreen"
    override var screen : @Composable () -> Unit= {
        val navController = rememberNavController()
        homescreen(navController = navController)}
}

object ViewSeatAvailableHotelList: seatSightDestinations{
    override val route: String ="viewSeatAvailableSeatList"
    override var screen: @Composable () -> Unit  = { viewSeatWindow() }

}

object AvailableHotelsForBookSeat : seatSightDestinations {
    override val route: String = "availableHotelsForBookSeats"
    override var screen: @Composable () -> Unit = {BookSeatWindow()}

}

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