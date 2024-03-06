package com.github.thoebert.krosbridge.rosmessages

import com.github.thoebert.krosbridge.Message
import com.github.thoebert.krosbridge.Ros
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

/*
{ "op": "publish",
  (optional) "id": <string>,
  "topic": <string>,
  "msg": <json>
}
 */

@Serializable
data class Publish(
    val topic: String,
    @Contextual
    @Serializable(with = Ros.MessageSerializer::class)
    val msg: Message,
    override val id: String? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "publish"
    }
}