package org.firas.datetime.zone

import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author Wu Yuping
 */
actual internal val ZONES: MutableMap<String, ZoneRulesProvider> =
        ConcurrentHashMap(512, 0.75f, 2)
