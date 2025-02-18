package com.example.seatsight.UI.authentication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.Home
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val focusColor = Color(android.graphics.Color.parseColor("#302F2F")) // Button color

    fun validateAndRegister() {
        when {
            email.isBlank() -> {
                errorMessage = "Please enter your email."
                showErrorDialog = true
            }
            password.isBlank() -> {
                errorMessage = "Please enter your password."
                showErrorDialog = true
            }
            else -> {
                // Navigate to Welcome Screen after successful registration
                navController.navigate(Home.route)
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SeatSight",
                    fontSize = 30.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Create your account",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Text Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = focusColor, // Border color when focused
                        cursorColor = focusColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Password Text Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = focusColor, // Border color when focused
                        cursorColor = focusColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { validateAndRegister() },
                    modifier = Modifier.padding(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = focusColor
                    )
                ) {
                    Text(
                        text = "Register",
                        color = Color(android.graphics.Color.parseColor("#F7F7F7"))
                    )
                }
            }

            // Error Dialog
            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    confirmButton = {
                        Button(
                            onClick = { showErrorDialog = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = focusColor, // Same as register button
                                contentColor = Color.White
                            )
                        ) {
                            Text("OK", fontSize = 16.sp)
                        }
                    },
                    title = {
                        Text(
                            text = "Registration Error",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    },
                    text = {
                        Text(
                            text = errorMessage,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.surface, // Uses existing theme
                    shape = RoundedCornerShape(12.dp) // Modern UI
                )
            }

            // Footer at the bottom center
            Text(
                text = "SeatSight $currentYear",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Preview
@Composable
fun RegisterScreenPrev() {
    val navController = rememberNavController()
    RegisterScreen(navController = navController)
}
