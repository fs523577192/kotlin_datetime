package org.firas.datetime.zone

import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author Wu Yuping
 */
internal actual val SECONDS_CACHE: MutableMap<Int, ZoneOffset> = ConcurrentHashMap<Int, ZoneOffset>(16, 0.75f, 4)
internal actual val ID_CACHE: MutableMap<String, ZoneOffset> = ConcurrentHashMap<String, ZoneOffset>(16, 0.75f, 4)
