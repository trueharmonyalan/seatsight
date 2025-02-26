package com.example.seatsight.data.model

// ✅ Registration Request Model (No Role Needed)
data class RegisterRequest(
    val email: String,
    val password: String
)

// ✅ Login Request Model
data class LoginRequest(
    val email: String,
    val password: String
)

// ✅ Model for API Response When Registering a New User
data class RegisterResponse(
    val message: String,
    val user: UserData
)

// ✅ Model for API Response When Logging In
data class LoginResponse(
    val id: Int,
    val email: String,
    val token: String
)

// ✅ User Data Model
data class UserData(
    val id: Int,
    val email: String
)