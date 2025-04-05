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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seatsight.AuthScreen
import com.example.seatsight.Home
import com.example.seatsight.data.SessionManager
import com.example.seatsight.data.repository.AuthRepository
import com.example.seatsight.ui.viewmodel.AuthViewModel
import com.example.seatsight.ui.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch
import java.util.Calendar
@Composable
fun LoginScreen(navController: NavController) {
    val authRepository = remember { AuthRepository() } // ✅ Create Repository
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(authRepository)) // ✅ ViewModel Factory

    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val customerId by authViewModel.customerId.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val loginResult by authViewModel.loginResult.collectAsState()

    val focusColor = Color(android.graphics.Color.parseColor("#302F2F")) // Button color

    fun validateAndLogin() {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter valid credentials."
            showErrorDialog = true
        } else {
            isLoading = true
            authViewModel.login(email, password)
        }
    }

    LaunchedEffect(loginResult) {
        loginResult?.let {
            isLoading = false
            if (it.isSuccess) {
                Log.d("API_TEST", "Login Successful")

                // Store user session data when login is successful
                customerId?.let { id ->
                    sessionManager.saveUserSession(id, email)
                    Log.d("API_TEST", "Saved Customer ID: $id")
                }

                navController.navigate(Home.route) {
                    popUpTo("LoginScreen") { inclusive = true }
                }
            } else {
                errorMessage = "Login failed: ${it.exceptionOrNull()?.message}"
                showErrorDialog = true
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
                    text = "Login to your account",
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Input Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = focusColor,
                        cursorColor = focusColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // Password Input Field
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
                        focusedBorderColor = focusColor,
                        cursorColor = focusColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Login Button
                Button(
                    onClick = { validateAndLogin() },
                    modifier = Modifier.padding(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = focusColor
                    ),
                    enabled = !isLoading
                ) {
                    Text(
                        text = if (isLoading) "Logging in..." else "Login",
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
                                containerColor = focusColor,
                                contentColor = Color.White
                            )
                        ) {
                            Text("OK", fontSize = 16.sp)
                        }
                    },
                    title = {
                        Text(
                            text = "Login Error",
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
                    containerColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
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
fun LoginScreenPrev() {
    val navController = rememberNavController()
    LoginScreen(navController = navController)
}
