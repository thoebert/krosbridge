package com.github.thoebert.krosbridge

import org.glassfish.tyrus.server.Server
import java.net.http.WebSocket

class DummyServer(port: Int) {
    private val s: Server

    init {
        setupLogging()
        s = Server("localhost", port, "", null, DummyHandler::class.java)
    }

    fun start(): Boolean {
        return try {
            s.start()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun stop() {
        s.stop()
    }
}