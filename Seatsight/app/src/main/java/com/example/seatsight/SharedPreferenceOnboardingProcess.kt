package com.example.seatsight

import android.content.Context

class SharedPreferenceOnboardingProcess(context : Context) {
    private val sharedPreferences = context.getSharedPreferences("SharedPreferenceOnboardingProcess",Context.MODE_PRIVATE)

var isWelcomeScreenCompleted : Boolean
    get() = sharedPreferences.getBoolean("isWelcomeScreenCompleted",false)
    set(value) {
        sharedPreferences.edit().putBoolean("isWelcomeScreenCompleted", value).apply()
    }
}
