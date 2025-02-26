package com.example.seatsight.data.api

import com.example.seatsight.data.model.LoginRequest
import com.example.seatsight.data.model.LoginResponse
import com.example.seatsight.data.model.RegisterRequest
import com.example.seatsight.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/customers/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/customers/register")  // ✅ Backend will auto-assign "customer"
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>
}
