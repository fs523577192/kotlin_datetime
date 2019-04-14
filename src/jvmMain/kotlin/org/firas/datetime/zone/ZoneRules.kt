package org.firas.datetime.zone

import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author Wu Yuping
 */
internal actual val lastRulesCache: MutableMap<Int, Array<ZoneOffsetTransition>> = ConcurrentHashMap()
