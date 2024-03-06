package com.github.thoebert.krosbridge.rosmessages

import kotlinx.serialization.Serializable

/*
{ "op": "auth",
  "mac": <string>,
  "client": <string>,
  "dest": <string>,
  "rand": <string>,
  "t": <int>,
  "level": <string>,
  "end": <int>
}
 */

@Serializable
data class Authenticate(
    val mac : String,
    val client : String,
    val dest : String,
    val rand : String,
    val t : Int,
    val level : String,
    val end : Int,
    override val id: String? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "auth"
    }
}