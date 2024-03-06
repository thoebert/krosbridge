import com.github.thoebert.krosbridge.Ros
import com.github.thoebert.krosbridge.Topic
import kotlin.test.Test
import kotlin.test.assertEquals


class TestJRosbridge {
    @Test
    fun testEnums() {
        assertEquals(2, Topic.CompressionType.entries.size)
        assertEquals(
            Topic.CompressionType.png,
            Topic.CompressionType.valueOf("png")
        )
        assertEquals(
            Topic.CompressionType.none,
            Topic.CompressionType.valueOf("none")
        )
        assertEquals(2, Ros.WebSocketType.entries.size)
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