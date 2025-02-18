package com.example.seatsight.UI.authentication


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.UI.homescreen
import java.util.Calendar


@Composable
fun AuthHome(
    navController: NavController
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center // Center content inside Box
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SeatSight",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Welcome! Please sign in or create an account.",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {navController.navigate("RegisterScreen")},
                    modifier = Modifier.padding(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(android.graphics.Color.parseColor("#302F2F"))
                    )
                ) {
                    Text(
                        text = "Register",
                        color = Color(android.graphics.Color.parseColor("#F7F7F7"))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { navController.navigate("LoginScreen") },
                    modifier = Modifier.padding(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(android.graphics.Color.parseColor("#302F2F"))
                    )
                ) {
                    Text(
                        text = "Login",
                        color = Color(android.graphics.Color.parseColor("#F7F7F7"))
                    )
                }
            }

            // Footer at the bottom center
            Text(
                text = "SeatSight $currentYear",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomCenter) // Align text to bottom
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Preview
@Composable
fun AuthHomePrev(){
    val navController = rememberNavController()
    AuthHome(navController = navController)
}