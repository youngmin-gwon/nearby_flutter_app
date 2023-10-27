package com.nportverse.nft_exchange

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

const val NOTIFICATION_ID = 101
const val CHANNEL_ID = "channel"

class NearbyService : Service() {
    private val binder: IBinder = LocalBinder(this)
    private lateinit var connectionsClient: ConnectionsClient
    private lateinit var callbackBundle: NearbyCallbackBundle

    override fun onCreate() {
        super.onCreate()
        // Check whether the android version is greater than or equals to
        // API 26 (meaning Android Oreo or 8).
        // End users must know background service is working
        // in the case of the versions over 26.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, getNotification())
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun initService(callbackBundle: NearbyCallbackBundle) {
        connectionsClient = Nearby.getConnectionsClient(this)
        this@NearbyService.callbackBundle = callbackBundle
    }

    fun startAdvertising(
        strategy: Strategy,
        deviceName: String,
    ) {
        connectionsClient.startAdvertising(
            deviceName,
            SERVICE_ID,
            callbackBundle.connectionLifecycleCallback,
            AdvertisingOptions.Builder().setStrategy(strategy).build(),
        )
    }

    fun startDiscovery(strategy: Strategy) {
        connectionsClient.startDiscovery(
            SERVICE_ID,
            callbackBundle.endpointDiscoveryCallback,
            DiscoveryOptions.Builder().setStrategy(strategy).build(),
        )
    }

    fun stopAdvertising() {
        connectionsClient.stopAdvertising()
    }

    fun stopDiscovery() {
        connectionsClient.stopDiscovery()
    }

    fun connect(
        endpointId: String,
        displayName: String,
    ) {
        connectionsClient.requestConnection(displayName, endpointId, callbackBundle.connectionLifecycleCallback)
    }

    fun disconnect(endpointId: String) {
        connectionsClient.disconnectFromEndpoint(endpointId)
    }

    fun sendStringPayload(
        endpointId: String,
        rawPayload: String,
    ) {
        connectionsClient.sendPayload(endpointId, Payload.fromBytes(rawPayload.toByteArray()))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getNotification(): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Nearby Service")
            .setContentText("Wi-Fi Direct")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .build()
    }
}

internal class LocalBinder(private val nearbyService: NearbyService) : Binder() {
    val service: NearbyService
        get() = nearbyService
}
