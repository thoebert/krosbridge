package com.github.thoebert.krosbridge.rosmessages

import kotlinx.serialization.Serializable

/*
{ "op": "unsubscribe",
  (optional) "id": <string>,
  "topic": <string>
}
 */

@Serializable
data class Unsubscribe  (
    val topic: String,
    override val id: String? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "unsubscribe"
    }
}