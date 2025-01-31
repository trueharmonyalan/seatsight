package com.example.seatsight.UI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

// Welcome screen design
//TODO: functionality is required for the BUTTON "Continue" -> Done
//TODO: functionality is required to add homescreen especially listMenu "Menu" button also homescreen's bookseats and viewseats button
@Composable
 fun welcomeScreen(modifier: Modifier = Modifier,
                   onClickContinue: () -> Unit) {

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {

        Column(

            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to the SeatSight",
                fontSize = 26.sp,
            )

            Button(
                onClick = onClickContinue,
                modifier = modifier.padding(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(android.graphics.Color.parseColor("#302F2F"))
                )
            ) {
                Text(
                    text = "Continue",
                    color = Color(android.graphics.Color.parseColor("#F7F7F7")),


                    )
            }
        }


    }


}
