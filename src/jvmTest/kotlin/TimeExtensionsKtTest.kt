import com.github.thoebert.krosbridge.fromDuration
import com.github.thoebert.krosbridge.fromInstant
import com.github.thoebert.krosbridge.messages.primitive.Duration
import com.github.thoebert.krosbridge.messages.primitive.Time
import com.github.thoebert.krosbridge.toDuration
import com.github.thoebert.krosbridge.toInstant
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeExtensionsKtTest {

    @Test
    fun testToInstant() {
        val i = Time(10, 20).toInstant()
        assertEquals(10, i.epochSecond)
        assertEquals(20, i.nano)
    }

    @Test
    fun testFromInstant() {
        val t = Time.fromInstant(Instant.ofEpochSecond(10,20))
        assertEquals(10, t.secs)
        assertEquals(20, t.nsecs)
    }

    @Test
    fun testToDuration() {
        val d = Duration(10, 20).toDuration()
        assertEquals(10, d.seconds)
        assertEquals(20, d.nano)
    }

    @Test
    fun testFromDuration() {
        val t = Duration.fromDuration(java.time.Duration.ofSeconds(10,20))
        assertEquals(10, t.secs)
        assertEquals(20, t.nsecs)
    }
}