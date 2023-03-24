package com.github.thoebert.krosbridge

import jakarta.json.Json
import jakarta.json.JsonObject
import java.io.StringReader
import javax.websocket.OnMessage
import javax.websocket.server.ServerEndpoint

@ServerEndpoint(value = "/")
class DummyHandler {
    @OnMessage
    fun onMessage(message: String?) {
        //println("Bridge received: "+message)
        val json = Json.createReader(StringReader(message)).readObject()
        latest = json
    }

    companion object {
        var latest: JsonObject? = null
    }
}