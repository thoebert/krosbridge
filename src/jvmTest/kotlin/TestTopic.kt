import com.github.thoebert.krosbridge.Message
import com.github.thoebert.krosbridge.Ros
import com.github.thoebert.krosbridge.Topic
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import kotlin.test.*


@Serializable
data class MyType1(val test1 : String) : Message()

class TestTopic {
    private lateinit var ros: Ros
    private lateinit var server: DummyServer
    private lateinit var t1: Topic
    private lateinit var t2: Topic
    private lateinit var t3: Topic
    private lateinit var t4: Topic
    private lateinit var t5: Topic
    @BeforeTest
    fun setUp() = runBlocking {
        ros = Ros()
        server = DummyServer(ros.port)
        server.start()
        ros.connect()

        t1 = Topic(ros, "myTopic1", "myType1", MyType1::class)
        t2 = Topic(
            ros, "myTopic2", "myType2",
            MyType1::class,
            Topic.CompressionType.png,
        )
        t3 = Topic(ros, "myTopic3", "myType3", MyType1::class, Topic.CompressionType.none, 10)
        t4 = Topic(
            ros, "myTopic4",
            "myType4",
            MyType1::class,
            Topic.CompressionType.png, 20
        )
        t5 = Topic(ros, "myTopic", "myType1", MyType1::class)
    }

    @AfterTest
    fun tearDown() = runBlocking {
        ros.disconnect()
        server.stop()
        DummyHandler.latest = null
    }

    @Test
    fun testRosStringAndStringConstructor() {
        assertEquals(ros, t1.ros)
        assertEquals("myTopic1", t1.name)
        assertEquals("myType1", t1.type)
        assertFalse(t1.isAdvertised)
        assertFalse(t1.isSubscribed)
        assertEquals(Topic.CompressionType.none, t1.compression)
        assertEquals(0, t1.throttleRate)
    }

    @Test
    fun testRosStringStringAndCompressionTypeConstructor() {
        assertEquals(ros, t2.ros)
        assertEquals("myTopic2", t2.name)
        assertEquals("myType2", t2.type)
        assertFalse(t2.isAdvertised)
        assertFalse(t2.isSubscribed)
        assertEquals(Topic.CompressionType.png, t2.compression)
        assertEquals(0, t2.throttleRate)
    }

    @Test
    fun testRosStringStringAndIntConstructor() {
        assertEquals(ros, t3.ros)
        assertEquals("myTopic3", t3.name)
        assertEquals("myType3", t3.type)
        assertFalse(t3.isAdvertised)
        assertFalse(t3.isSubscribed)
        assertEquals(Topic.CompressionType.none, t3.compression)
        assertEquals(10, t3.throttleRate)
    }

    @Test
    fun testRosStringStringCompressionTypeAndIntConstructor() {
        assertEquals(ros, t4.ros)
        assertEquals("myTopic4", t4.name)
        assertEquals("myType4", t4.type)
        assertFalse(t4.isAdvertised)
        assertFalse(t4.isSubscribed)
        assertEquals(Topic.CompressionType.png, t4.compression)
        assertEquals(20, t4.throttleRate)
    }

    @Test
    fun testSubscribe() = runBlocking {
        var latestMessage : Message? = null
        t1.subscribeGeneric (this) { msg, _ -> latestMessage = msg }

        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(
            """{"op":"subscribe","topic":"myTopic1","id":"subscribe:myTopic1:0","type":"myType1","throttle_rate":0,"compression":"none"}""",
                        DummyHandler.latest.toString()
        )
        DummyHandler.latest = null
        ros.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\"" + JRosbridge.OP_CODE_PUBLISH + "\",\""
                    + JRosbridge.FIELD_TOPIC + "\":\"myTopic1\",\""
                    + JRosbridge.FIELD_MESSAGE + "\":{\"test1\":\"test2\"}}"
        )
        assertNotNull(latestMessage)
        assertEquals(MyType1("test2"), latestMessage)
        assertFalse(t1.isAdvertised)
        assertTrue(t1.isSubscribed)
    }

    @Test
    fun testUnsubscribe() = runBlocking {
        var latestMessage : Message? = null
        t1.subscribeGeneric (this) { msg, _ -> latestMessage = msg }
        while (DummyHandler.latest == null) Thread.yield()
        assertFalse(t1.isAdvertised)
        assertTrue(t1.isSubscribed)
        assertNull(latestMessage)

        DummyHandler.latest = null
        t1.unsubscribe(this)
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(
            """{"op":"unsubscribe","topic":"myTopic1","id":"subscribe:myTopic1:0"}""",
            DummyHandler.latest.toString()
        )

        DummyHandler.latest = null
        ros.send("{\"" + JRosbridge.FIELD_OP + "\":\"" + JRosbridge.OP_CODE_PUBLISH + "\",\""
                + JRosbridge.FIELD_TOPIC + "\":\"myTopic1\",\""
                + JRosbridge.FIELD_MESSAGE + "\":{\"test1\":\"test2\"}}"
        )
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertNull(latestMessage)
        assertFalse(t1.isAdvertised)
        assertFalse(t1.isSubscribed)
    }

    @Test
    fun testUnsubscribeNoSubscribe() {
        t1.unsubscribe ("cb1")
        assertNull(DummyHandler.latest)
        assertFalse(t1.isAdvertised)
        assertFalse(t1.isSubscribed)
    }

    @Test
    fun testAdvertise() {
        t1.advertise()
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(
            """{"op":"advertise","topic":"myTopic1","type":"myType1","id":"advertise:myTopic1:0"}""",
            DummyHandler.latest.toString()
        )
        assertTrue(t1.isAdvertised)
        assertFalse(t1.isSubscribed)
    }

    @Test
    fun testUnadvertise() {
        t1.advertise()
        while (DummyHandler.latest == null) Thread.yield()
        DummyHandler.latest = null
        t1.unadvertise()
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(
            """{"op":"unadvertise","topic":"myTopic1","id":"advertise:myTopic1:0"}""",
            DummyHandler.latest.toString()
        )
        assertFalse(t1.isAdvertised)
        assertFalse(t1.isSubscribed)
    }
    
    @Test
    fun testImpl(){
        val t1 = Topic(ros, "ServiceName", "MyType1", MyType1::class)
        t1.publishGeneric(MyType1("p1"))
    }


    @Test
    fun testPublish() {
        t1.advertise()
        while (DummyHandler.latest == null) Thread.yield()
        DummyHandler.latest = null
        t1.publishGeneric(MyType1("test2"))
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(
            """{"op":"publish","topic":"myTopic1","msg":{"test1":"test2"},"id":"publish:myTopic1:1"}""",
            DummyHandler.latest.toString()
        )
        assertTrue(t1.isAdvertised)
        assertFalse(t1.isSubscribed)
    }

    @Test
    fun testOnMessagePngData() = runBlocking {
        assertTrue(ros.connect())
        var latestMessage : Message? = null
        t5.subscribeGeneric (this) { msg, _ -> latestMessage = msg }
        while (DummyHandler.latest == null) Thread.yield()
        DummyHandler.latest = null
        assertNull(latestMessage)
        ros.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_PNG + "\",\"" + JRosbridge.FIELD_DATA
                    + "\":\"iVBORw0KGgoAAAANSUhEUgAAAAQAAAAFCAIAAADtz9qMAAAATEl"
                    + "EQVR4nAFBAL7/AXsib/UAy7JOO0D89AL4RrO8ADpNAPQBttECrwVXKE3"
                    + "8+vO5yQAzBFH6qccUACD2UUgPrwLHu1Ir+FK+vQoJ2ejGjx3lsrwJjwA"
                    + "AAABJRU5ErkJggg==\"}"
        )
        assertNull(DummyHandler.latest)
        assertNotNull(latestMessage)
        assertEquals(MyType1("test2"), latestMessage)
    }


    @Test
    fun testOnMessageNoTopicCallbacks() = runBlocking {
        assertTrue(ros.connect())
        ros.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_PUBLISH + "\",\"" + JRosbridge.FIELD_TOPIC
                    + "\":\"myTopic\",\"" + JRosbridge.FIELD_MESSAGE
                    + "\":{\"test1\":\"test2\"}}"
        )
        assertNull(DummyHandler.latest)
    }


    @Test
    fun testOnMessageMultiTopicCallbacks() = runBlocking {
        assertTrue(ros.connect())
        var latestMessage1 : Message? = null
        var latestMessage2 : Message? = null
        t1.subscribeGeneric ("cb1") { msg, _ -> latestMessage1 = msg }
        while (DummyHandler.latest == null) Thread.yield()
        DummyHandler.latest = null
        t1.subscribeGeneric ("cb2") { msg, _ -> latestMessage2 = msg }
        assertNull(latestMessage1)
        assertNull(latestMessage2)
        ros!!.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_PUBLISH + "\",\"" + JRosbridge.FIELD_TOPIC
                    + "\":\"myTopic1\",\"" + JRosbridge.FIELD_MESSAGE
                    + "\":{\"test1\":\"test2\"}}"
        )
        assertNull(DummyHandler.latest)
        assertNotNull(latestMessage1)
        assertNotNull(latestMessage2)
        assertEquals(MyType1("test2"), latestMessage1)
        assertEquals(MyType1("test2"), latestMessage2)
    }


    @Test
    fun testDeregisterTopicCallback() = runBlocking {
        assertTrue(ros.connect())
        var latestMessage1 : Message? = null
        var latestMessage2 : Message? = null
        t1.subscribeGeneric ("cb1") { msg, _ -> latestMessage1 = msg }
        while (DummyHandler.latest == null) Thread.yield()
        DummyHandler.latest = null
        t1.subscribeGeneric ("cb2") { msg, _ -> latestMessage2 = msg }
        assertNull(latestMessage1)
        assertNull(latestMessage2)
        t1.unsubscribe("cb1")
        ros!!.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_PUBLISH + "\",\"" + JRosbridge.FIELD_TOPIC
                    + "\":\"myTopic1\",\"" + JRosbridge.FIELD_MESSAGE
                    + "\":{\"test1\":\"test2\"}}"
        )
        Thread.yield()
        assertNull(DummyHandler.latest)
        assertNull(latestMessage1)
        assertNotNull(latestMessage2)
        assertEquals(MyType1("test2"), latestMessage2)
    }


    @Test
    fun testDeregisterTopicCallbackAll() = runBlocking {
        assertTrue(ros.connect())
        var latestMessage1 : Message? = null
        var latestMessage2 : Message? = null
        t1.subscribeGeneric ("cb1") { msg, _ -> latestMessage1 = msg }
        while (DummyHandler.latest == null) Thread.yield()
        DummyHandler.latest = null
        t1.subscribeGeneric ("cb2") { msg, _ -> latestMessage2 = msg }
        assertNull(latestMessage1)
        assertNull(latestMessage2)
        t1.unsubscribe("cb1")
        t1.unsubscribe("cb2")
        while (DummyHandler.latest == null) Thread.yield()
        DummyHandler.latest = null
        ros.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_PUBLISH + "\",\"" + JRosbridge.FIELD_TOPIC
                    + "\":\"myTopic1\",\"" + JRosbridge.FIELD_MESSAGE
                    + "\":{\"test1\":\"test2\"}}"
        )
        Thread.yield()
        assertNull(DummyHandler.latest)
        assertNull(latestMessage1)
        assertNull(latestMessage2)
    }


    @Test
    fun testDeregisterTopicCallbackInvalidTopic() = runBlocking {
        assertTrue(ros.connect())
        var latestMessage1 : Message? = null
        var latestMessage2 : Message? = null
        t1.subscribeGeneric ("cb1") { msg, _ -> latestMessage1 = msg }
        while (DummyHandler.latest == null) Thread.yield()
        DummyHandler.latest = null
        t1.subscribeGeneric ("cb2") { msg, _ -> latestMessage2 = msg }
        assertNull(latestMessage1)
        assertNull(latestMessage2)
        t1.unsubscribe("cb3")
        t1.unsubscribe("cb4")
        DummyHandler.latest = null
        ros.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_PUBLISH + "\",\"" + JRosbridge.FIELD_TOPIC
                    + "\":\"myTopic1\",\"" + JRosbridge.FIELD_MESSAGE
                    + "\":{\"test1\":\"test2\"}}"
        )
        Thread.yield()
        assertNull(DummyHandler.latest)
        assertNotNull(latestMessage1)
        assertNotNull(latestMessage2)
        assertEquals(MyType1("test2"), latestMessage1)
        assertEquals(MyType1("test2"), latestMessage2)
    }
}