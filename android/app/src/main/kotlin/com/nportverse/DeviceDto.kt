package com.nportverse.nft_exchange

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

data class DeviceDto(
    @SerializedName("id") var id: String,
    @SerializedName("name") var name: String,
    @SerializedName("state") var state: ConnectionState,
)

enum class ConnectionState {
    CONNECTING,
    CONNECTED,
    NOT_CONNECTED,
}

class ConnectionStateAdapter : JsonDeserializer<ConnectionState> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?,
    ): ConnectionState {
        if ( json?.asInt !is Int) {
            throw JsonParseException("Invalid int value for ConnectionState")
        }
        return ConnectionState.values()[json?.asInt!!]
    }
}
