package com.example.seatsight.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionManager(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore(name = "user_session")
        private val CUSTOMER_ID_KEY = intPreferencesKey("customer_id")
        private val EMAIL_KEY = stringPreferencesKey("email")
    }

    // Get customer ID flow
    val customerId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[CUSTOMER_ID_KEY]
    }

    // Get email flow
    val email: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[EMAIL_KEY]
    }

    // Save user session data
    suspend fun saveUserSession(customerId: Int, email: String) {
        context.dataStore.edit { preferences ->
            preferences[CUSTOMER_ID_KEY] = customerId
            preferences[EMAIL_KEY] = email
        }
    }

    // Clear session data on logout
    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}