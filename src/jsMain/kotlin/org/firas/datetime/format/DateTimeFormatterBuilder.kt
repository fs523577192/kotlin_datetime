package org.firas.datetime.format

import org.firas.datetime.Instant
import org.firas.datetime.LocalDate
import org.firas.datetime.LocalDateTime
import org.firas.datetime.LocalTime
import org.firas.datetime.temporal.ChronoField
import org.firas.datetime.temporal.TemporalAccessor
import org.firas.datetime.temporal.TemporalQueries
import org.firas.datetime.util.SimpleImmutableEntry
import org.firas.datetime.util.TimeZoneNameUtility
import org.firas.datetime.zone.ZoneId
import org.firas.datetime.zone.ZoneOffset
import org.firas.datetime.zone.ZoneRulesProvider
import org.firas.util.Locale

/**
 *
 * @author Wu Yuping (migrate from OpenJDK 11)
 */
internal actual class ZoneTextPrinterParser internal actual constructor(
    /** The text style to output. */
    private val textStyle: TextStyle,

    /** The preferred zoneid map */
    preferredZones: Set<ZoneId>?,

    /**  Display in generic time-zone format. True in case of pattern letter 'v' */
    private val isGeneric: Boolean
): DateTimeFormatterBuilder.Companion.ZoneIdPrinterParser(
    TemporalQueries.ZONE, "ZoneText($textStyle)"
) {
    private val preferredZones: Set<String> = if (null == preferredZones) HashSet()
            else preferredZones.mapTo(HashSet()) { it.getId() }

    companion object {
        private const val STD = 0
        private const val DST = 1
        private const val GENERIC = 2

        private val cache = HashMap<String, MutableMap<Locale, Array<String>> >()
    }

    private fun getDisplayName(id: String, type: Int, locale: Locale): String? {
        if (this.textStyle == TextStyle.NARROW) {
            return null
        }
        var perLocale: MutableMap<Locale, Array<String>>? = cache[id]
        var names: Array<String>? = if (null == perLocale) null else perLocale[locale]
        if (null == names) {
            names = TimeZoneNameUtility.retrieveDisplayNames(id, locale)
            if (null == names) {
                return null
            }
            names = names.copyOfRange(0, 7)
            names[5] = TimeZoneNameUtility.retrieveGenericDisplayName(id, ZoneId.LONG, locale)
                ?: names[0] // use the id
            names[6] = TimeZoneNameUtility.retrieveGenericDisplayName(id, ZoneId.SHORT, locale)
                ?: names[0]
            if (perLocale == null) {
                perLocale = HashMap()
            }
            perLocale[locale] = names
            cache[id] = perLocale
        }
        when (type) {
            STD -> return names[textStyle.zoneNameStyleIndex + 1]
            DST -> return names[textStyle.zoneNameStyleIndex + 3]
        }
        return names[textStyle.zoneNameStyleIndex + 5]
    }

    override fun format(context: DateTimePrintContext, buf: StringBuilder): Boolean {
        val zone: ZoneId? = context.getValue(TemporalQueries.ZONE_ID)
        if (zone == null) {
            return false
        }
        var zname: String = zone.getId()
        if (zone !is ZoneOffset) {
            val dt: TemporalAccessor? = context.temporal
            var type = GENERIC
            if (!isGeneric) {
                if (dt!!.isSupported(ChronoField.INSTANT_SECONDS)) {
                    type = if (zone.getRules()!!.isDaylightSavings(Instant.from(dt))) DST else STD
                } else if (dt.isSupported(ChronoField.EPOCH_DAY) &&
                    dt.isSupported(ChronoField.NANO_OF_DAY)
                ) {
                    val date: LocalDate = LocalDate.ofEpochDay(dt.getLong(ChronoField.EPOCH_DAY))
                    val time: LocalTime = LocalTime.ofNanoOfDay(dt.getLong(ChronoField.NANO_OF_DAY))
                    val ldt: LocalDateTime = date.atTime(time)
                    if (zone.getRules()!!.getTransition(ldt) == null) {
                        type = if (zone.getRules()!!.isDaylightSavings(ldt.atZone(zone).toInstant()))
                            DST else STD
                    }
                }
            }
            val name = getDisplayName(zname, type, context.getLocale())
            if (name != null) {
                zname = name
            }
        }
        buf.append(zname)
        return true
    }

    // cache per instance for now
    private val cachedTree = HashMap<Locale,Map.Entry<Int,
            DateTimeFormatterBuilder.Companion.PrefixTree>>()
    private val cachedTreeCI = HashMap<Locale, Map.Entry<Int,
            DateTimeFormatterBuilder.Companion.PrefixTree>>()

    override fun getTree(context: DateTimeParseContext): DateTimeFormatterBuilder.Companion.PrefixTree {
        if (this.textStyle == TextStyle.NARROW) {
            return super.getTree(context)
        }
        val locale = context.getLocale()
        val isCaseSensitive = context.caseSensitive
        val regionIds: Set<String> = ZoneRulesProvider.getAvailableZoneIds()
        val regionIdsSize = regionIds.size

        val cached: MutableMap<Locale, Map.Entry<Int, DateTimeFormatterBuilder.Companion.PrefixTree>> =
                if (isCaseSensitive) cachedTree else cachedTreeCI

        val entry: Map.Entry<Int, DateTimeFormatterBuilder.Companion.PrefixTree>? = cached.get(locale)
        var tree: DateTimeFormatterBuilder.Companion.PrefixTree? =
                if (null == entry || entry.key != regionIdsSize) null else entry.value
        if (null == tree) {
            tree = DateTimeFormatterBuilder.Companion.PrefixTree.newTree(context)
            val zoneStrings = TimeZoneNameUtility.getZoneStrings(locale)?:arrayOf()
            for (names in zoneStrings) {
                var zid: String? = names[0]
                if (!regionIds.contains(zid)) {
                    continue
                }
                tree.add(zid!!, zid)    // don't convert zid -> metazone
                zid = ZoneName.toZid(zid, locale)
                var i = if (this.textStyle == TextStyle.FULL) 1 else 2
                while (i < names.size) {
                    tree.add(names[i], zid!!)
                    i += 2
                }
            }
            // if we have a set of preferred zones, need a copy and
            // add the preferred zones again to overwrite
            if (this.preferredZones != null) {
                for (names in zoneStrings) {
                    val zid: String = names[0]
                    if (!this.preferredZones.contains(zid) || !regionIds.contains(zid)) {
                        continue
                    }
                    var i = if (this.textStyle == TextStyle.FULL) 1 else 2
                    while (i < names.size) {
                        tree.add(names[i], zid)
                        i += 2
                    }
                }
            }
            cached.put(locale, SimpleImmutableEntry(regionIdsSize, tree))
        }
        return tree
    }
}
