package com.github.thoebert.krosbridge.rosmessages

import kotlinx.serialization.Serializable

/*
{ "op": "png",
  (optional) "id": <string>,
  "data": <string>,
  (optional) "num": <int>,
  (optional) "total": <int>
}
 */

@Serializable
data class PNGCompression  (
    override val id: String? = null,
    val data: String,
    val num: Int? = null,
    val total : Int? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "png"
    }
}