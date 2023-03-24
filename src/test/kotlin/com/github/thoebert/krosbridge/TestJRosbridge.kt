package com.github.thoebert.krosbridge

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestJRosbridge {
    @Test
    fun testEnums() {
        assertEquals(2, Topic.CompressionType.values().size)
        assertEquals(
            Topic.CompressionType.png,
            Topic.CompressionType.valueOf("png")
        )
        assertEquals(
            Topic.CompressionType.none,
            Topic.CompressionType.valueOf("none")
        )
        assertEquals(2, Ros.WebSocketType.values().size)
        assertEquals(
            Ros.WebSocketType.ws,
            Ros.WebSocketType.valueOf("ws")
        )
        assertEquals(
            Ros.WebSocketType.wss,
            Ros.WebSocketType.valueOf("wss")
        )
    }
}