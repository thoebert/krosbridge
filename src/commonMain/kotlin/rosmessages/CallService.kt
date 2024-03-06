package com.github.thoebert.krosbridge.rosmessages

import com.github.thoebert.krosbridge.Ros
import com.github.thoebert.krosbridge.ServiceRequest
import kotlinx.serialization.Serializable

/*
{ "op": "call_service",
  (optional) "id": <string>,
  "service": <string>,
  (optional) "args": <list<json>>,
  (optional) "fragment_size": <int>,
  (optional) "compression": <string>
}
 */

@Serializable
data class CallService (
    val service: String,
    override val id: String? = null,
    @Serializable(with = Ros.ServiceRequestSerializer::class)
    val args: ServiceRequest? = null,
    val type: String? = null,
    val fragment_size: Int? = null,
    val compression: String? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "call_service"
    }
}