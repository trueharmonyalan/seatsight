package com.example.seatsight.data.repository

import android.util.Log
import com.example.seatsight.data.api.AuthService
import com.example.seatsight.data.model.LoginRequest
import com.example.seatsight.data.model.LoginResponse
import com.example.seatsight.data.model.RegisterRequest
import com.example.seatsight.data.model.RegisterResponse
import com.example.seatsight.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Response

class AuthRepository {
    private val authService = RetrofitClient.instance.create(AuthService::class.java)

    suspend fun login(email: String, password: String): Response<LoginResponse> {
        return authService.login(LoginRequest(email, password))
    }

    suspend fun register(email: String, password: String): Response<RegisterResponse> {
        return authService.register(RegisterRequest(email, password))
    }
}


