/*
 * Copyright (c) 2012, 2017, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2008-2012, Stephen Colebourne & Michael Nascimento Santos
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

import org.firas.datetime.*
import org.firas.datetime.util.MathUtils
import org.firas.util.Arrays
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * The rules defining how the zone offset varies for a single time-zone.
 *
 *
 * The rules model all the historic and future transitions for a time-zone.
 * [ZoneOffsetTransition] is used for known transitions, typically historic.
 * [ZoneOffsetTransitionRule] is used for future transitions that are based
 * on the result of an algorithm.
 *
 *
 * The rules are loaded via [ZoneRulesProvider] using a [ZoneId].
 * The same rules may be shared internally between multiple zone IDs.
 *
 *
 * Serializing an instance of `ZoneRules` will store the entire set of rules.
 * It does not store the zone ID as it is not part of the state of this object.
 *
 *
 * A rule implementation may or may not store full information about historic
 * and future transitions, and the information stored is only as accurate as
 * that supplied to the implementation by the rules provider.
 * Applications should treat the data provided as representing the best information
 * available to the implementation of this rule.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class ZoneRules {

    /**
     * The transitions between standard offsets (epoch seconds), sorted.
     */
    private val standardTransitions: LongArray

    /**
     * The standard offsets.
     */
    private val standardOffsets: Array<ZoneOffset?>

    /**
     * The transitions between instants (epoch seconds), sorted.
     */
    private val savingsInstantTransitions: LongArray

    /**
     * The transitions between local date-times, sorted.
     * This is a paired array, where the first entry is the start of the transition
     * and the second entry is the end of the transition.
     */
    private val savingsLocalTransitions: Array<LocalDateTime>

    /**
     * The wall offsets.
     */
    private val wallOffsets: Array<ZoneOffset>

    /**
     * The last rule.
     */
    private val lastRules: Array<ZoneOffsetTransitionRule>

    /**
     * Creates an instance.
     *
     * @param baseStandardOffset  the standard offset to use before legal rules were set, not null
     * @param baseWallOffset  the wall offset to use before legal rules were set, not null
     * @param standardOffsetTransitionList  the list of changes to the standard offset, not null
     * @param transitionList  the list of transitions, not null
     * @param lastRules  the recurring last rules, size 16 or less, not null
     */
    internal constructor(baseStandardOffset: ZoneOffset,
                         baseWallOffset: ZoneOffset,
                         standardOffsetTransitionList:  List<ZoneOffsetTransition>,
                         transitionList: List<ZoneOffsetTransition>,
                         lastRules: List<ZoneOffsetTransitionRule>) {
        // convert standard transitions
        this.standardTransitions = LongArray(standardOffsetTransitionList.size)

        this.standardOffsets = arrayOfNulls(standardOffsetTransitionList.size + 1)
        this.standardOffsets[0] = baseStandardOffset
        for (i in 0 until standardOffsetTransitionList.size) {
            this.standardTransitions[i] = standardOffsetTransitionList.get(i).epochSecond
            this.standardOffsets[i + 1] = standardOffsetTransitionList.get(i).offsetAfter
        }

        // convert savings transitions to locals
        val localTransitionList: MutableList<LocalDateTime> = ArrayList()
        val localTransitionOffsetList: MutableList<ZoneOffset> = ArrayList()
        localTransitionOffsetList.add(baseWallOffset)
        transitionList.forEach { trans ->
            if (trans.isGap()) {
                localTransitionList.add(trans.getDateTimeBefore())
                localTransitionList.add(trans.getDateTimeAfter())
            } else {
                localTransitionList.add(trans.getDateTimeAfter())
                localTransitionList.add(trans.getDateTimeBefore())
            }
            localTransitionOffsetList.add(trans.offsetAfter)
        }
        this.savingsLocalTransitions = localTransitionList.toTypedArray()
        this.wallOffsets = localTransitionOffsetList.toTypedArray()

        // convert savings transitions to instants
        this.savingsInstantTransitions = LongArray(transitionList.size)
        for (i in 0 until transitionList.size) {
            this.savingsInstantTransitions[i] = transitionList.get(i).epochSecond
        }

        // last rules
        if (lastRules.size > 16) {
            throw IllegalArgumentException("Too many transition rules")
        }
        this.lastRules = lastRules.toTypedArray()
    }

    /**
     * Constructor.
     *
     * @param standardTransitions  the standard transitions, not null
     * @param standardOffsets  the standard offsets, not null
     * @param savingsInstantTransitions  the standard transitions, not null
     * @param wallOffsets  the wall offsets, not null
     * @param lastRules  the recurring last rules, size 15 or less, not null
     */
    private constructor(
        standardTransitions: LongArray,
        standardOffsets: Array<ZoneOffset?>,
        savingsInstantTransitions: LongArray,
        wallOffsets: Array<ZoneOffset>,
        lastRules: Array<ZoneOffsetTransitionRule>
    ) {
        this.standardTransitions = standardTransitions
        this.standardOffsets = standardOffsets
        this.savingsInstantTransitions = savingsInstantTransitions
        this.wallOffsets = wallOffsets
        this.lastRules = lastRules

        if (savingsInstantTransitions.isEmpty()) {
            this.savingsLocalTransitions = EMPTY_LDT_ARRAY
        } else {
            // convert savings transitions to locals
            val localTransitionList = ArrayList<LocalDateTime>()
            for (i in savingsInstantTransitions.indices) {
                val before = wallOffsets[i]
                val after = wallOffsets[i + 1]
                val trans = ZoneOffsetTransition(savingsInstantTransitions[i], before, after)
                if (trans.isGap()) {
                    localTransitionList.add(trans.getDateTimeBefore())
                    localTransitionList.add(trans.getDateTimeAfter())
                } else {
                    localTransitionList.add(trans.getDateTimeAfter())
                    localTransitionList.add(trans.getDateTimeBefore())
                }
            }
            this.savingsLocalTransitions = localTransitionList.toTypedArray()
        }
    }

    /**
     * Creates an instance of ZoneRules that has fixed zone rules.
     *
     * @param offset  the offset this fixed zone rules is based on, not null
     * @see .isFixedOffset
     */
    private constructor(offset: ZoneOffset) {
        this.standardOffsets = arrayOf(offset)
        this.standardTransitions = EMPTY_LONG_ARRAY
        this.savingsInstantTransitions = EMPTY_LONG_ARRAY
        this.savingsLocalTransitions = EMPTY_LDT_ARRAY
        this.wallOffsets = arrayOf(offset)
        this.lastRules = EMPTY_LASTRULES
    }

    companion object {
        /**
         * Serialization version.
         */
        private const val serialVersionUID = 3044319355680032515L
        /**
         * The last year to have its transitions cached.
         */
        private const val LAST_CACHED_YEAR = 2100

        /**
         * The zero-length long array.
         */
        @JvmStatic
        private val EMPTY_LONG_ARRAY = LongArray(0)

        /**
         * The zero-length lastrules array.
         */
        @JvmStatic
        private val EMPTY_LASTRULES = arrayOf<ZoneOffsetTransitionRule>()

        /**
         * The zero-length ldt array.
         */
        @JvmStatic
        private val EMPTY_LDT_ARRAY = arrayOf<LocalDateTime>()

        /**
         * Obtains an instance of ZoneRules that has fixed zone rules.
         *
         * @param offset  the offset this fixed zone rules is based on, not null
         * @return the zone rules, not null
         * @see .isFixedOffset
         */
        @JsName("ofZoneOffset")
        @JvmStatic
        fun of(offset: ZoneOffset): ZoneRules {
            return ZoneRules(offset)
        }

        /**
         * Obtains an instance of a ZoneRules.
         *
         * @param baseStandardOffset  the standard offset to use before legal rules were set, not null
         * @param baseWallOffset  the wall offset to use before legal rules were set, not null
         * @param standardOffsetTransitionList  the list of changes to the standard offset, not null
         * @param transitionList  the list of transitions, not null
         * @param lastRules  the recurring last rules, size 16 or less, not null
         * @return the zone rules, not null
         */
        @JsName("of")
        @JvmStatic
        fun of(
            baseStandardOffset: ZoneOffset,
            baseWallOffset: ZoneOffset,
            standardOffsetTransitionList: List<ZoneOffsetTransition>,
            transitionList: List<ZoneOffsetTransition>,
            lastRules: List<ZoneOffsetTransitionRule>
        ): ZoneRules {
            return ZoneRules(
                baseStandardOffset, baseWallOffset,
                standardOffsetTransitionList, transitionList, lastRules
            )
        }
    } // companion object

    /**
     * Checks of the zone rules are fixed, such that the offset never varies.
     *
     * @return true if the time-zone is fixed and the offset never changes
     */
    @JsName("isFixedOffset")
    fun isFixedOffset(): Boolean {
        return savingsInstantTransitions.isEmpty()
    }

    /**
     * Gets the offset applicable at the specified instant in these rules.
     *
     *
     * The mapping from an instant to an offset is simple, there is only
     * one valid offset for each instant.
     * This method returns that offset.
     *
     * @param instant  the instant to find the offset for, not null, but null
     * may be ignored if the rules have a single offset for all instants
     * @return the offset, not null
     */
    @JsName("getOffsetByInstant")
    fun getOffset(instant: Instant): ZoneOffset? {
        if (savingsInstantTransitions.isEmpty()) {
            return standardOffsets[0]
        }
        val epochSec = instant.epochSecond
        // check if using last rules
        if (lastRules.isNotEmpty() && epochSec > savingsInstantTransitions[savingsInstantTransitions.size - 1]) {
            val year = findYear(epochSec, wallOffsets[wallOffsets.size - 1])
            val transArray = findTransitionArray(year)
            var trans: ZoneOffsetTransition? = null
            for (i in transArray.indices) {
                trans = transArray[i]
                if (epochSec < trans!!.epochSecond) {
                    return trans!!.offsetBefore
                }
            }
            return trans!!.offsetAfter
        }

        // using historic rules
        var index = Arrays.binarySearch(savingsInstantTransitions, epochSec)
        if (index < 0) {
            // switch negative insert position to start of matched range
            index = -index - 2
        }
        return wallOffsets[index + 1]
    }

    /**
     * Gets a suitable offset for the specified local date-time in these rules.
     *
     *
     * The mapping from a local date-time to an offset is not straightforward.
     * There are three cases:
     *
     *  * Normal, with one valid offset. For the vast majority of the year, the normal
     * case applies, where there is a single valid offset for the local date-time.
     *  * Gap, with zero valid offsets. This is when clocks jump forward typically
     * due to the spring daylight savings change from "winter" to "summer".
     * In a gap there are local date-time values with no valid offset.
     *  * Overlap, with two valid offsets. This is when clocks are set back typically
     * due to the autumn daylight savings change from "summer" to "winter".
     * In an overlap there are local date-time values with two valid offsets.
     *
     * Thus, for any given local date-time there can be zero, one or two valid offsets.
     * This method returns the single offset in the Normal case, and in the Gap or Overlap
     * case it returns the offset before the transition.
     *
     *
     * Since, in the case of Gap and Overlap, the offset returned is a "best" value, rather
     * than the "correct" value, it should be treated with care. Applications that care
     * about the correct offset should use a combination of this method,
     * [.getValidOffsets] and [.getTransition].
     *
     * @param localDateTime  the local date-time to query, not null, but null
     * may be ignored if the rules have a single offset for all instants
     * @return the best available offset for the local date-time, not null
     */
    @JsName("getOffsetByLocalDateTime")
    fun getOffset(localDateTime: LocalDateTime): ZoneOffset {
        val info = getOffsetInfo(localDateTime)
        return if (info is ZoneOffsetTransition) {
            info.offsetBefore
        } else info as ZoneOffset
    }

    /**
     * Gets the offset applicable at the specified local date-time in these rules.
     *
     *
     * The mapping from a local date-time to an offset is not straightforward.
     * There are three cases:
     *
     *  * Normal, with one valid offset. For the vast majority of the year, the normal
     * case applies, where there is a single valid offset for the local date-time.
     *  * Gap, with zero valid offsets. This is when clocks jump forward typically
     * due to the spring daylight savings change from "winter" to "summer".
     * In a gap there are local date-time values with no valid offset.
     *  * Overlap, with two valid offsets. This is when clocks are set back typically
     * due to the autumn daylight savings change from "summer" to "winter".
     * In an overlap there are local date-time values with two valid offsets.
     *
     * Thus, for any given local date-time there can be zero, one or two valid offsets.
     * This method returns that list of valid offsets, which is a list of size 0, 1 or 2.
     * In the case where there are two offsets, the earlier offset is returned at index 0
     * and the later offset at index 1.
     *
     *
     * There are various ways to handle the conversion from a `LocalDateTime`.
     * One technique, using this method, would be:
     * <pre>
     * List&lt;ZoneOffset&gt; validOffsets = rules.getOffset(localDT);
     * if (validOffsets.size() == 1) {
     * // Normal case: only one valid offset
     * zoneOffset = validOffsets.get(0);
     * } else {
     * // Gap or Overlap: determine what to do from transition (which will be non-null)
     * ZoneOffsetTransition trans = rules.getTransition(localDT);
     * }
     * </pre>
     *
     *
     * In theory, it is possible for there to be more than two valid offsets.
     * This would happen if clocks to be put back more than once in quick succession.
     * This has never happened in the history of time-zones and thus has no special handling.
     * However, if it were to happen, then the list would return more than 2 entries.
     *
     * @param localDateTime  the local date-time to query for valid offsets, not null, but null
     * may be ignored if the rules have a single offset for all instants
     * @return the list of valid offsets, may be immutable, not null
     */
    fun getValidOffsets(localDateTime: LocalDateTime): List<ZoneOffset> {
        // should probably be optimized
        val info = getOffsetInfo(localDateTime)
        return if (info is ZoneOffsetTransition) {
            info.getValidOffsets()
        } else listOf(info as ZoneOffset)
    }

    /**
     * Gets the offset transition applicable at the specified local date-time in these rules.
     *
     *
     * The mapping from a local date-time to an offset is not straightforward.
     * There are three cases:
     *
     *  * Normal, with one valid offset. For the vast majority of the year, the normal
     * case applies, where there is a single valid offset for the local date-time.
     *  * Gap, with zero valid offsets. This is when clocks jump forward typically
     * due to the spring daylight savings change from "winter" to "summer".
     * In a gap there are local date-time values with no valid offset.
     *  * Overlap, with two valid offsets. This is when clocks are set back typically
     * due to the autumn daylight savings change from "summer" to "winter".
     * In an overlap there are local date-time values with two valid offsets.
     *
     * A transition is used to model the cases of a Gap or Overlap.
     * The Normal case will return null.
     *
     *
     * There are various ways to handle the conversion from a `LocalDateTime`.
     * One technique, using this method, would be:
     * <pre>
     * ZoneOffsetTransition trans = rules.getTransition(localDT);
     * if (trans != null) {
     * // Gap or Overlap: determine what to do from transition
     * } else {
     * // Normal case: only one valid offset
     * zoneOffset = rule.getOffset(localDT);
     * }
     * </pre>
     *
     * @param localDateTime  the local date-time to query for offset transition, not null, but null
     * may be ignored if the rules have a single offset for all instants
     * @return the offset transition, null if the local date-time is not in transition
     */
    @JsName("getTransition")
    fun getTransition(localDateTime: LocalDateTime): ZoneOffsetTransition? {
        val info = getOffsetInfo(localDateTime)
        return if (info is ZoneOffsetTransition) info else null
    }

    /**
     * Gets the standard offset for the specified instant in this zone.
     *
     *
     * This provides access to historic information on how the standard offset
     * has changed over time.
     * The standard offset is the offset before any daylight saving time is applied.
     * This is typically the offset applicable during winter.
     *
     * @param instant  the instant to find the offset information for, not null, but null
     * may be ignored if the rules have a single offset for all instants
     * @return the standard offset, not null
     */
    @JsName("getStandardOffset")
    fun getStandardOffset(instant: Instant): ZoneOffset? {
        if (savingsInstantTransitions.isEmpty()) {
            return standardOffsets[0]
        }
        val epochSec = instant.epochSecond
        var index = Arrays.binarySearch(standardTransitions, epochSec)
        if (index < 0) {
            // switch negative insert position to start of matched range
            index = -index - 2
        }
        return standardOffsets[index + 1]
    }

    /**
     * Gets the amount of daylight savings in use for the specified instant in this zone.
     *
     *
     * This provides access to historic information on how the amount of daylight
     * savings has changed over time.
     * This is the difference between the standard offset and the actual offset.
     * Typically the amount is zero during winter and one hour during summer.
     * Time-zones are second-based, so the nanosecond part of the duration will be zero.
     *
     *
     * This default implementation calculates the duration from the
     * [actual][.getOffset] and
     * [standard][.getStandardOffset] offsets.
     *
     * @param instant  the instant to find the daylight savings for, not null, but null
     * may be ignored if the rules have a single offset for all instants
     * @return the difference between the standard and actual offset, not null
     */
    @JsName("getDaylightSavings")
    fun getDaylightSavings(instant: Instant): Duration {
        if (savingsInstantTransitions.isEmpty()) {
            return Duration.ZERO
        }
        val standardOffset = getStandardOffset(instant)
        val actualOffset = getOffset(instant)
        return Duration.ofSeconds(actualOffset!!.totalSeconds.toLong() - standardOffset!!.totalSeconds)
    }

    /**
     * Checks if the specified instant is in daylight savings.
     *
     *
     * This checks if the standard offset and the actual offset are the same
     * for the specified instant.
     * If they are not, it is assumed that daylight savings is in operation.
     *
     *
     * This default implementation compares the {@link #getOffset(java.time.Instant) actual}
     * and {@link #getStandardOffset(java.time.Instant) standard} offsets.
     *
     * @param instant  the instant to find the offset information for, not null, but null
     *  may be ignored if the rules have a single offset for all instants
     * @return the standard offset, not null
     */
    @JsName("isDaylightSavings")
    fun isDaylightSavings(instant: Instant): Boolean {
        return (getStandardOffset(instant) != getOffset(instant))
    }

    /**
     * Checks if the offset date-time is valid for these rules.
     *
     *
     * To be valid, the local date-time must not be in a gap and the offset
     * must match one of the valid offsets.
     *
     *
     * This default implementation checks if [.getValidOffsets]
     * contains the specified offset.
     *
     * @param localDateTime  the date-time to check, not null, but null
     * may be ignored if the rules have a single offset for all instants
     * @param offset  the offset to check, null returns false
     * @return true if the offset date-time is valid for these rules
     */
    @JsName("isValidOffset")
    fun isValidOffset(localDateTime: LocalDateTime, offset: ZoneOffset): Boolean {
        return getValidOffsets(localDateTime).contains(offset)
    }

    /**
     * Gets the next transition after the specified instant.
     *
     *
     * This returns details of the next transition after the specified instant.
     * For example, if the instant represents a point where "Summer" daylight savings time
     * applies, then the method will return the transition to the next "Winter" time.
     *
     * @param instant  the instant to get the next transition after, not null, but null
     * may be ignored if the rules have a single offset for all instants
     * @return the next transition after the specified instant, null if this is after the last transition
     */
    @JsName("nextTransition")
    fun nextTransition(instant: Instant): ZoneOffsetTransition? {
        if (savingsInstantTransitions.isEmpty()) {
            return null
        }
        val epochSec = instant.epochSecond
        // check if using last rules
        if (epochSec >= savingsInstantTransitions[savingsInstantTransitions.size - 1]) {
            if (lastRules.isEmpty()) {
                return null
            }
            // search year the instant is in
            val year = findYear(epochSec, wallOffsets[wallOffsets.size - 1])
            var transArray = findTransitionArray(year)
            for (trans in transArray) {
                if (epochSec < trans.epochSecond) {
                    return trans
                }
            }
            // use first from following year
            if (year < Year.MAX_VALUE) {
                transArray = findTransitionArray(year + 1)
                return transArray[0]
            }
            return null
        }

        // using historic rules
        var index = Arrays.binarySearch(savingsInstantTransitions, epochSec)
        if (index < 0) {
            index = -index - 1  // switched value is the next transition
        } else {
            index += 1  // exact match, so need to add one to get the next
        }
        return ZoneOffsetTransition(savingsInstantTransitions[index],
                wallOffsets[index], wallOffsets[index + 1])
    }

    /**
     * Gets the previous transition before the specified instant.
     *
     *
     * This returns details of the previous transition before the specified instant.
     * For example, if the instant represents a point where "summer" daylight saving time
     * applies, then the method will return the transition from the previous "winter" time.
     *
     * @param instant  the instant to get the previous transition after, not null, but null
     * may be ignored if the rules have a single offset for all instants
     * @return the previous transition before the specified instant, null if this is before the first transition
     */
    @JsName("previousTransition")
    fun previousTransition(instant: Instant): ZoneOffsetTransition? {
        if (savingsInstantTransitions.isEmpty()) {
            return null
        }
        var epochSec = instant.epochSecond
        if (instant.nanos > 0 && epochSec < Long.MAX_VALUE) {
            epochSec += 1  // allow rest of method to only use seconds
        }

        // check if using last rules
        val lastHistoric = savingsInstantTransitions[savingsInstantTransitions.size - 1]
        if (lastRules.isNotEmpty() && epochSec > lastHistoric) {
            // search year the instant is in
            val lastHistoricOffset = wallOffsets[wallOffsets.size - 1]
            var year = findYear(epochSec, lastHistoricOffset)
            var transArray = findTransitionArray(year)
            for (i in transArray.indices.reversed()) {
                if (epochSec > transArray[i].epochSecond) {
                    return transArray[i]
                }
            }
            // use last from preceding year
            val lastHistoricYear = findYear(lastHistoric, lastHistoricOffset)
            if (--year > lastHistoricYear) {
                transArray = findTransitionArray(year)
                return transArray[transArray.size - 1]
            }
            // drop through
        }

        // using historic rules
        var index = Arrays.binarySearch(savingsInstantTransitions, epochSec)
        if (index < 0) {
            index = -index - 1
        }
        return if (index <= 0) {
            null
        } else ZoneOffsetTransition(savingsInstantTransitions[index - 1],
                wallOffsets[index - 1], wallOffsets[index])
    }

    /**
     * Gets the complete list of fully defined transitions.
     *
     *
     * The complete set of transitions for this rules instance is defined by this method
     * and [.getTransitionRules]. This method returns those transitions that have
     * been fully defined. These are typically historical, but may be in the future.
     *
     *
     * The list will be empty for fixed offset rules and for any time-zone where there has
     * only ever been a single offset. The list will also be empty if the transition rules are unknown.
     *
     * @return an immutable list of fully defined transitions, not null
     */
    @JsName("getTransitions")
    fun getTransitions(): List<ZoneOffsetTransition> {
        val list = ArrayList<ZoneOffsetTransition>()
        for (i in 0 until savingsInstantTransitions.size) {
            list.add(ZoneOffsetTransition(savingsInstantTransitions[i], wallOffsets[i], wallOffsets[i + 1]))
        }
        return list
    }

    /**
     * Gets the list of transition rules for years beyond those defined in the transition list.
     *
     *
     * The complete set of transitions for this rules instance is defined by this method
     * and [.getTransitions]. This method returns instances of [ZoneOffsetTransitionRule]
     * that define an algorithm for when transitions will occur.
     *
     *
     * For any given `ZoneRules`, this list contains the transition rules for years
     * beyond those years that have been fully defined. These rules typically refer to future
     * daylight saving time rule changes.
     *
     *
     * If the zone defines daylight savings into the future, then the list will normally
     * be of size two and hold information about entering and exiting daylight savings.
     * If the zone does not have daylight savings, or information about future changes
     * is uncertain, then the list will be empty.
     *
     *
     * The list will be empty for fixed offset rules and for any time-zone where there is no
     * daylight saving time. The list will also be empty if the transition rules are unknown.
     *
     * @return an immutable list of transition rules, not null
     */
    @JsName("getTransitionRules")
    fun getTransitionRules(): List<ZoneOffsetTransitionRule> {
        return lastRules.asList()
    }

    // ----==== Override methods inherited from Any ====----
    override fun equals(other: Any?): Boolean {
        if (this === other) {
           return true
        }
        if (other is ZoneRules) {
            return Arrays.equals(standardTransitions, other.standardTransitions) &&
                    Arrays.equals(standardOffsets, other.standardOffsets) &&
                    Arrays.equals(savingsInstantTransitions, other.savingsInstantTransitions) &&
                    Arrays.equals(wallOffsets, other.wallOffsets) &&
                    Arrays.equals(lastRules, other.lastRules)
        }
        return false
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(standardTransitions) xor
                Arrays.hashCode(standardOffsets) xor
                Arrays.hashCode(savingsInstantTransitions) xor
                Arrays.hashCode(wallOffsets) xor
                Arrays.hashCode(lastRules)
    }

    override fun toString(): String {
        return "ZoneRules[currentStandardOffset=" + standardOffsets[standardOffsets.size - 1] + "]"
    }

    private fun findYear(epochSecond: Long, offset: ZoneOffset): Int {
        // inline for performance
        val localSecond = epochSecond + offset.totalSeconds
        val localEpochDay = MathUtils.floorDiv(localSecond, 86400)
        return LocalDate.ofEpochDay(localEpochDay).year
    }

    private fun getOffsetInfo(dt: LocalDateTime): Any? {
        if (savingsInstantTransitions.isEmpty()) {
            return standardOffsets[0]
        }
        // check if using last rules
        if (lastRules.isNotEmpty() &&
                dt.isAfter(savingsLocalTransitions[savingsLocalTransitions.size - 1])) {
            val transArray = findTransitionArray(dt.getYear())
            var info: Any? = null
            for (trans in transArray) {
                info = findOffsetInfo(dt, trans)
                if (info is ZoneOffsetTransition || info == trans.offsetBefore) {
                    return info
                }
            }
            return info
        }

        // using historic rules
        var index  = Arrays.binarySearch(savingsLocalTransitions, dt)
        if (index == -1) {
            // before first transition
            return wallOffsets[0]
        }
        if (index < 0) {
            // switch negative insert position to start of matched range
            index = -index - 2
        } else if (index < savingsLocalTransitions.size - 1 &&
                savingsLocalTransitions[index] == savingsLocalTransitions[index + 1]) {
            // handle overlap immediately following gap
            index += 1
        }
        if ((index and 1) == 0) {
            // gap or overlap
            val dtBefore = savingsLocalTransitions[index]
            val dtAfter = savingsLocalTransitions[index + 1]
            val offsetBefore = wallOffsets[index / 2]
            val offsetAfter = wallOffsets[index / 2 + 1]
            if (offsetAfter.totalSeconds > offsetBefore.totalSeconds) {
                // gap
                return ZoneOffsetTransition(dtBefore, offsetBefore, offsetAfter)
            } else {
                // overlap
                return ZoneOffsetTransition(dtAfter, offsetBefore, offsetAfter)
            }
        } else {
            // normal (neither gap or overlap)
            return wallOffsets[index / 2 + 1]
        }
    }

    /**
     * Finds the offset info for a local date-time and transition.
     *
     * @param dt  the date-time, not null
     * @param trans  the transition, not null
     * @return the offset info, not null
     */
    private fun findOffsetInfo(dt: LocalDateTime, trans: ZoneOffsetTransition): Any {
        val localTransition = trans.getDateTimeBefore()
        if (trans.isGap()) {
            if (dt.isBefore(localTransition)) {
                return trans.offsetBefore
            }
            return if (dt.isBefore(trans.getDateTimeAfter())) {
                trans
            } else {
                trans.offsetAfter
            }
        } else {
            if (!dt.isBefore(localTransition)) {
                return trans.offsetAfter
            }
            return if (dt.isBefore(trans.getDateTimeAfter())) {
                trans.offsetBefore
            } else {
                trans
            }
        }
    }

    /**
     * Finds the appropriate transition array for the given year.
     *
     * @param year  the year, not null
     * @return the transition array, not null
     */
    private fun findTransitionArray(year: Int): Array<ZoneOffsetTransition> {
        val yearObj = year  // should use Year class, but this saves a class load
        var transArray = lastRulesCache[yearObj]
        if (transArray != null) {
            return transArray
        }
        val ruleArray = lastRules
        transArray = Array(ruleArray.size) {
            ruleArray[it].createTransition(year)
        }
        if (year < LAST_CACHED_YEAR) {
            lastRulesCache[yearObj] = transArray
        }
        return transArray
    }
}
internal expect val lastRulesCache: MutableMap<Int, Array<ZoneOffsetTransition>>