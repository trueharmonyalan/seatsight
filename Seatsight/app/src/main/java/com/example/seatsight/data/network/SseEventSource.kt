
package com.example.seatsight.data.network

import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * Handles Server-Sent Events (SSE) connections for real-time data streaming.
 */
class SseEventSource {
    private val TAG = "SseEventSource"

    // Store the current event source
    private val currentEventSource = AtomicReference<EventSource?>(null)

    // Create a client with appropriate settings
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    /**
     * Connect to an SSE endpoint and return a Flow that emits events.
     *
     * @param url The SSE endpoint URL
     * @return Flow that emits event data as Strings
     */
    fun connect(url: String): Flow<String> = callbackFlow {
        Log.d(TAG, "Connecting to SSE endpoint: $url")

        // Close any existing connection
        closeConnection()

        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.d(TAG, "SSE connection opened: $url")
                trySend("CONNECTION_ESTABLISHED").isSuccess
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                if (data.isBlank()) return

                Log.d(TAG, "Received SSE event: ${data.take(50)}...")
                trySend(data).isSuccess
            }

            override fun onClosed(eventSource: EventSource) {
                Log.d(TAG, "SSE connection closed normally")
                trySend("CONNECTION_CLOSED").isSuccess
                channel.close()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e(TAG, "SSE connection failed", t)
                trySend("CONNECTION_ERROR").isSuccess
                channel.close(t)
            }
        }

        try {
            val newEventSource = EventSources.createFactory(client).newEventSource(request, listener)
            currentEventSource.set(newEventSource)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create event source", e)
            channel.close(e)
        }

        awaitClose {
            Log.d(TAG, "Flow closing, cancelling SSE connection")
            closeConnection()
        }
    }

    /**
     * Close the current SSE connection if open.
     */
    fun closeConnection() {
        val eventSource = currentEventSource.getAndSet(null)
        if (eventSource != null) {
            try {
                Log.d(TAG, "Closing SSE connection")
                eventSource.cancel()
                Log.d(TAG, "SSE connection closed")
            } catch (e: Exception) {
                Log.e(TAG, "Error closing SSE connection", e)
            }
        }
    }
}