package com.github.thoebert.krosbridge.rosmessages

import kotlinx.serialization.Serializable

/*
{ "op": "fragment",
  "id": <string>,
  "data": <string>,
  "num": <int>,
  "total": <int>
}
 */

@Serializable
data class Fragmentation  (
    override val id: String,
    val data: String,
    val num: Int? = null,
    val total : Int? = null,
): ROSMessage(OPERATION){
    companion object{
        const val OPERATION = "fragment"
    }
}