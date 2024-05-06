package com.github.thoebert.krosbridge.rosmessages


import kotlinx.serialization.Serializable


/**
 * ROSMessages are data objects to communicate with the ROSBridge
 *
 * @author Timon Hoebert - timon.hoebert@gmx.at
 */
@Serializable
sealed class ROSMessage(val op: String) {
    abstract val id: String?
}