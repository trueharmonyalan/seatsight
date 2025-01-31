package com.example.seatsight.UI

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.data.hotels


//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
//HomeScreen Design.
//here we pass the listMenu composable's required list of hoteldetails from the data class hotelDetails
//>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
val surfaceColor = Color(android.graphics.Color.parseColor("#E4E4E4"))
@Composable
 fun homescreen(modifier: Modifier = Modifier,
             navController: NavController ) {

    val seatAndViewButtonColor = Color(android.graphics.Color.parseColor("#D9D9D9"))

    val hotelNamehereOuterColor = Color(android.graphics.Color.parseColor("#F0EBEB"))

    val buttonTextColor = Color(android.graphics.Color.parseColor("#222222"))




    Surface(
        modifier = Modifier.fillMaxSize(),

        color = surfaceColor


    ) {


        Column {
// book seat button and view seats button
            Surface(
                modifier = Modifier
                    .padding(vertical = 1.dp, horizontal = 10.dp),
                color = surfaceColor



            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 0.dp)


                ) {
                    Button(
                        onClick = {navController.navigate("availableHotelsForBookSeats")},
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp)
                            .height(80.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(seatAndViewButtonColor)

                    ) {
                        Text(
                            text = "Book seats",
                            color = buttonTextColor,
                            fontSize = 20.sp
                        )

                    }

                    Button(
                        onClick = { navController.navigate("viewSeatAvailableSeatList") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(5.dp)
                            .height(80.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(seatAndViewButtonColor)
                    ) {
                        Text(text = "View seats",
                            color = buttonTextColor,
                            fontSize = 20.sp
                        )
                    }
                }
            }


// available hotels options
            Surface(
                modifier = Modifier
                    .padding(vertical = 0.dp, horizontal = 10.dp)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(5.dp),
                color = surfaceColor

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)


                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = 0.dp)
                            .fillMaxWidth(),


                        ) {
                        Column(
                        ) {
                            Text(
                                text = "Available hotels",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(bottom = 18.dp)
                                    .padding(start = 10.dp)
                            )

                            displayListMenu(hotelDetail = hotels)

                        }

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