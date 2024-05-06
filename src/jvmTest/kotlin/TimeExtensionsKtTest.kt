import com.github.thoebert.krosbridge.fromDuration
import com.github.thoebert.krosbridge.fromInstant
import com.github.thoebert.krosbridge.messages.primitive.Duration
import com.github.thoebert.krosbridge.messages.primitive.Time
import com.github.thoebert.krosbridge.toDuration
import com.github.thoebert.krosbridge.toInstant
import kotlinx.datetime.toKotlinInstant
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

class TimeExtensionsKtTest {

    @Test
    fun testToInstant() {
        val i = Time(10, 20).toInstant()
        assertEquals(10, i.epochSeconds)
        assertEquals(20, i.nanosecondsOfSecond)
    }

    @Test
    fun testFromInstant() {
        val t = Time.fromInstant(Instant.ofEpochSecond(10L,20L).toKotlinInstant())
        assertEquals(10, t.secs)
        assertEquals(20, t.nsecs)
    }

    @Test
    fun testToDuration() {
        val d = Duration(10, 20).toDuration()
        assertEquals(10, d.toJavaDuration().seconds)
        assertEquals(20, d.toJavaDuration().nano)
    }

    @Test
    fun testFromDuration() {
        val t = Duration.fromDuration(10.seconds + 20.nanoseconds)
        assertEquals(10, t.secs)
        assertEquals(20, t.nsecs)
    }
}