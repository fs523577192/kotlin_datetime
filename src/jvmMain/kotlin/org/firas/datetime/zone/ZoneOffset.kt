package org.firas.datetime.zone

import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author Wu Yuping
 */
internal actual val SECONDS_CACHE: MutableMap<Int, ZoneOffset> = ConcurrentHashMap<Int, ZoneOffset>(16, 0.75f, 4)
internal actual val ID_CACHE: MutableMap<String, ZoneOffset> = ConcurrentHashMap<String, ZoneOffset>(16, 0.75f, 4)

actual fun getSystemZoneOffset(): ZoneOffset {
    val offset = java.util.TimeZone.getDefault().rawOffset
    // TODO: daylight saving time
    return ZoneOffset.ofTotalSeconds(offset / 1000)
}
