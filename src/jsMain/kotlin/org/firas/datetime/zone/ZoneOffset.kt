package org.firas.datetime.zone

/**
 *
 * @author Wu Yuping
 */
internal actual val SECONDS_CACHE: MutableMap<Int, ZoneOffset> = HashMap(16, 0.75f)
internal actual val ID_CACHE: MutableMap<String, ZoneOffset> = HashMap(16, 0.75f)

@JsName("getSystemZoneOffset")
actual fun getSystemZoneOffset(): ZoneOffset {
    return ZoneOffset.ofTotalSeconds(kotlin.js.Date().getTimezoneOffset() * 60)
}
