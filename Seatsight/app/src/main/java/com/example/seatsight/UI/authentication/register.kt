
package com.example.seatsight.UI.authentication

import android.util.Log
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
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.Home
import com.example.seatsight.data.repository.AuthRepository
import kotlinx.coroutines.launch
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val authRepository = AuthRepository()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val focusColor = Color(android.graphics.Color.parseColor("#302F2F"))

    // ✅ Fix: Use CoroutineScope (Not LaunchedEffect Here)
    val coroutineScope = rememberCoroutineScope()

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
                isLoading = true
                coroutineScope.launch {
                    try {
                        val response = authRepository.register(email, password)
                        if (response.isSuccessful && response.body() != null) {
                            Log.d("API_TEST", "Registration Successful: ${response.body()?.message}")
                            navController.navigate(Home.route)
                        } else {
                            errorMessage = "Registration failed: ${response.errorBody()?.string() ?: "Unknown error"}"
                            showErrorDialog = true
                        }
                    } catch (e: Exception) {
                        errorMessage = "API Error: ${e.message}"
                        showErrorDialog = true
                        Log.e("API_TEST", "Registration Failed", e)
                    }
                    isLoading = false
                }
            }
        }
    }


    // 🎨 UI Layout (No Change)
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "SeatSight", fontSize = 30.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Create your account", fontSize = 18.sp)
                Spacer(modifier = Modifier.height(24.dp))

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(0.8f).padding(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = focusColor, cursorColor = focusColor),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(0.8f).padding(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = focusColor, cursorColor = focusColor),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Register Button
                Button(
                    onClick = { validateAndRegister() },
                    modifier = Modifier.padding(),
                    colors = ButtonDefaults.buttonColors(containerColor = focusColor),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White)
                    } else {
                        Text(text = "Register", color = Color.White)
                    }
                }
            }

            // Error Dialog
            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    confirmButton = {
                        Button(
                            onClick = { showErrorDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = focusColor, contentColor = Color.White)
                        ) {
                            Text("OK", fontSize = 16.sp)
                        }
                    },
                    title = { Text(text = "Registration Error", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                    text = { Text(text = errorMessage, fontSize = 16.sp) },
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Footer at Bottom
            Text(
                text = "SeatSight $currentYear",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)
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

