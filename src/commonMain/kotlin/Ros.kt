package com.github.thoebert.krosbridge

import com.github.thoebert.krosbridge.rosmessages.*
import io.github.aakira.napier.Napier
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.utils.io.charsets.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.core.EOFException
import io.ktor.utils.io.errors.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.reflect.KClass

/**
 * The Ros object is the main connection point to the rosbridge server. This
 * object manages all communication to-and-from ROS. Typically, this object is
 * not used on its own. Instead, helper classes, such as
 * [Topic][edu.wpi.rail.jrosbridge.JRosbridge.Topic], are used.
 *
 * @param hostname The hostname associated with this connection.
 * @param port The port associated with this connection.
 * @param protocol The WebSocket protocol to use.
 *
 * @author Russell Toris - russell.toris@gmail.com
 * @author Timon Hoebert - timon.hoebert@gmx.at
 */
class Ros(
    val hostname: String = DEFAULT_HOSTNAME,
    var port: Int = DEFAULT_PORT,
    val protocol: WebSocketType = WebSocketType.ws
) : JsonContentPolymorphicSerializer<ROSMessage>(ROSMessage::class) {

    // used throughout the library to create unique IDs for requests.
    private var idCounter: Long = 0

    // keeps track of callback functions for a given topic
    private val topicNames: MutableMap<String, Topic> = HashMap()

    // keeps track of callback functions for a given service request
    private val serviceNames: MutableMap<String, Service> = HashMap()


    private val logger = Napier


    private val client = HttpClient {
        install(WebSockets)
    }

    private var session: DefaultClientWebSocketSession? = null


    /**
     * The types of websocket protocols supported by jrosbridge and rosbridge.
     *
     */
    enum class WebSocketType {
        ws, wss
    }

    /**
     * Get the next unique ID number for this connection.
     *
     * @return The next unique ID number for this connection.
     */
    fun nextId(): Long {
        return idCounter++
    }


    /**
     * Attempt to establish a connection to rosbridge.
     *
     * @return Returns true if the connection was established successfully and
     * false otherwise.
     */
    suspend fun connect(): Boolean {
        val wsJob = Job()
        CoroutineScope(wsJob).launch {
            try {
                logger.i("Connecting to ${this@Ros.uRL}")
                this@Ros.session = client.webSocketSession(this@Ros.uRL)
                for (message in this@Ros.session!!.incoming) {
                    message as? Frame.Text ?: continue
                    onMessage(message.readText())
                }
            } catch (e: EOFException) {
                logger.d("Handling EOFException in Websocket coroutine: ", e)
            } catch (e: WebSocketException) {
                logger.d("Handling SocketException in Websocket coroutine: ", e)
            } catch (e: IllegalArgumentException) {
                logger.d("Handling IllegalArgumentException in Websocket coroutine: ", e)
            }
            wsJob.cancel()
        }
        while (wsJob.isActive && this.session == null) delay(25)
        if (wsJob.isCancelled) return false
        return true
    }

    /**
     * Disconnect the connection to rosbridge.
     *
     * @return Returns true if the disconnection was successful and false
     * otherwise.
     */
    suspend fun disconnect(): Boolean {
        if (isConnected) {
            logger.i("Disconnecting from ${this@Ros.uRL}")
            try {
                client.close()
                session?.let { it.close() }
                session = null
                return true
            } catch (e: IOException) {
                logger.e("Could not disconnect", e)
            }
        }
        return false // could not disconnect cleanly
    }

    /**
     * Indicates if there is a connection to rosbridge.
     */
    val isConnected: Boolean
        get() = session != null

    /**
     * The full URL this client is connecting to.
     */
    val uRL: String
        get() = "$protocol://$hostname:$port"

    fun topicByName(topicName: String): Topic? {
        val topic = topicNames[topicName]
        return if (topic != null) {
            topic
        } else {
            logger.e("Invalid topic name $topicName")
            null
        }
    }

    fun serviceByName(serviceName: String): Service? {
        val service = serviceNames[serviceName]
        return if (service != null) {
            service
        } else {
            logger.e("Invalid service name $serviceName")
            null
        }
    }


    abstract class GuidedSerializer<T : Any>(baseClass: KClass<T>) : JsonContentPolymorphicSerializer<T>(baseClass) {

        var serializer: DeserializationStrategy<out T>? = null

        override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out T> {
            if (serializer == null) throw IllegalArgumentException("No valid serialization type existing")
            return serializer!!
        }
    }

    object ServiceRequestSerializer : GuidedSerializer<ServiceRequest>(ServiceRequest::class)
    object ServiceResponseSerializer : GuidedSerializer<ServiceResponse>(ServiceResponse::class)
    object MessageSerializer : GuidedSerializer<Message>(Message::class)

    private fun getField(content: JsonElement, field: String): String? {
        return content.jsonObject[field]?.jsonPrimitive?.content
    }

    @OptIn(InternalSerializationApi::class)
    override fun selectDeserializer(content: JsonElement): KSerializer<out ROSMessage> {
        val op = getField(content, "op")
        when (op) {
            Publish.OPERATION -> {
                getField(content, "topic")?.let { topicName ->
                    topicByName(topicName)?.let { MessageSerializer.serializer = it.clz.serializer() }
                }
            }

            CallService.OPERATION -> {
                getField(content, "service")?.let { serviceName ->
                    serviceByName(serviceName)?.let { ServiceRequestSerializer.serializer = it.requestClz.serializer() }
                }
            }

            ResponseService.OPERATION -> {
                getField(content, "service")?.let { serviceName ->
                    serviceByName(serviceName)?.let {
                        ServiceResponseSerializer.serializer = it.responseClz.serializer()
                    }
                }
            }
        }
        return when (op) {
            Advertise.OPERATION -> Advertise.serializer()
            AdvertiseService.OPERATION -> AdvertiseService.serializer()
            Authenticate.OPERATION -> Authenticate.serializer()
            CallService.OPERATION -> CallService.serializer()
            Fragmentation.OPERATION -> Fragmentation.serializer()
            PNGCompression.OPERATION -> PNGCompression.serializer()
            Publish.OPERATION -> Publish.serializer()
            ResponseService.OPERATION -> ResponseService.serializer()
            Subscribe.OPERATION -> Subscribe.serializer()
            Unadvertise.OPERATION -> Unadvertise.serializer()
            UnadvertiseService.OPERATION -> UnadvertiseService.serializer()
            Unsubscribe.OPERATION -> Unsubscribe.serializer()
            else -> throw IllegalArgumentException("Coult not find op named $op")
        }
    }


    /**
     * This method is called once an entire message has been read in by the
     * connection from rosbridge. It will parse the incoming JSON and attempt to
     * handle the request appropriately.
     *
     * @param message
     * The incoming JSON message from rosbridge.
     */
    @OptIn(ExperimentalEncodingApi::class)
    fun onMessage(message: String) {
        if (message.isEmpty()) return
        try {
            logger.d("Received message $message")
            val rosmsg = Json.decodeFromString(this, message)

            if (rosmsg is PNGCompression) {
                val data: String = rosmsg.data
                // decompress the PNG data
                // check for compression
                val bytes: ByteArray = Base64.decode(data.toByteArray())
                val image = Image.makeFromEncoded(bytes)
                val bitmap = Bitmap.makeFromImage(image)
                val rawData = bitmap.readPixels()
                println(
                    rawData?.toList()?.windowed(image.imageInfo.width, image.imageInfo.width)
                        ?.map {
                            it.filter { it.toInt() != -1 }.reversed()
                                .joinToString("") { it.toInt().toChar().toString() }
                        }?.joinToString(""),
                )

                if (rawData != null) {
                    val buffer = StringBuilder()
                    for (i in rawData.indices) buffer.append(rawData[i].toInt().toChar())
                    val newJsonObject = Json.decodeFromString(
                        this@Ros,
                        rawData.toList().windowed(image.imageInfo.width, image.imageInfo.width).joinToString("") {
                            it.filter { it.toInt() != -1 }.reversed()
                                .joinToString("") { it.toInt().toChar().toString() }
                        }
                    )
                    handleMessage(newJsonObject)
                }
            } else {
                handleMessage(rosmsg)
            }
        } catch (e: IOException) {
            logger.e("Invalid incoming message $message", e)
        } catch (e: IllegalArgumentException) {
            logger.i("Illegal argument for $message", e)
        } catch (e: SerializationException) {
            logger.e("Could not serialize message $message", e)
        }
    }

    /**
     * Handle the incoming rosbridge message by calling the appropriate
     * callbacks.
     *
     * @param rosmsg
     * The ROSMessage from the incoming rosbridge message.
     */
    private fun handleMessage(rosmsg: ROSMessage) {
        when (rosmsg) {
            is Publish ->
                topicByName(rosmsg.topic)?.let { it.receivedMessage(rosmsg.msg, rosmsg.id) }

            is ResponseService ->
                serviceByName(rosmsg.service)?.let { it.receivedResponse(rosmsg.values, rosmsg.result, rosmsg.id) }

            is CallService ->
                serviceByName(rosmsg.service)?.let { it.receivedRequest(rosmsg.args, rosmsg.id) }

            else ->
                logger.e("Unrecognized op code: $rosmsg")
        }
    }

    /**
     * Send the given JSON object to rosbridge.
     *
     * @param rosmsg
     * The JSON object to send to rosbridge.
     * @return If the sending of the message was successful.
     */
    suspend fun send(jsonString: String): Boolean {
        if (isConnected) { // check the connection
            try {
                logger.d("Sending message $jsonString")
                session!!.send(jsonString)
                return true
            } catch (e: IOException) {
                logger.e("Could not send message", e)
            }
        } else {
            logger.e("Could not send message because of disconnection")
        }
        return false // message send failed
    }

    /**
     * Send the given ROSMessage to rosbridge.
     *
     * @param rosmsg
     * The ROSMessage object to send to rosbridge.
     * @return If the sending of the message was successful.
     */
    suspend fun send(rosmsg: ROSMessage): Boolean {
        return send(Json.encodeToString(this, rosmsg))
    }


    /**
     * Sends an authorization request to the server.
     *
     * @param mac
     * The MAC (hash) string given by the trusted source.
     * @param client
     * The IP of the client.
     * @param dest
     * The IP of the destination.
     * @param rand
     * The random string given by the trusted source.
     * @param t
     * The time of the authorization request.
     * @param level
     * The user level as a string given by the client.
     * @param end
     * The end time of the client's session.
     */
    suspend fun authenticate(
        mac: String, client: String, dest: String,
        rand: String, t: Int, level: String, end: Int
    ) {
        // build and send the rosbridge call
        send(Authenticate(mac, client, dest, rand, t, level, end))
    }

    /**
     * Register a callback for a given topic.
     *
     * @param topic
     * The topic to register this callback with.
     * @param cb
     * The callback that will be called when messages come in for the
     * associated topic.
     */
    fun registerTopic(topic: Topic) {
        if (topic.name in topicNames) throw IllegalArgumentException("Duplicate Topic registration: ${topic.name}")
        topicNames[topic.name] = topic
    }


    /**
     * Deregister a topic
     *
     * @param topic
     * The topic associated with the callback.
     */
    fun deregisterTopic(topic: Topic) {
        topicNames.remove(topic.name)
    }

    /**
     * Register a callback for a given outgoing service call.
     *
     * @param serviceCallId
     * The unique ID of the service call.
     * @param cb
     * The callback that will be called when a service response comes
     * back for the associated request.
     */
    fun registerService(service: Service) {
        if (service.name in serviceNames) throw IllegalArgumentException("Duplicate Service registration: ${service.name}")
        serviceNames[service.name] = service
    }

    /**
     * Deregister a callback for a given incoming service request.
     *
     * @param serviceName
     * The unique name of the service call.
     */
    fun deregisterService(service: Service) {
        serviceNames.remove(service.name) // remove the callback
    }

    companion object {
        /**
         * The default hostname used if none is provided.
         */
        const val DEFAULT_HOSTNAME: String = "localhost"

        /**
         * The default port used if none is provided.
         */
        const val DEFAULT_PORT: Int = 9090
    }
}