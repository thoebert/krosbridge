package com.github.thoebert.krosbridge.rosmessages

import kotlinx.serialization.Serializable

/*
{ "op": "advertise",
  (optional) "id": <string>,
  "topic": <string>,
  "type": <string>
}
 */

@Serializable
data class Advertise  (
    val topic: String,
    val type: String,
    override val id: String? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "advertise"
    }
}