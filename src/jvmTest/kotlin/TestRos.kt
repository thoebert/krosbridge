import com.github.thoebert.krosbridge.Ros
import io.ktor.util.reflect.*
import jakarta.json.Json
import kotlinx.coroutines.runBlocking
import kotlin.test.*


class TestRos {
    private var r1: Ros? = null
    private var r2: Ros? = null
    private var r3: Ros? = null
    private var r4: Ros? = null
    private var server: DummyServer? = null
    @BeforeTest
    @Throws(InterruptedException::class)
    fun setUp() {
        r1 = Ros()
        r2 = Ros("test")
        r3 = Ros("test2", -123)
        r4 = Ros(
            "test3",
            1234,
            Ros.WebSocketType.wss
        )
        server = DummyServer(r1!!.port)
        server!!.start()
    }

    @AfterTest
    fun tearDown() = runBlocking {
        r1!!.disconnect()
        r2!!.disconnect()
        r3!!.disconnect()
        r4!!.disconnect()
        server!!.stop()
        DummyHandler.latest = null
    }

    @Test
    fun testConstructor() {
        assertEquals(Ros.DEFAULT_HOSTNAME, r1!!.hostname)
        assertEquals(Ros.DEFAULT_PORT, r1!!.port)
        assertEquals(Ros.WebSocketType.ws, r1!!.protocol)
        assertEquals("ws://localhost:9090", r1!!.uRL)
        assertFalse(r1!!.isConnected)
    }

    @Test
    fun testStringConstructor() {
        assertEquals("test", r2!!.hostname)
        assertEquals(Ros.DEFAULT_PORT, r2!!.port)
        assertEquals(Ros.WebSocketType.ws, r2!!.protocol)
        assertEquals("ws://test:9090", r2!!.uRL)
        assertFalse(r2!!.isConnected)
    }

    @Test
    fun testStringAndIntConstructor() {
        assertEquals("test2", r3!!.hostname)
        assertEquals(-123, r3!!.port)
        assertEquals(Ros.WebSocketType.ws, r3!!.protocol)
        assertEquals("ws://test2:-123", r3!!.uRL)
        assertFalse(r3!!.isConnected)
    }

    @Test
    fun testStringIntAndProtocolConstructor() {
        assertEquals("test3", r4!!.hostname)
        assertEquals(1234, r4!!.port)
        assertEquals(Ros.WebSocketType.wss, r4!!.protocol)
        assertEquals("wss://test3:1234", r4!!.uRL)
        assertFalse(r4!!.isConnected)
    }

    @Test
    fun testNextID() {
        for (i in 0..19) {
            assertEquals(i.toLong(), r1!!.nextId())
            assertEquals(i.toLong(), r2!!.nextId())
            assertEquals(i.toLong(), r3!!.nextId())
            assertEquals(i.toLong(), r4!!.nextId())
        }
    }

    @Test
    @Throws(InterruptedException::class)
    fun testConnect() = runBlocking {
        assertTrue(r1!!.connect())
        assertTrue(r1!!.isConnected)
    }

    @Test
    fun testDisconnect() = runBlocking {
        assertTrue(r1!!.connect())
        assertTrue(r1!!.disconnect())
        assertFalse(r1!!.isConnected)
    }

    @Test
    fun testConnectFailed(): Unit = runBlocking {
        runCatching {
            assertFalse(r3!!.connect())
            assertFalse(r3!!.isConnected)
        }.onFailure {
            assertTrue(it.instanceOf(IllegalArgumentException::class))
        }

    }

    @Test
    fun testDisconnectNoConnection() = runBlocking {
        assertFalse(r1!!.disconnect())
        assertFalse(r1!!.isConnected)
        assertFalse(r2!!.disconnect())
        assertFalse(r2!!.isConnected)
        assertFalse(r3!!.disconnect())
        assertFalse(r3!!.isConnected)
        assertFalse(r4!!.disconnect())
        assertFalse(r4!!.isConnected)
    }


    @Test
    fun testSend() = runBlocking {
        assertTrue(r1!!.connect())
        assertTrue(
            r1!!.send(
                Json.createObjectBuilder().add("test", "value")
                    .build().toString()
            )
        )
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(1, DummyHandler.latest!!.size)
        assertTrue(DummyHandler.latest!!.containsKey("test"))
        assertEquals("value", DummyHandler.latest!!.getString("test"))
    }

    @Test
    fun testSendNoConnection() {
        assertFalse(r1!!.send(Json.createObjectBuilder().build().toString()))
        assertFalse(r2!!.send(Json.createObjectBuilder().build().toString()))
        assertFalse(r3!!.send(Json.createObjectBuilder().build().toString()))
        assertFalse(r4!!.send(Json.createObjectBuilder().build().toString()))
        assertNull(DummyHandler.latest)
    }

    @Test
    fun testAuthenticate() = runBlocking {
        assertTrue(r1!!.connect())
        r1!!.authenticate("test1", "test2", "test3", "test4", 5, "test5", 10)
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(8, DummyHandler.latest!!.size)
        assertEquals(
            JRosbridge.OP_CODE_AUTH,
            DummyHandler.latest!!.getString(JRosbridge.FIELD_OP)
        )
        assertEquals(
            "test1",
            DummyHandler.latest!!.getString(JRosbridge.FIELD_MAC)
        )
        assertEquals(
            "test2",
            DummyHandler.latest!!.getString(JRosbridge.FIELD_CLIENT)
        )
        assertEquals(
            "test3",
            DummyHandler.latest!!
                .getString(JRosbridge.FIELD_DESTINATION)
        )
        assertEquals(
            "test4",
            DummyHandler.latest!!.getString(JRosbridge.FIELD_RAND)
        )
        assertEquals(5, DummyHandler.latest!!.getInt(JRosbridge.FIELD_TIME))
        assertEquals(
            "test5",
            DummyHandler.latest!!.getString(JRosbridge.FIELD_LEVEL)
        )
        assertEquals(
            10,
            DummyHandler.latest!!.getInt(JRosbridge.FIELD_END_TIME)
        )
    }

    @Test
    fun testOnMessageInvalidOpCode() = runBlocking {
        assertTrue(r1!!.connect())
        r1!!.onMessage("{\"" + JRosbridge.FIELD_OP + "\":\"invalid\"}")
    }

    @Test
    fun testOnMessageInvalidPngData() = runBlocking {
        assertTrue(r1!!.connect())
        r1!!.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_PNG + "\"}"
        )
        Thread.yield()
        assertNull(DummyHandler.latest)
        r1!!.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_PNG + "\",\"" + JRosbridge.FIELD_DATA
                    + "\":\"iVBORw0KGgoAAAANSUhEUgAAAAoAAAAKCAMAAAC67D+PAAAAGXR"
                    + "FWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAGBQTFRF///"
                    + "/AGb/AGbMmcz/M5nMZpnM////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                    + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
                    + "AAAAAAAAAAAAA7feQVwAAAAd0Uk5T////////ABpLA0YAAAA6SURBVHj"
                    + "aJMtBDgBABARBs4P/P3kbfZCKEE3aAmUFLVu5fCQfGQ7nciTV0GW9zp4"
                    + "Ds+B5SMcLfgEGADSKAPVZzedhAAAAAElFTkSuQmCC\"}"
        )
        Thread.yield()
        assertNull(DummyHandler.latest)
    }


}