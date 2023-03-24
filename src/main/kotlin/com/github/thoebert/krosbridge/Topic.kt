package com.github.thoebert.krosbridge

import com.github.thoebert.krosbridge.rosmessages.*
import kotlin.reflect.KClass


typealias TopicSubscriber = (Message, String?) -> Unit

/**
 * The Topic object is responsible for publishing and/or subscribing to a topic
 * in ROS.
 * 
 * @param ros The ROS connection handle for this topic.
 * @param name The name of this topic.
 * @param type The message type of this topic.
 * @param compression The compression type for this topic.
 * @param throttleRate The throttle rate for this topic.
 *
 * @author Russell Toris - russell.toris@gmail.com
 * @author Timon Hoebert - timon.hoebert@gmx.at
 */
open class Topic(
    open val ros: Ros,
    open var name: String,
    open val type: String,
    open val clz: KClass<out Message>,
    val compression: CompressionType = CompressionType.none,
    val throttleRate: Int = 0
) {

    /**
     * The types of compression supported by jrosbridge and rosbridge.
     *
     * @author Russell Toris - russell.toris@gmail.com
     * @version April 1, 2014
     */
    enum class CompressionType {
        png, none
    }

    /**
     * Check if the current topic is advertising to ROS.
     * @return If the current topic is advertising to ROS.
     */
    val isAdvertised: Boolean
        get() = advertiseID != null

    /**
     * Check if the current topic is subscribed to ROS.
     * @return If the current topic is subscribed to ROS.
     */
    val isSubscribed: Boolean
        get() = subscriptionID != null

    private val subscribers = mutableMapOf<Any, TopicSubscriber>()

    private var subscriptionID: String? = null
    private var advertiseID: String? = null


    /**
     * Subscribe to this topic. A callback function is required and will be
     * called with any incoming message for this topic.
     *
     * @param callback
     * The callback that will be called when incoming messages are
     * received.
     */
    fun subscribeGeneric(handle : Any, callback: TopicSubscriber) : Boolean {
        if (handle in subscribers) return false
        if (subscribers.isEmpty()) startSubscription()
        subscribers[handle] = callback
        return true
    }

    private fun startSubscription(){
        ros.registerTopic(this)
        subscriptionID = "subscribe:" + name + ":" + ros.nextId()
        ros.send(Subscribe(name, subscriptionID, type, throttleRate, null, null, compression.toString()))
    }

    private fun endSubscription(){
        ros.deregisterTopic(this)
        ros.send(Unsubscribe(name, subscriptionID))
        subscriptionID = null
    }

    /**
     * Unregisters as a subscriber for the topic. Unsubscribing will remove all
     * the associated subscribe callbacks.
     */
    fun unsubscribe(handle : Any): Boolean {
        if (handle !in subscribers) return false
        this.subscribers.remove(handle)
        if (subscribers.isEmpty()) endSubscription()
        return true
    }

    internal fun receivedMessage(message : Message, id : String?){
        subscribers.forEach { (_, callback) -> callback(message, id) }
    }

    /**
     * Registers as a publisher for the topic. This call will be automatically
     * called by publish if you do not explicitly call it.
     */
    fun advertise() {
        advertiseID = "advertise:" + name + ":" + ros.nextId()
        ros.send(Advertise(name, type, advertiseID))
    }

    /**
     * Unregister as a publisher for the topic.
     */
    fun unadvertise() {
        ros.send(Unadvertise(name, advertiseID))
        advertiseID = null
    }

    /**
     * Publish the given message to ROS on this topic. If the topic is not
     * advertised, it will be advertised first.
     *
     * @param message
     * The message to publish.
     */
    fun publishGeneric(message: Message) {
        if (!isAdvertised) advertise() // check if we have advertised yet.
        val publishId = "publish:" + name + ":" + ros.nextId()
        ros.send(Publish(name, message, publishId))
    }
}

open class GenericTopic<M : Message>(
    override val ros: Ros,
    override var name: String,
    override val type: String,
    override val clz: KClass<out Message>,
) : Topic(ros, name, type, clz){

    fun subscribe(handle : Any, callback: (M, String?) -> Unit) : Boolean {
        return super.subscribeGeneric(handle) { m, id ->
            callback(m as M, id)
        }
    }

    fun publish(message : M) {
        super.publishGeneric(message)
    }
}