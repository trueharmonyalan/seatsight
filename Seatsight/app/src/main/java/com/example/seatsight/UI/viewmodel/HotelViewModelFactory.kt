package com.example.seatsight.UI.viewmodel

import HotelViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.seatsight.data.repository.HotelRepository

class HotelViewModelFactory(private val repository: HotelRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HotelViewModel::class.java)) {
            return HotelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
