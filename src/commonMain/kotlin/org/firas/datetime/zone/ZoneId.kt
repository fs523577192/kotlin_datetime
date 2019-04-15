/*
 * Copyright (c) 2012, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * This file is available under and governed by the GNU General Public
 * License version 2 only, as published by the Free Software Foundation.
 * However, the following notice accompanied the original version of this
 * file:
 *
 * Copyright (c) 2007-2012, Stephen Colebourne & Michael Nascimento Santos
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  * Neither the name of JSR-310 nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.firas.datetime.zone

import org.firas.datetime.DateTimeException
import org.firas.datetime.Instant
import org.firas.datetime.temporal.TemporalAccessor
import org.firas.datetime.temporal.TemporalQueries
import org.firas.datetime.temporal.getClassName

/**
 * A time-zone ID, such as `Europe/Paris`.
 * <p>
 * A `ZoneId` is used to identify the rules used to convert between
 * an {@link Instant} and a {@link LocalDateTime}.
 * There are two distinct types of ID:
 * <ul>
 * <li>Fixed offsets - a fully resolved offset from UTC/Greenwich, that uses
 *  the same offset for all local date-times
 * <li>Geographical regions - an area where a specific set of rules for finding
 *  the offset from UTC/Greenwich apply
 * </ul>
 * Most fixed offsets are represented by {@link ZoneOffset}.
 * Calling {@link #normalized()} on any `ZoneId` will ensure that a
 * fixed offset ID will be represented as a `ZoneOffset`.
 *
 *
 * The actual rules, describing when and how the offset changes, are defined by {@link ZoneRules}.
 * This class is simply an ID used to obtain the underlying rules.
 * This approach is taken because rules are defined by governments and change
 * frequently, whereas the ID is stable.
 *
 *
 * The distinction has other effects. Serializing the `ZoneId` will only send
 * the ID, whereas serializing the rules sends the entire data set.
 * Similarly, a comparison of two IDs only examines the ID, whereas
 * a comparison of two rules examines the entire data set.
 *
 * <h3>Time-zone IDs</h3>
 * The ID is unique within the system.
 * There are three types of ID.
 *
 *
 * The simplest type of ID is that from `ZoneOffset`.
 * This consists of 'Z' and IDs starting with '+' or '-'.
 *
 *
 * The next type of ID are offset-style IDs with some form of prefix,
 * such as 'GMT+2' or 'UTC+01:00'.
 * The recognised prefixes are 'UTC', 'GMT' and 'UT'.
 * The offset is the suffix and will be normalized during creation.
 * These IDs can be normalized to a `ZoneOffset` using `normalized()`.
 *
 *
 * The third type of ID are region-based IDs. A region-based ID must be of
 * two or more characters, and not start with 'UTC', 'GMT', 'UT' '+' or '-'.
 * Region-based IDs are defined by configuration, see {@link ZoneRulesProvider}.
 * The configuration focuses on providing the lookup from the ID to the
 * underlying `ZoneRules`.
 *
 *
 * Time-zone rules are defined by governments and change frequently.
 * There are a number of organizations, known here as groups, that monitor
 * time-zone changes and collate them.
 * The default group is the IANA Time Zone Database (TZDB).
 * Other organizations include IATA (the airline industry body) and Microsoft.
 *
 *
 * Each group defines its own format for the region ID it provides.
 * The TZDB group defines IDs such as 'Europe/London' or 'America/New_York'.
 * TZDB IDs take precedence over other groups.
 *
 *
 * It is strongly recommended that the group name is included in all IDs supplied by
 * groups other than TZDB to avoid conflicts. For example, IATA airline time-zone
 * region IDs are typically the same as the three letter airport code.
 * However, the airport of Utrecht has the code 'UTC', which is obviously a conflict.
 * The recommended format for region IDs from groups other than TZDB is 'group~region'.
 * Thus if IATA data were defined, Utrecht airport would be 'IATA~UTC'.
 *
 *
 * <h3>Serialization</h3>
 * This class can be serialized and stores the string zone ID in the external form.
 * The `ZoneOffset` subclass uses a dedicated format that only stores the
 * offset from UTC/Greenwich.
 *
 *
 * A `ZoneId` can be deserialized in a Java Runtime where the ID is unknown.
 * For example, if a server-side Java Runtime has been updated with a new zone ID, but
 * the client-side Java Runtime has not been updated. In this case, the `ZoneId`
 * object will exist, and can be queried using `getId`, `equals`,
 * `hashCode`, `toString`, `getDisplayName` and `normalized`.
 * However, any call to `getRules` will fail with `ZoneRulesException`.
 * This approach is designed to allow a {@link ZonedDateTime} to be loaded and
 * queried, but not modified, on a Java Runtime with incomplete time-zone information.
 *
 *
 *
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `ZoneId` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This abstract class has two implementations, both of which are immutable and thread-safe.
 * One implementation models region-based IDs, the other is `ZoneOffset` modelling
 * offset-based IDs. This difference is visible in serialization.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
abstract class ZoneId internal constructor() {

    companion object {
        /**
         * A map of zone overrides to enable the short time-zone names to be used.
         *
         *
         * Use of short zone IDs has been deprecated in `java.util.TimeZone`.
         * This map allows the IDs to continue to be used via the
         * {@link #of(String, Map)} factory method.
         *
         *
         * This map contains a mapping of the IDs that is in line with TZDB 2005r and
         * later, where 'EST', 'MST' and 'HST' map to IDs which do not include daylight
         * savings.
         *
         *
         * This maps as follows:
         * <ul>
         * <li>EST - -05:00</li>
         * <li>HST - -10:00</li>
         * <li>MST - -07:00</li>
         * <li>ACT - Australia/Darwin</li>
         * <li>AET - Australia/Sydney</li>
         * <li>AGT - America/Argentina/Buenos_Aires</li>
         * <li>ART - Africa/Cairo</li>
         * <li>AST - America/Anchorage</li>
         * <li>BET - America/Sao_Paulo</li>
         * <li>BST - Asia/Dhaka</li>
         * <li>CAT - Africa/Harare</li>
         * <li>CNT - America/St_Johns</li>
         * <li>CST - America/Chicago</li>
         * <li>CTT - Asia/Shanghai</li>
         * <li>EAT - Africa/Addis_Ababa</li>
         * <li>ECT - Europe/Paris</li>
         * <li>IET - America/Indiana/Indianapolis</li>
         * <li>IST - Asia/Kolkata</li>
         * <li>JST - Asia/Tokyo</li>
         * <li>MIT - Pacific/Apia</li>
         * <li>NET - Asia/Yerevan</li>
         * <li>NST - Pacific/Auckland</li>
         * <li>PLT - Asia/Karachi</li>
         * <li>PNT - America/Phoenix</li>
         * <li>PRT - America/Puerto_Rico</li>
         * <li>PST - America/Los_Angeles</li>
         * <li>SST - Pacific/Guadalcanal</li>
         * <li>VST - Asia/Ho_Chi_Minh</li>
         * </ul>
         * The map is unmodifiable.
         */
        val SHORT_IDS: Map<String, String> = hashMapOf(
            Pair("ACT", "Australia/Darwin"),
            Pair("AET", "Australia/Sydney"),
            Pair("AGT", "America/Argentina/Buenos_Aires"),
            Pair("ART", "Africa/Cairo"),
            Pair("AST", "America/Anchorage"),
            Pair("BET", "America/Sao_Paulo"),
            Pair("BST", "Asia/Dhaka"),
            Pair("CAT", "Africa/Harare"),
            Pair("CNT", "America/St_Johns"),
            Pair("CST", "America/Chicago"),
            Pair("CTT", "Asia/Shanghai"),
            Pair("EAT", "Africa/Addis_Ababa"),
            Pair("ECT", "Europe/Paris"),
            Pair("IET", "America/Indiana/Indianapolis"),
            Pair("IST", "Asia/Kolkata"),
            Pair("JST", "Asia/Tokyo"),
            Pair("MIT", "Pacific/Apia"),
            Pair("NET", "Asia/Yerevan"),
            Pair("NST", "Pacific/Auckland"),
            Pair("PLT", "Asia/Karachi"),
            Pair("PNT", "America/Phoenix"),
            Pair("PRT", "America/Puerto_Rico"),
            Pair("PST", "America/Los_Angeles"),
            Pair("SST", "Pacific/Guadalcanal"),
            Pair("VST", "Asia/Ho_Chi_Minh"),
            Pair("EST", "-05:00"),
            Pair("MST", "-07:00"),
            Pair("HST", "-10:00")
        )

        /**
         * Serialization version.
         */
        private const val serialVersionUID = 8352817235686L

        /**
         * Obtains an instance of `ZoneId` wrapping an offset.
         *
         *
         * If the prefix is "GMT", "UTC", or "UT" a `ZoneId`
         * with the prefix and the non-zero offset is returned.
         * If the prefix is empty `""` the `ZoneOffset` is returned.
         *
         * @param prefix  the time-zone ID, not null
         * @param offset  the offset, not null
         * @return the zone ID, not null
         * @throws IllegalArgumentException if the prefix is not one of
         * "GMT", "UTC", or "UT", or ""
         */
        fun ofOffset(prefix: String, offset: ZoneOffset): ZoneId {
            var prefix = prefix
            if (prefix.isEmpty()) {
                return offset
            }

            if (!prefix.equals("GMT") && !prefix.equals("UTC") && !prefix.equals("UT")) {
                throw IllegalArgumentException("prefix should be GMT, UTC or UT, is: $prefix")
            }

            if (offset.totalSeconds != 0) {
                prefix = prefix + offset.getId()
            }
            return ZoneRegion(prefix, offset.getRules())
        }

        /**
         * Obtains an instance of `ZoneId` from a temporal object.
         *
         *
         * This obtains a zone based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `ZoneId`.
         *
         *
         * A `TemporalAccessor` represents some form of date and time information.
         * This factory converts the arbitrary temporal object to an instance of `ZoneId`.
         *
         *
         * The conversion will try to obtain the zone in a way that favours region-based
         * zones over offset-based zones using {@link TemporalQueries#zone()}.
         *
         *
         * This method matches the signature of the functional interface [TemporalQuery]
         * allowing it to be used as a query via method reference, `ZoneId::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the zone ID, not null
         * @throws DateTimeException if unable to convert to a `ZoneId`
         */
        fun from(temporal: TemporalAccessor): ZoneId {
            return temporal.query(TemporalQueries.ZONE) ?: throw DateTimeException(
                "Unable to obtain ZoneId from TemporalAccessor: " +
                        temporal + " of type " + temporal.getClassName()
            )
        }

        /**
         * Parses the ID, taking a flag to indicate whether `ZoneRulesException`
         * should be thrown or not, used in deserialization.
         *
         * @param zoneId  the time-zone ID, not null
         * @param checkAvailable  whether to check if the zone ID is available
         * @return the zone ID, not null
         * @throws DateTimeException if the ID format is invalid
         * @throws ZoneRulesException if checking availability and the ID cannot be found
         */
        internal fun of(zoneId: String, checkAvailable: Boolean): ZoneId {
            if (zoneId.length <= 1 || zoneId.startsWith("+") || zoneId.startsWith("-")) {
                return ZoneOffset.of(zoneId)
            } else if (zoneId.startsWith("UTC") || zoneId.startsWith("GMT")) {
                return ofWithPrefix(zoneId, 3, checkAvailable)
            } else if (zoneId.startsWith("UT")) {
                return ofWithPrefix(zoneId, 2, checkAvailable)
            }
            return ZoneRegion.ofId(zoneId, checkAvailable)
        }

        /**
         * Parse once a prefix is established.
         *
         * @param zoneId  the time-zone ID, not null
         * @param prefixLength  the length of the prefix, 2 or 3
         * @return the zone ID, not null
         * @throws DateTimeException if the zone ID has an invalid format
         */
        private fun ofWithPrefix(zoneId: String, prefixLength: Int, checkAvailable: Boolean): ZoneId {
            val prefix = zoneId.substring(0, prefixLength)
            if (zoneId.length == prefixLength) {
                return ofOffset(prefix, ZoneOffset.UTC)
            }
            if (zoneId[prefixLength] != '+' && zoneId[prefixLength] != '-') {
                return ZoneRegion.ofId(zoneId, checkAvailable)  // drop through to ZoneRulesProvider
            }
            try {
                val offset = ZoneOffset.of(zoneId.substring(prefixLength))
                return if (offset === ZoneOffset.UTC) {
                    ofOffset(prefix, offset)
                } else ofOffset(prefix, offset)
            } catch (ex: DateTimeException) {
                throw DateTimeException("Invalid ID for offset-based ZoneId: $zoneId", ex)
            }

        }
    } // companion object

    /**
     * Gets the unique time-zone ID.
     *
     *
     * This ID uniquely defines this object.
     * The format of an offset based ID is defined by [ZoneOffset.getId].
     *
     * @return the time-zone unique ID, not null
     */
    abstract fun getId(): String

    /**
     * Gets the time-zone rules for this ID allowing calculations to be performed.
     *
     *
     * The rules provide the functionality associated with a time-zone,
     * such as finding the offset for a given instant or local date-time.
     *
     *
     * A time-zone can be invalid if it is deserialized in a Java Runtime which
     * does not have the same rules loaded as the Java Runtime that stored it.
     * In this case, calling this method will throw a `ZoneRulesException`.
     *
     *
     * The rules are supplied by [ZoneRulesProvider]. An advanced provider may
     * support dynamic updates to the rules without restarting the Java Runtime.
     * If so, then the result of this method may change over time.
     * Each individual call will be still remain thread-safe.
     *
     *
     * [ZoneOffset] will always return a set of rules where the offset never changes.
     *
     * @return the rules, not null
     * @throws ZoneRulesException if no rules are available for this ID
     */
    abstract fun getRules(): ZoneRules?

    /**
     * Normalizes the time-zone ID, returning a `ZoneOffset` where possible.
     *
     *
     * The returns a normalized `ZoneId` that can be used in place of this ID.
     * The result will have `ZoneRules` equivalent to those returned by this object,
     * however the ID returned by `getId()` may be different.
     *
     *
     * The normalization checks if the rules of this `ZoneId` have a fixed offset.
     * If they do, then the `ZoneOffset` equal to that offset is returned.
     * Otherwise `this` is returned.
     *
     * @return the time-zone unique ID, not null
     */
    fun normalized(): ZoneId? {
        try {
            val rules = getRules()!!
            if (rules.isFixedOffset()) {
                return rules.getOffset(Instant.EPOCH)
            }
        } catch (ex: ZoneRulesException) {
            // invalid ZoneRegion is not important to this method
        }
        return this
    }
}