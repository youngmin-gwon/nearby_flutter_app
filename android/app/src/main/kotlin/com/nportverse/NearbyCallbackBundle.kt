package com.nportverse.nft_exchange

import android.app.Activity
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.flutter.plugin.common.MethodChannel

const val INVOKE_CHANGE_STATE_METHOD = "invoke_change_state_method"

class NearbyCallbackBundle constructor(private val channel: MethodChannel, private val activity: Activity) {
    private val devices = mutableListOf<DeviceDto>()
    private val gson: Gson =
        GsonBuilder()
            .registerTypeAdapter(ConnectionState::class.java, ConnectionStateAdapter())
            .create()

    private fun checkDeviceExistsById(id: String) = deviceById(id) != null

    private fun deviceById(id: String): DeviceDto? = devices.find { element -> element.id == id }

    fun updateState(
        id: String,
        state: ConnectionState,
    ) {
        deviceById(id)?.state = state
        val json = gson.toJson(devices)
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, json)
    }

    fun addDevice(device: DeviceDto) {
        if (checkDeviceExistsById(device.id)) {
            updateState(device.id, device.state)
        } else {
            devices.add(device)
        }
        val json = gson.toJson(devices)
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, json)
    }

    fun removeDevice(device: DeviceDto) {
        devices.remove(device)
        val json = gson.toJson(devices)
        channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, json)
    }

    val endpointDiscoveryCallback: EndpointDiscoveryCallback =
        object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(
                endpointId: String,
                discoveredEndpointInfo: DiscoveredEndpointInfo,
            ) {
                Log.d("nearby_connections", "onEndpointFound $discoveredEndpointInfo")
                if (!checkDeviceExistsById(endpointId)) {
                    val data = DeviceDto(endpointId, discoveredEndpointInfo.endpointName, ConnectionState.NOT_CONNECTED)
                    addDevice(data)
                }
            }

            override fun onEndpointLost(endpointId: String) {
                Log.d("nearby_connections", "onEndpointLost $endpointId")
                if (checkDeviceExistsById(endpointId)) {
                    Nearby.getConnectionsClient(activity).disconnectFromEndpoint(endpointId)
                }

                val device = deviceById(endpointId) ?: return
                removeDevice(device)
            }
        }

    private val payloadCallback: PayloadCallback =
        object : PayloadCallback() {
            override fun onPayloadReceived(
                endpointId: String,
                payload: Payload,
            ) {
                Log.d("nearby_connections", "onPayloadReceived $endpointId")
                val args = mutableMapOf<String, String>("id" to endpointId, "message" to String(payload.asBytes()!!))
                channel.invokeMethod(INVOKE_CHANGE_STATE_METHOD, args)
            }

            override fun onPayloadTransferUpdate(
                endpointId: String,
                payloadTransferUpdate: PayloadTransferUpdate,
            ) {
                Log.d("nearby_connections", "onPayloadTransferUpdate $endpointId")
            }
        }

    val connectionLifecycleCallback =
        object : ConnectionLifecycleCallback() {
            override fun onConnectionInitiated(
                endpointId: String,
                connectionInfo: ConnectionInfo,
            ) {
                Log.d("nearby_connections", "onConnectionInitiated $connectionInfo")
                val data = DeviceDto(endpointId, connectionInfo.endpointName, ConnectionState.CONNECTING)
                Nearby.getConnectionsClient(activity).acceptConnection(endpointId, payloadCallback)
            }

            override fun onConnectionResult(
                endpointId: String,
                result: ConnectionResolution,
            ) {
                Log.d("nearby_connections", "onConnectionResult $endpointId")
                val data =
                    if (result.status.isSuccess) {
                        DeviceDto(
                            endpointId,
                            if (deviceById(endpointId)?.name == null) "Null" else deviceById(endpointId)?.name!!,
                            ConnectionState.CONNECTED,
                        )
                    } else {
                        DeviceDto(
                            endpointId,
                            if (deviceById(endpointId)?.name == null) "Null" else deviceById(endpointId)?.name!!,
                            ConnectionState.NOT_CONNECTED,
                        )
                    }
                addDevice(data)
            }

            override fun onDisconnected(endpointId: String) {
                Log.d("nearby_connections", "onDisconnected $endpointId")
                if (checkDeviceExistsById(endpointId)) {
                    updateState(endpointId, ConnectionState.NOT_CONNECTED)
                } else {
                    val data =
                        DeviceDto(
                            endpointId,
                            if (deviceById(endpointId)?.name == null) "Null" else deviceById(endpointId)?.name!!,
                            ConnectionState.NOT_CONNECTED,
                        )
                    addDevice(data)
                }
            }
        }
}
