package com.github.thoebert.krosbridge.rosmessages

import com.github.thoebert.krosbridge.Ros
import com.github.thoebert.krosbridge.ServiceResponse
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.Serializable

/*
{ "op": "service_response",
  (optional) "id": <string>,
  "service": <string>,
  (optional) "values": <list<json>>,
  "result": <boolean>
}
 */

@Serializable
data class ResponseService (
    val service: String,
    @Serializable(with = Ros.ServiceResponseSerializer::class)
    val values: ServiceResponse? = null,
    @EncodeDefault
    val result : Boolean = true,
    override val id: String? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "service_response"
    }
}