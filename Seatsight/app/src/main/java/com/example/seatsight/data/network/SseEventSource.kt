package com.example.seatsight.data.network

import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Utility class for creating and managing Server-Sent Events connections.
 */
class SseEventSource {
    private val TAG = "SseEventSource"

    // Track the active event source
    private var activeEventSource: EventSource? = null
    private val isConnected = AtomicBoolean(false)

    // Configure OkHttp client for SSE with longer timeouts
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS) // Longer read timeout for SSE
        .retryOnConnectionFailure(true)
        .build()

    private val factory = EventSources.createFactory(client)

    /**
     * Connect to an SSE endpoint and return a Flow of event data.
     *
     * @param url The SSE endpoint URL
     * @return Flow of string data received from the server
     */
    fun connect(url: String): Flow<String> = callbackFlow {
        // First ensure any existing connection is closed properly
        closeConnection()

        // Fix double slashes in URL
        val sanitizedUrl = url.replace("//api", "/api")
        Log.d(TAG, "Connecting to SSE endpoint: $sanitizedUrl")

        val request = Request.Builder()
            .url(sanitizedUrl)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d(TAG, "SSE connection opened successfully")
                isConnected.set(true)
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                Log.d(TAG, "Received SSE event data")
                trySend(data)
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e(TAG, "SSE connection failed", t)
                isConnected.set(false)
                // Don't close the flow right away - let the consumer decide
                // This prevents crashes when the connection fails
                trySend("ERROR: ${t?.message ?: "Unknown error"}")
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d(TAG, "SSE connection closed by server")
                isConnected.set(false)
                close()
            }
        }

        try {
            val eventSource = factory.newEventSource(request, listener)
            activeEventSource = eventSource
        } catch (e: Exception) {
            Log.e(TAG, "Error creating event source", e)
            close(e)
        }

        awaitClose {
            Log.d(TAG, "Flow closing, cancelling SSE connection")
            closeConnection()
        }
    }.catch { exception ->
        // Provide an opportunity to handle exceptions without crashing
        Log.e(TAG, "Error in SSE flow", exception)
        throw exception  // Rethrow to allow caller to handle
    }

    /**
     * Closes any active connection.
     * Safe to call even if no connection exists.
     */
    fun closeConnection() {
        val eventSource = activeEventSource
        activeEventSource = null

        if (eventSource != null) {
            Log.d(TAG, "Explicitly closing previous SSE connection")
            try {
                eventSource.cancel()
                isConnected.set(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error closing SSE connection", e)
            }
        }
    }

    /**
     * Check if a connection is currently active
     */
    fun isConnectionActive(): Boolean {
        return isConnected.get() && activeEventSource != null
    }
}