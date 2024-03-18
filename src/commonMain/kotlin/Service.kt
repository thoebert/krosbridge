package com.github.thoebert.krosbridge

import com.github.thoebert.krosbridge.rosmessages.*
import kotlin.reflect.KClass

typealias ServiceRequestSubscriber = (ServiceRequest?, String?) -> Unit
typealias ServiceResponseSubscriber = (ServiceResponse?, Boolean, String?) -> Unit


/**
 * The Service object is responsible for calling or advertising a service in ROS.
 *
 * @param ros
 * A handle to the ROS connection.
 * @param name
 * The name of the service (e.g., "/add_two_ints").
 * @param type
 * The service type (e.g., "rospy_tutorials/AddTwoInts").
 *
 * @author Russell Toris - russell.toris@gmail.com
 * @author Timon Hoebert - timon.hoebert@gmx.at
 */
open class Service(
    open val ros: Ros,
    open val name: String,
    open val type: String,
    open val requestClz: KClass<out ServiceRequest>,
    open val responseClz: KClass<out ServiceResponse>,
) {

    /**
     * Check if the current service is advertising to ROS.
     *
     * @return If the current service is advertising to ROS.
     */
    val isAdvertised: Boolean
        get() = requestSubscriber != null

    private var requestSubscriber : ServiceRequestSubscriber? = null
    private val responseSubscribers = mutableMapOf<String, ServiceResponseSubscriber>()

    /**
     * Call this service. The callback function will be called with the
     * associated service response.
     *
     * @param request
     * The service request to send.
     * @param cb
     * The callback used when the associated response comes back.
     */
    suspend fun callService(request: ServiceRequest, callback: ServiceResponseSubscriber) {
        val callServceId = ("call_service:" + name + ":" + ros.nextId()) // construct the unique ID
        responseSubscribers[callServceId] = callback
        ros.registerService(this)
        ros.send(CallService(name, callServceId, request, type))
    }

    internal fun receivedResponse(response: ServiceResponse?, result : Boolean, id : String?) {
        responseSubscribers[id]?.let { it(response, result, id) }
        ros.deregisterService(this)
        responseSubscribers.remove(id)
    }


    /**
     * Registers as service advertiser.
     */
    suspend fun advertiseServiceGeneric(callback: ServiceRequestSubscriber?) {
        requestSubscriber = callback
        ros.registerService(this) // register the callback
        ros.send(AdvertiseService(name, type)) // build and send the rosbridge call
    }

    /**
     * Unregisters as service advertiser.
     */
    suspend fun unadvertiseService() {
        ros.deregisterService(this)
        ros.send(UnadvertiseService(name)) // build and send the rosbridge call
        requestSubscriber = null
    }


    fun receivedRequest(request: ServiceRequest?, id : String?) {
        requestSubscriber?.let { it(request, id) }
    }

    /**
     * Send a service response.
     *
     * @param response
     * The service response to send.
     * @param id
     * The ID of the response (matching that of the service call).
     */
    suspend fun sendResponseGeneric(response: ServiceResponse?, result : Boolean = true, id: String? = null) {
        ros.send(ResponseService(name, response, result, id))
    }


    /**
     * Call the service and gets the response later. For an
     * asynchronous version of this call, see the
     * [ callService][.callService] method.
     *
     * @param request
     * The service request to send.
     * @return The corresponding service response from ROS.
     */
    suspend fun callService(request: ServiceRequest): Pair<ServiceResponse?, Boolean> {
        return TODO("implement in kmp") /*suspendCoroutine { continuation ->
            callService(request) { response, result, _ ->
                continuation.resume(response to result)
            }
        }*/
    }

}

open class GenericService<In : ServiceRequest, Out : ServiceResponse>(
    override val ros: Ros,
    override val name: String,
    override val type: String,
    override val requestClz: KClass<out ServiceRequest>,
    override val responseClz: KClass<out ServiceResponse>,
) : Service(ros, name, type, requestClz, responseClz){
    suspend fun call(input : In) : Pair<Out?, Boolean> {
        val (resp, result) = super.callService(input)
        val respCasted = resp as Out?
        return respCasted to result
    }

    suspend fun advertiseService(callback: (In?, String?) -> Unit) {
        return super.advertiseServiceGeneric { m, id ->
            callback(m as In?, id)
        }
    }

    suspend fun sendResponse(response: Out?, result : Boolean = true, id: String? = null) {
        super.sendResponseGeneric(response, result, id)
    }
}