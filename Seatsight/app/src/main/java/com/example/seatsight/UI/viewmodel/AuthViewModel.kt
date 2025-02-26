package com.example.seatsight.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.seatsight.data.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableStateFlow<Result<String>?>(null)
    val loginResult: StateFlow<Result<String>?> = _loginResult

    private val _registerResult = MutableStateFlow<Result<String>?>(null)
    val registerResult: StateFlow<Result<String>?> = _registerResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.login(email, password)
                if (response.isSuccessful) {
                    _loginResult.value = Result.success("Login successful!")
                } else {
                    _loginResult.value = Result.failure(Exception("Invalid credentials"))
                }
            } catch (e: Exception) {
                _loginResult.value = Result.failure(e)
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.register(email, password)
                if (response.isSuccessful) {
                    _registerResult.value = Result.success("Registration successful!")
                } else {
                    _registerResult.value = Result.failure(Exception("Registration failed"))
                }
            } catch (e: Exception) {
                _registerResult.value = Result.failure(e)
            }
        }
    }
}
