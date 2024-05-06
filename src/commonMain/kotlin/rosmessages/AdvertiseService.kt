package com.github.thoebert.krosbridge.rosmessages

import kotlinx.serialization.Serializable

/*
{ "op": "advertise_service",
  "type": <string>,
  "service": <string>
}
 */

@Serializable
data class AdvertiseService  (
    val service: String,
    val type: String,
    override val id: String? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "advertise_service"
    }
}