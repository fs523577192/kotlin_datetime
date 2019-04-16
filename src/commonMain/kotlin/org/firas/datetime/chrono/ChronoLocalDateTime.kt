/*
 * Copyright (c) 2012, 2013, Oracle and/or its affiliates. All rights reserved.
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
package org.firas.datetime.chrono

import org.firas.datetime.zone.ZoneOffset
import org.firas.datetime.Instant
import org.firas.datetime.LocalTime
import org.firas.datetime.temporal.*
import org.firas.datetime.zone.ZoneId

/**
 * A date-time without a time-zone in an arbitrary chronology, intended
 * for advanced globalization use cases.
 *
 *
 * <b>Most applications should declare method signatures, fields and variables
 * as [LocalDateTime], not this interface.</b>
 *
 *
 * A `ChronoLocalDateTime` is the abstract representation of a local date-time
 * where the `Chronology chronology`, or calendar system, is pluggable.
 * The date-time is defined in terms of fields expressed by [TemporalField],
 * where most common implementations are defined in [ChronoField].
 * The chronology defines how the calendar system operates and the meaning of
 * the standard fields.
 *
 * <h3>When to use this interface</h3>
 * The design of the API encourages the use of `LocalDateTime` rather than this
 * interface, even in the case where the application needs to deal with multiple
 * calendar systems. The rationale for this is explored in detail in [ChronoLocalDate].
 *
 *
 * Ensure that the discussion in `ChronoLocalDate` has been read and understood
 * before using this interface.
 *
 * @implSpec
 * This interface must be implemented with care to ensure other classes operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * Subclasses should be Serializable wherever possible.
 *
 * @param <D> the concrete type for the date of this date-time
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
interface ChronoLocalDateTime<D: ChronoLocalDate>: Temporal, TemporalAdjuster {

    /**
     * renamed from ~toLocalDate`
     */
    fun getDate(): D

    /**
     * renamed from `toLocalTime`
     */
    fun getTime(): LocalTime

    /**
     * Gets the chronology of this date-time.
     *
     *
     * The `Chronology` represents the calendar system in use.
     * The era and other fields in [ChronoField] are defined by the chronology.
     *
     * @return the chronology, not null
     */
    fun getChronology(): Chronology {
        return getDate().getChronology()
    }

    /**
     * Combines this time with a time-zone to create a `ChronoZonedDateTime`.
     *
     *
     * This returns a `ChronoZonedDateTime` formed from this date-time at the
     * specified time-zone. The result will match this date-time as closely as possible.
     * Time-zone rules, such as daylight savings, mean that not every local date-time
     * is valid for the specified zone, thus the local date-time may be adjusted.
     *
     *
     * The local date-time is resolved to a single instant on the time-line.
     * This is achieved by finding a valid offset from UTC/Greenwich for the local
     * date-time as defined by the [rules][ZoneRules] of the zone ID.
     *
     *
     * In most cases, there is only one valid offset for a local date-time.
     * In the case of an overlap, where clocks are set back, there are two valid offsets.
     * This method uses the earlier offset typically corresponding to "summer".
     *
     *
     * In the case of a gap, where clocks jump forward, there is no valid offset.
     * Instead, the local date-time is adjusted to be later by the length of the gap.
     * For a typical one hour daylight savings change, the local date-time will be
     * moved one hour later into the offset typically corresponding to "summer".
     *
     *
     * To obtain the later offset during an overlap, call
     * [ChronoZonedDateTime.withLaterOffsetAtOverlap] on the result of this method.
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date-time, not null
     */
    fun atZone(zone: ZoneId): ChronoZonedDateTime<D>

    /**
     * Converts this date-time to an `Instant`.
     *
     *
     * This combines this local date-time and the specified offset to form
     * an `Instant`.
     *
     *
     * This default implementation calculates from the epoch-day of the date and the
     * second-of-day of the time.
     *
     * @param offset  the offset to use for the conversion, not null
     * @return an `Instant` representing the same instant, not null
     */
    fun toInstant(offset: ZoneOffset): Instant {
        return Instant.ofEpochSecond(toEpochSecond(offset), getTime().nano.toLong())
    }

    /**
     * Converts this date-time to the number of seconds from the epoch
     * of 1970-01-01T00:00:00Z.
     *
     *
     * This combines this local date-time and the specified offset to calculate the
     * epoch-second value, which is the number of elapsed seconds from 1970-01-01T00:00:00Z.
     * Instants on the time-line after the epoch are positive, earlier are negative.
     *
     *
     * This default implementation calculates from the epoch-day of the date and the
     * second-of-day of the time.
     *
     * @param offset  the offset to use for the conversion, not null
     * @return the number of seconds from the epoch of 1970-01-01T00:00:00Z
     */
    fun toEpochSecond(offset: ZoneOffset): Long {
        val epochDay = getDate().toEpochDay()
        var secs = epochDay * 86400 + getTime().toSecondOfDay()
        secs -= offset.totalSeconds.toLong()
        return secs
    }

    /**
     * Checks if the specified unit is supported.
     *
     *
     * This checks if the specified unit can be added to or subtracted from this date-time.
     * If false, then calling the [.plus] and
     * [minus][.minus] methods will throw an exception.
     *
     *
     * The set of supported units is defined by the chronology and normally includes
     * all `ChronoUnit` units except `FOREVER`.
     *
     *
     * If the unit is not a `ChronoUnit`, then the result of this method
     * is obtained by invoking `TemporalUnit.isSupportedBy(Temporal)`
     * passing `this` as the argument.
     * Whether the unit is supported is determined by the unit.
     *
     * @param unit  the unit to check, null returns false
     * @return true if the unit can be added/subtracted, false if not
     */
    override fun isSupported(unit: TemporalUnit): Boolean {
        return if (unit is ChronoUnit) {
            unit !== ChronoUnit.FOREVER
        } else unit.isSupportedBy(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Queries this date-time using the specified query.
     *
     *
     * This queries this date-time using the specified query strategy object.
     * The `TemporalQuery` object defines the logic to be used to
     * obtain the result. Read the documentation of the query to understand
     * what the result of this method will be.
     *
     *
     * The result of this method is obtained by invoking the
     * [TemporalQuery.queryFrom] method on the
     * specified query passing `this` as the argument.
     *
     * @param <R> the type of the result
     * @param query  the query to invoke, not null
     * @return the query result, null may be returned (defined by the query)
     * @throws DateTimeException if unable to query (defined by the query)
     * @throws ArithmeticException if numeric overflow occurs (defined by the query)
     */
    override fun <R> query(query: TemporalQuery<R>): R? {
        if (query == TemporalQueries.ZONE_ID || query == TemporalQueries.ZONE || query == TemporalQueries.OFFSET) {
            return null
        } else if (query == TemporalQueries.LOCAL_TIME) {
            return getTime() as R
        } else if (query == TemporalQueries.CHRONO) {
            return getChronology() as R
        } else if (query == TemporalQueries.PRECISION) {
            return ChronoUnit.NANOS as R
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this)
    }

    /**
     * Adjusts the specified temporal object to have the same date and time as this object.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with the date and time changed to be the same as this.
     *
     *
     * The adjustment is equivalent to using [Temporal.with]
     * twice, passing [ChronoField.EPOCH_DAY] and
     * [ChronoField.NANO_OF_DAY] as the fields.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * [Temporal.with]:
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * temporal = thisLocalDateTime.adjustInto(temporal);
     * temporal = temporal.with(thisLocalDateTime);
     * </pre>
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param temporal  the target object to be adjusted, not null
     * @return the adjusted object, not null
     * @throws DateTimeException if unable to make the adjustment
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun adjustInto(temporal: Temporal): Temporal {
        return temporal
            .with(ChronoField.EPOCH_DAY, getDate().toEpochDay())
            .with(ChronoField.NANO_OF_DAY, getTime().toNanoOfDay())
    }

    /**
     * Compares this date-time to another date-time, including the chronology.
     *
     *
     * The comparison is based first on the underlying time-line date-time, then
     * on the chronology.
     * It is "consistent with equals", as defined by [Comparable].
     *
     *
     * For example, the following is the comparator order:
     *
     *  1. `2012-12-03T12:00 (ISO)`
     *  1. `2012-12-04T12:00 (ISO)`
     *  1. `2555-12-04T12:00 (ThaiBuddhist)`
     *  1. `2012-12-05T12:00 (ISO)`
     *
     * Values #2 and #3 represent the same date-time on the time-line.
     * When two values represent the same date-time, the chronology ID is compared to distinguish them.
     * This step is needed to make the ordering "consistent with equals".
     *
     *
     * If all the date-time objects being compared are in the same chronology, then the
     * additional chronology stage is not required and only the local date-time is used.
     *
     *
     * This default implementation performs the comparison defined above.
     *
     * @param other  the other date-time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    operator fun compareTo(other: ChronoLocalDateTime<*>): Int {
        var cmp = getDate().compareTo(other.getDate())
        if (cmp == 0) {
            cmp = getTime().compareTo(other.getTime())
            if (cmp == 0) {
                cmp = getChronology().compareTo(other.getChronology())
            }
        }
        return cmp
    }

    /**
     * Checks if this date-time is after the specified date-time ignoring the chronology.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the underlying date-time and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     *
     *
     * This default implementation performs the comparison based on the epoch-day
     * and nano-of-day.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this is after the specified date-time
     */
    fun isAfter(other: ChronoLocalDateTime<*>): Boolean {
        val thisEpDay = this.getDate().toEpochDay()
        val otherEpDay = other.getDate().toEpochDay()
        return thisEpDay > otherEpDay ||
                thisEpDay == otherEpDay && this.getTime().toNanoOfDay() > other.getTime().toNanoOfDay()
    }

    /**
     * Checks if this date-time is before the specified date-time ignoring the chronology.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the underlying date-time and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     *
     *
     * This default implementation performs the comparison based on the epoch-day
     * and nano-of-day.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this is before the specified date-time
     */
    fun isBefore(other: ChronoLocalDateTime<*>): Boolean {
        val thisEpDay = this.getDate().toEpochDay()
        val otherEpDay = other.getDate().toEpochDay()
        return thisEpDay < otherEpDay ||
                thisEpDay == otherEpDay && this.getTime().toNanoOfDay() < other.getTime().toNanoOfDay()
    }

    /**
     * Checks if this date-time is equal to the specified date-time ignoring the chronology.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the underlying date and time and not the chronology.
     * This allows date-times in different calendar systems to be compared based
     * on the time-line position.
     *
     *
     * This default implementation performs the comparison based on the epoch-day
     * and nano-of-day.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if the underlying date-time is equal to the specified date-time on the timeline
     */
    fun isEqual(other: ChronoLocalDateTime<*>): Boolean {
        // Do the time check first, it is cheaper than computing EPOCH day.
        return this.getTime().toNanoOfDay() == other.getTime().toNanoOfDay() &&
                this.getDate().toEpochDay() == other.getDate().toEpochDay()
    }
}