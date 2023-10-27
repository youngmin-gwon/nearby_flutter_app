package com.nportverse.nft_exchange

import androidx.annotation.NonNull
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import java.util.Locale

const val SERVICE_ID = "flutter_nearby_connections"

class MainActivity : FlutterActivity() {
    private val METHOD_CHANNEL = "example.com/channel/method"

    override fun configureFlutterEngine(
        @NonNull flutterEngine: FlutterEngine,
    ) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            METHOD_CHANNEL,
        ).setMethodCallHandler { call, result ->
            try {
                // TODO: handle situations for each case
            } catch (e: IllegalArgumentException) {
                // TODO: define argument error type
                result.error("", e.message, null)
            } catch (e: Exception) {
                // TODO: define event processing exception
                result.error("", e.message, null)
            }
        }
    }
}

enum class ConnectionType {
    START_ADVERTISING,
    START_DISCOVERY,
    STOP_ADVERTISING,
    STOP_DISCOVERY,
    REQUEST_CONNECTION,
    ACCEPT_CONNECTION,
    REJECT_CONNECTION,
    STOP_ALL_ENDPOINTS,
    DISCONNECT_FROM_ENDPOINT,
    SEND_PAYLOAD,
    SEND_FILE_PAYLOAD,
    CANCEL_PAYLOAD,
    ;

    companion object {
        fun byName(name: String): ConnectionType? {
            return values().find { it.name == name.uppercase(Locale.getDefault()) }
        }
    }
}
