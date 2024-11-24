package com.github.thoebert.krosbridge
import com.github.thoebert.krosbridge.messages.primitive.Time
import com.github.thoebert.krosbridge.messages.primitive.Duration
import java.time.Instant

fun Time.toInstant() : Instant {
    return Instant.ofEpochSecond(this.secs.toLong(), this.nsecs.toLong())
}

fun Time.Companion.fromInstant(instant : Instant) : Time {
    return Time(instant.epochSecond.toInt(), instant.nano)
}

fun Time.Companion.now() : Time {
    return fromInstant(Instant.now())
}

fun Duration.toDuration() : java.time.Duration {
    return java.time.Duration.ofSeconds(this.secs.toLong(), this.nsecs.toLong())
}

fun Duration.Companion.fromDuration(duration : java.time.Duration) : Duration {
    return Duration(duration.seconds.toInt(), duration.nano)
}