package com.github.thoebert.krosbridge.rosmessages

import kotlinx.serialization.Serializable

/*
{ "op": "subscribe",
  (optional) "id": <string>,
  "topic": <string>,
  (optional) "type": <string>,
  (optional) "throttle_rate": <int>,
  (optional) "queue_length": <int>,
  (optional) "fragment_size": <int>,
  (optional) "compression": <string>
}
 */

@Serializable
data class Subscribe (
    val topic: String,
    override val id: String? = null,
    val type: String? = null,
    val throttle_rate: Int? = null,
    val queue_length: Int? = null,
    val fragment_size: Int? = null,
    val compression: String? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "subscribe"
    }
}