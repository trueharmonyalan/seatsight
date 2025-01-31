package com.example.seatsight

import BookSeatWindow
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
            Home.route
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
                            navController.navigate(Home.route){
                                popUpTo(Welcome.route) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }

                    composable(route = Home.route){
                        homescreen(navController = navController)
                }

                composable(route = ViewSeatAvailableHotelList.route) {
                    ViewSeatAvailableHotelList.screen()
                }

                composable(route = AvailableHotelsForBookSeat.route){
                    AvailableHotelsForBookSeat.screen()
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
//fun bookSeatWindow(){
//    SeatsightTheme {
//        BookSeatWindow()
//    }
//}
