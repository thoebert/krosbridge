package com.github.thoebert.krosbridge

import com.github.thoebert.krosbridge.messages.primitive.Duration
import com.github.thoebert.krosbridge.messages.primitive.Time
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

fun Time.toInstant(): Instant {
    return Instant.fromEpochSeconds(this.secs.toLong(), this.nsecs.toLong())
}

fun Time.Companion.fromInstant(instant: Instant): Time {
    return Time(instant.epochSeconds.toInt(), instant.nanosecondsOfSecond)
}

fun Time.Companion.now(): Time {
    return fromInstant(Clock.System.now())
}

fun Duration.toDuration(): kotlin.time.Duration {
    return this.secs.seconds + this.nsecs.nanoseconds
}

fun Duration.Companion.fromDuration(duration: kotlin.time.Duration): Duration {
    return Duration(
        duration.inWholeSeconds.toInt(),
        (duration.inWholeNanoseconds - duration.inWholeSeconds * 1_000_000_000).toInt()
    )
}