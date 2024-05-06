/**
 * The JRosbridge class contains constant definitions used in the rosbridge
 * protocol itself (e.g., op code types).
 *
 * @author Russell Toris - russell.toris@gmail.com
 * @version April 1, 2014
 */
object JRosbridge {
    /**
     * The args field for the rosbridge protocol.
     */
    const val FIELD_ARGS = "args"

    /**
     * The client field for the rosbridge protocol.
     */
    const val FIELD_CLIENT = "client"

    /**
     * The compression field for the rosbridge protocol.
     */
    const val FIELD_COMPRESSION = "compression"

    /**
     * The data field for the rosbridge protocol.
     */
    const val FIELD_DATA = "data"

    /**
     * The destination field for the rosbridge protocol.
     */
    const val FIELD_DESTINATION = "dest"

    /**
     * The end time field for the rosbridge protocol.
     */
    const val FIELD_END_TIME = "end"

    /**
     * The ID field for the rosbridge protocol.
     */
    const val FIELD_ID = "id"

    /**
     * The user level field for the rosbridge protocol.
     */
    const val FIELD_LEVEL = "level"

    /**
     * The MAC field for the rosbridge protocol.
     */
    const val FIELD_MAC = "mac"

    /**
     * The message data field for the rosbridge protocol.
     */
    const val FIELD_MESSAGE = "msg"

    /**
     * The op code field for the rosbridge protocol.
     */
    const val FIELD_OP = "op"

    /**
     * The random field for the rosbridge protocol.
     */
    const val FIELD_RAND = "rand"

    /**
     * The result field for the rosbridge protocol.
     */
    const val FIELD_RESULT = "result"

    /**
     * The service field for the rosbridge protocol.
     */
    const val FIELD_SERVICE = "service"

    /**
     * The throttle rate field for the rosbridge protocol.
     */
    const val FIELD_THROTTLE_RATE = "throttle_rate"

    /**
     * The time field for the rosbridge protocol.
     */
    const val FIELD_TIME = "t"

    /**
     * The topic field for the rosbridge protocol.
     */
    const val FIELD_TOPIC = "topic"

    /**
     * The message/service type field for the rosbridge protocol.
     */
    const val FIELD_TYPE = "type"

    /**
     * The values field for the rosbridge protocol.
     */
    const val FIELD_VALUES = "values"

    /**
     * The advertise op code for the rosbridge protocol.
     */
    const val OP_CODE_ADVERTISE = "advertise"

    /**
     * The advertise service op code for the rosbridge protocol.
     */
    const val OP_CODE_ADVERTISE_SERVICE = "advertise_service"

    /**
     * The unadvertise service op code for the rosbridge protocol.
     */
    const val OP_CODE_UNADVERTISE_SERVICE = "unadvertise_service"

    /**
     * The authenticate op code for the rosbridge protocol.
     */
    const val OP_CODE_AUTH = "auth"

    /**
     * The call service op code for the rosbridge protocol.
     */
    const val OP_CODE_CALL_SERVICE = "call_service"

    /**
     * The png compression op code for the rosbridge protocol.
     */
    const val OP_CODE_PNG = "png"

    /**
     * The publish op code for the rosbridge protocol.
     */
    const val OP_CODE_PUBLISH = "publish"

    /**
     * The service response op code for the rosbridge protocol.
     */
    const val OP_CODE_SERVICE_RESPONSE = "service_response"

    /**
     * The subscribe op code for the rosbridge protocol.
     */
    const val OP_CODE_SUBSCRIBE = "subscribe"

    /**
     * The unadvertise op code for the rosbridge protocol.
     */
    const val OP_CODE_UNADVERTISE = "unadvertise"

    /**
     * The unsubscribe op code for the rosbridge protocol.
     */
    const val OP_CODE_UNSUBSCRIBE = "unsubscribe"



}