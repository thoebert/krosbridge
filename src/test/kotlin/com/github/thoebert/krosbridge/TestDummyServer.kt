package com.github.thoebert.krosbridge

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestDummyServer {
    @Test
    fun testConnect() {
        val server = DummyServer(9091)
        assertTrue(server.start())
        server.stop()
    }
}