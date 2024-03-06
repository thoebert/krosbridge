import com.github.thoebert.krosbridge.Ros
import com.github.thoebert.krosbridge.Service
import com.github.thoebert.krosbridge.ServiceRequest
import com.github.thoebert.krosbridge.ServiceResponse
import jakarta.json.Json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import org.junit.Assert.assertEquals

import java.util.*
import kotlin.test.*

@Serializable
data class MyTypeRequest(val test1 : String) : ServiceRequest()

@Serializable
data class MyTypeResponse(val test3 : String) : ServiceResponse()


class TestService {
    private var ros: Ros? = null
    private var server: DummyServer? = null
    private var s1: Service? = null
    @BeforeTest
    fun setUp() = runBlocking {
        ros = Ros()
        server = DummyServer(ros!!.port)
        server!!.start()
        ros!!.connect()
        s1 = Service(ros!!, "myService", "myType", MyTypeRequest::class, MyTypeResponse::class)
    }

    @AfterTest
    fun tearDown() = runBlocking {
        ros!!.disconnect()
        server!!.stop()
        DummyHandler.latest = null
    }

    @Test
    fun testRosStringAndStringConstructor() {
        assertEquals(ros, s1!!.ros)
        assertEquals("myService", s1!!.name)
        assertEquals("myType", s1!!.type)
        assertFalse(s1!!.isAdvertised)
    }

    @Test
    fun testCallService() {
        var latestResponse : ServiceResponse? = null
        val req = MyTypeRequest("test2")
        s1!!.callService(req) { res, _, _ -> latestResponse = res }
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(5, DummyHandler.latest!!.size)
        assertEquals(
            JRosbridge.OP_CODE_CALL_SERVICE,
            DummyHandler.latest!!.getString(JRosbridge.FIELD_OP)
        )
        assertEquals("call_service:myService:0",
            DummyHandler.latest!!.getString(JRosbridge.FIELD_ID)
        )
        assertEquals("myType",
            DummyHandler.latest!!.getString(JRosbridge.FIELD_TYPE)
        )
        assertEquals("myService",
            DummyHandler.latest!!.getString(JRosbridge.FIELD_SERVICE)
        )
        assertEquals(
            "{\"test1\":\"test2\"}", DummyHandler.latest!!
                .getJsonObject(JRosbridge.FIELD_ARGS).toString()
        )
        val toSend =
                Json.createObjectBuilder()
                    .add(JRosbridge.FIELD_OP, JRosbridge.OP_CODE_SERVICE_RESPONSE)
                    .add(JRosbridge.FIELD_SERVICE,"myService")
                    .add(JRosbridge.FIELD_ID,"call_service:myService:0")
                    .add(JRosbridge.FIELD_RESULT, false)
                    .add(
                        JRosbridge.FIELD_VALUES,
                        Json.createObjectBuilder().add("test3", "test4").build()
                    )
                    .build().toString()
        ros!!.onMessage(toSend)
        while (latestResponse == null) Thread.yield()
        assertNotNull(latestResponse)
        assertEquals(MyTypeResponse("test4"), latestResponse)
    }


    @Test
    fun testCallServiceAndWait() = runBlocking {
        val req = MyTypeRequest("test2")
        val timer = Timer()
        timer.schedule(SendServiceResponse(ros), 300)
        val resp: ServiceResponse? = s1?.callService(req)?.first
        assertNotNull(resp)
        assertEquals(MyTypeResponse("test4"), resp)
    }


    @Test
    fun testAdvertiseService() {
        var latestRequest : ServiceRequest? = null
        s1!!.advertiseServiceGeneric { req, _ -> latestRequest = req }
        assertNull(latestRequest)
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(
            """{"op":"advertise_service","service":"myService","type":"myType"}""",
            DummyHandler.latest.toString()
        )
        assertTrue(s1!!.isAdvertised)
    }
    @Test
    fun testUnadvertiseService() {
        s1!!.unadvertiseService()
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(
            "{\"op\":\"unadvertise_service\",\"service\":\"myService\"}",
            DummyHandler.latest.toString()
        )
        assertFalse(s1!!.isAdvertised)
    }

    @Test
    fun testSendResponse() {
        val resp = MyTypeResponse("test4")
        s1!!.sendResponseGeneric(resp, true, "myServiceId")
        while (DummyHandler.latest == null) Thread.yield()
        assertNotNull(DummyHandler.latest)
        assertEquals(
            "{" +
                    "\"op\":\"service_response\"," +
                    "\"service\":\"myService\"," +
                    "\"values\":{\"test3\":\"test4\"}," +
                    "\"result\":true," +
                    "\"id\":\"myServiceId\"" +
                    "}",
            DummyHandler.latest.toString()
        )
    }

    @Test
    fun testOnMessageNoServiceCallbacks() = runBlocking {
        assertTrue(ros!!.connect())
        ros!!.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_SERVICE_RESPONSE + "\",\""
                    + JRosbridge.FIELD_ID + "\":\"id123\",\""
                    + JRosbridge.FIELD_RESULT + "\":true,\""
                    + JRosbridge.FIELD_MESSAGE + "\":{\"test1\":\"test2\"}}"
        )
        Thread.yield()
        assertNull(DummyHandler.latest)
    }

    @Test
    fun testOnMessageServiceCallback() {
        var latestResponse : ServiceResponse? = null
        var latestResult : Boolean = true
        val req = MyTypeRequest("test2")
        s1!!.callService(req) { res, r, _ ->
            latestResponse = res
            latestResult = r
        }
        assertNull(latestResponse)
        ros!!.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_SERVICE_RESPONSE + "\",\""
                    + JRosbridge.FIELD_ID + "\":\"call_service:myService:0\",\""
                    + JRosbridge.FIELD_SERVICE + "\":\"myService\",\""
                    + JRosbridge.FIELD_RESULT + "\":false,\""
                    + JRosbridge.FIELD_VALUES + "\":{\"test3\":\"test4\"}}"
        )
        while (latestResponse == null) Thread.yield()
        assertNotNull(latestResponse)
        assertEquals(MyTypeResponse("test4"), latestResponse)
        assertFalse(latestResult)
    }


    @Test
    fun testOnMessageServiceCallbackNoResult() {
        var latestResponse : ServiceResponse? = null
        var latestResult : Boolean = false
        val req = MyTypeRequest("test2")
        s1!!.callService(req) { res, r, _ ->
            latestResponse = res
            latestResult = r
        }
        assertNull(latestResponse)
        ros!!.onMessage(
            "{\"" + JRosbridge.FIELD_OP + "\":\""
                    + JRosbridge.OP_CODE_SERVICE_RESPONSE + "\",\""
                    + JRosbridge.FIELD_ID + "\":\"call_service:myService:0\",\""
                    + JRosbridge.FIELD_SERVICE + "\":\"myService\",\""
                    + JRosbridge.FIELD_VALUES + "\":{\"test3\":\"test4\"}}"
        )
        while (latestResponse == null) Thread.yield()
        assertNotNull(latestResponse)
        assertEquals(MyTypeResponse("test4"), latestResponse)
        assertTrue(latestResult)
    }

    private inner class SendServiceResponse(private val ros: Ros?) : TimerTask() {
        override fun run() {
            val toSend =
                    Json.createObjectBuilder()
                        .add(JRosbridge.FIELD_OP, JRosbridge.OP_CODE_SERVICE_RESPONSE)
                        .add(JRosbridge.FIELD_ID, "call_service:myService:0")
                        .add(JRosbridge.FIELD_SERVICE, "myService")
                        .add(JRosbridge.FIELD_RESULT, false)
                        .add(
                            JRosbridge.FIELD_VALUES, Json.createObjectBuilder()
                                .add("test3", "test4")
                                .build()
                        ).build()
                        .toString()
            ros!!.onMessage(toSend)
        }
    }

}