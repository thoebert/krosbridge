import kotlin.test.Test
import kotlin.test.assertTrue

class TestDummyServer {
    @Test
    fun testConnect() {
        val server = DummyServer(9091)
        assertTrue(server.start())
        server.stop()
    }
}