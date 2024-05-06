package com.github.thoebert.krosbridge.rosmessages

import kotlinx.serialization.Serializable

/*
{ "op": "unadvertise_service",
  "service": <string>
}
 */

@Serializable
data class UnadvertiseService  (
    val service: String,
    override val id: String? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "unadvertise_service"
    }
}