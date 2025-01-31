package com.example.seatsight.UI


import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.isTraceInProgress
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.seatsight.data.hotels
import com.example.seatsight.ui.theme.SeatsightTheme

val buttonColorBook = Color(android.graphics.Color.parseColor("#045F1F"))

@Composable
fun BookSeatScreen(
    modifier: Modifier = Modifier,


    ){
    val background = Color(android.graphics.Color.parseColor("#E4E4E4"))




    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
    ) {

        Text("Hotel Name Here")
        
        Column (
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 50.dp, bottom = 50.dp)
                .padding(10.dp)


                .background(surfaceColor),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            Column(
                modifier = Modifier

            ) {

                Row(
                    modifier = Modifier

                ) {



                    SeatSelector()


                }
                ButtonForBookAndView()

            }

        }
    }

}

@Composable
fun SeatSelector(modifier: Modifier =Modifier){
    var seats = listOf("S1","S2")
    OutlinedButton(onClick = {}) {

    }



}

@Composable
fun SeatbookingLogic(){

}




@Composable
fun ButtonForBookAndView(modifier: Modifier =Modifier){


    Column {
        Button(
            onClick = {},
            modifier = Modifier
                .height(40.dp)
                .width(160.dp),



            ) {
            Text(text = "Book",
                fontSize = 16.sp,
                )
        }

    }
    }










@Preview
@Composable
fun bookseat(){
    BookSeatScreen()
}
@Preview
@Composable
fun seatlayout(){
    SeatSelector()
}