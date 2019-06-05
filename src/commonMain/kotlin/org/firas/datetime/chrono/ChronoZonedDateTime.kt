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

import org.firas.datetime.DateTimeException
import org.firas.datetime.Instant
import org.firas.datetime.LocalTime
import org.firas.datetime.ZonedDateTime
import org.firas.datetime.temporal.*
import org.firas.datetime.zone.ZoneId
import org.firas.datetime.zone.ZoneOffset
import org.firas.lang.getName
import org.firas.util.Integers
import kotlin.js.JsName

/**
 * A date-time with a time-zone in an arbitrary chronology,
 * intended for advanced globalization use cases.
 *
 *
 * <b>Most applications should declare method signatures, fields and variables
 * as [ZonedDateTime], not this interface.</b>
 *
 *
 * A `ChronoZonedDateTime` is the abstract representation of an offset date-time
 * where the `Chronology chronology`, or calendar system, is pluggable.
 * The date-time is defined in terms of fields expressed by [TemporalField],
 * where most common implementations are defined in [ChronoField].
 * The chronology defines how the calendar system operates and the meaning of
 * the standard fields.
 *
 *
 * <h3>When to use this interface</h3>
 * The design of the API encourages the use of `ZonedDateTime` rather than this
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
interface ChronoZonedDateTime<D: ChronoLocalDate>: Temporal {

    companion object {
        internal val timeLineOrder = { dateTime1: ChronoZonedDateTime<*>, dateTime2: ChronoZonedDateTime<*> ->
            val cmp = Integers.compare(dateTime1.toEpochSecond(), dateTime2.toEpochSecond())
            if (cmp == 0) {
                Integers.signum(dateTime1.toLocalTime().nano - dateTime2.toLocalTime().nano)
            } else cmp
        } as Comparator<ChronoZonedDateTime<*>>

        /**
         * Obtains an instance of `ChronoZonedDateTime` from a temporal object.
         *
         *
         * This creates a zoned date-time based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `ChronoZonedDateTime`.
         *
         *
         * The conversion extracts and combines the chronology, date, time and zone
         * from the temporal object. The behavior is equivalent to using
         * {@link Chronology#zonedDateTime(TemporalAccessor)} with the extracted chronology.
         * Implementations are permitted to perform optimizations such as accessing
         * those fields that are equivalent to the relevant objects.
         *
         *
         * This method matches the signature of the functional interface [TemporalQuery]
         * allowing it to be used as a query via method reference, `ChronoZonedDateTime::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the date-time, not null
         * @throws DateTimeException if unable to convert to a `ChronoZonedDateTime`
         * @see Chronology#zonedDateTime(TemporalAccessor)
         */
        @JsName("from")
        fun from(temporal: TemporalAccessor): ChronoZonedDateTime<*> {
            if (temporal is ChronoZonedDateTime<*>) {
                return temporal
            }
            val chrono = temporal.query(TemporalQueries.CHRONO)
            if (chrono == null) {
                throw DateTimeException(
                    "Unable to obtain ChronoZonedDateTime from TemporalAccessor: " +
                            temporal::class.getName()
                )
            }
            return chrono.zonedDateTime(temporal)
        }
    } // companion object

    override fun range(field: TemporalField): ValueRange {
        if (field is ChronoField) {
            if (field == ChronoField.INSTANT_SECONDS || field == ChronoField.OFFSET_SECONDS) {
                return field.range()
            }
            return toLocalDateTime().range(field)
        }
        return field.rangeRefinedBy(this)
    }

    override fun get(field: TemporalField): Int {
        if (field is ChronoField) {
            return when (field) {
                ChronoField.INSTANT_SECONDS -> throw UnsupportedTemporalTypeException(
                        "Invalid field 'InstantSeconds' for get() method, use getLong() instead")
                ChronoField.OFFSET_SECONDS -> getOffset().totalSeconds
                else -> toLocalDateTime().get(field)
            }
        }
        TODO("return Temporal.super.get(this, field)")
    }

    override fun getLong(field: TemporalField): Long {
        if (field is ChronoField) {
            return when (field) {
                ChronoField.INSTANT_SECONDS -> toEpochSecond()
                ChronoField.OFFSET_SECONDS -> getOffset().totalSeconds.toLong()
                else -> toLocalDateTime().getLong(field)
            }
        }
        return field.getFrom(this)
    }

    /**
     * Gets the local date part of this date-time.
     *
     *
     * This returns a local date with the same year, month and day
     * as this date-time.
     *
     * @return the date part of this date-time, not null
     */
    fun toLocalDate(): D {
        return toLocalDateTime().getDate()
    }

    /**
     * Gets the local time part of this date-time.
     *
     *
     * This returns a local time with the same hour, minute, second and
     * nanosecond as this date-time.
     *
     * @return the time part of this date-time, not null
     */
    fun toLocalTime(): LocalTime {
        return toLocalDateTime().getTime()
    }

    /**
     * Gets the local date-time part of this date-time.
     *
     *
     * This returns a local date with the same year, month and day
     * as this date-time.
     *
     * @return the local date-time part of this date-time, not null
     */
    fun toLocalDateTime(): ChronoLocalDateTime<D>

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
        return toLocalDate().getChronology()
    }

    /**
     * Gets the zone offset, such as '+01:00'.
     *
     *
     * This is the offset of the local date-time from UTC/Greenwich.
     *
     * @return the zone offset, not null
     */
    fun getOffset(): ZoneOffset

    /**
     * Gets the zone ID, such as 'Europe/Paris'.
     *
     *
     * This returns the stored time-zone id used to determine the time-zone rules.
     *
     * @return the zone ID, not null
     */
    fun getZone(): ZoneId

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time changing the zone offset to the
     * earlier of the two valid offsets at a local time-line overlap.
     *
     *
     * This method only has any effect when the local time-line overlaps, such as
     * at an autumn daylight savings cutover. In this scenario, there are two
     * valid offsets for the local date-time. Calling this method will return
     * a zoned date-time with the earlier of the two selected.
     *
     *
     * If this method is called when it is not an overlap, `this`
     * is returned.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return a `ChronoZonedDateTime` based on this date-time with the earlier offset, not null
     * @throws DateTimeException if no rules can be found for the zone
     * @throws DateTimeException if no rules are valid for this date-time
     */
    fun withEarlierOffsetAtOverlap(): ChronoZonedDateTime<D>

    /**
     * Returns a copy of this date-time changing the zone offset to the
     * later of the two valid offsets at a local time-line overlap.
     *
     *
     * This method only has any effect when the local time-line overlaps, such as
     * at an autumn daylight savings cutover. In this scenario, there are two
     * valid offsets for the local date-time. Calling this method will return
     * a zoned date-time with the later of the two selected.
     *
     *
     * If this method is called when it is not an overlap, `this`
     * is returned.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return a `ChronoZonedDateTime` based on this date-time with the later offset, not null
     * @throws DateTimeException if no rules can be found for the zone
     * @throws DateTimeException if no rules are valid for this date-time
     */
    fun withLaterOffsetAtOverlap(): ChronoZonedDateTime<D>

    /**
     * Returns a copy of this date-time with a different time-zone,
     * retaining the local date-time if possible.
     *
     *
     * This method changes the time-zone and retains the local date-time.
     * The local date-time is only changed if it is invalid for the new zone.
     *
     *
     * To change the zone and adjust the local date-time,
     * use [.withZoneSameInstant].
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to change to, not null
     * @return a `ChronoZonedDateTime` based on this date-time with the requested zone, not null
     */
    fun withZoneSameLocal(zone: ZoneId): ChronoZonedDateTime<D>

    /**
     * Returns a copy of this date-time with a different time-zone,
     * retaining the instant.
     *
     *
     * This method changes the time-zone and retains the instant.
     * This normally results in a change to the local date-time.
     *
     *
     * This method is based on retaining the same instant, thus gaps and overlaps
     * in the local time-line have no effect on the result.
     *
     *
     * To change the offset while keeping the local time,
     * use [.withZoneSameLocal].
     *
     * @param zone  the time-zone to change to, not null
     * @return a `ChronoZonedDateTime` based on this date-time with the requested zone, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun withZoneSameInstant(zone: ZoneId): ChronoZonedDateTime<D>

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
            unit != ChronoUnit.FOREVER
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
    override fun <R> query(query: TemporalQuery<R>): R {
        if (query === TemporalQueries.ZONE || query === TemporalQueries.ZONE_ID) {
            return getZone() as R
        } else if (query === TemporalQueries.OFFSET) {
            return getOffset() as R
        } else if (query === TemporalQueries.LOCAL_TIME) {
            return toLocalTime() as R
        } else if (query === TemporalQueries.CHRONO) {
            return getChronology() as R
        } else if (query === TemporalQueries.PRECISION) {
            return ChronoUnit.NANOS as R
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this date-time to an `Instant`.
     *
     *
     * This returns an `Instant` representing the same point on the
     * time-line as this date-time. The calculation combines the
     * [local date-time][.toLocalDateTime] and
     * [offset][.getOffset].
     *
     * @return an `Instant` representing the same instant, not null
     */
    fun toInstant(): Instant {
        return Instant.ofEpochSecond(toEpochSecond(), toLocalTime().nano.toLong())
    }

    /**
     * Converts this date-time to the number of seconds from the epoch
     * of 1970-01-01T00:00:00Z.
     *
     *
     * This uses the [local date-time][.toLocalDateTime] and
     * [offset][.getOffset] to calculate the epoch-second value,
     * which is the number of elapsed seconds from 1970-01-01T00:00:00Z.
     * Instants on the time-line after the epoch are positive, earlier are negative.
     *
     * @return the number of seconds from the epoch of 1970-01-01T00:00:00Z
     */
    fun toEpochSecond(): Long {
        val epochDay = toLocalDate().toEpochDay()
        var secs = epochDay * 86400 + toLocalTime().toSecondOfDay()
        secs -= getOffset().totalSeconds
        return secs
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this date-time to another date-time, including the chronology.
     *
     *
     * The comparison is based first on the instant, then on the local date-time,
     * then on the zone ID, then on the chronology.
     * It is "consistent with equals", as defined by [Comparable].
     *
     *
     * If all the date-time objects being compared are in the same chronology, then the
     * additional chronology stage is not required.
     *
     *
     * This default implementation performs the comparison defined above.
     *
     * @param other  the other date-time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    operator fun compareTo(other: ChronoZonedDateTime<*>): Int {
        var cmp = Integers.compare(toEpochSecond(), other.toEpochSecond())
        if (cmp == 0) {
            cmp = toLocalTime().nano - other.toLocalTime().nano
            if (cmp == 0) {
                cmp = toLocalDateTime().compareTo(other.toLocalDateTime())
                if (cmp == 0) {
                    cmp = getZone().getId().compareTo(other.getZone().getId())
                    if (cmp == 0) {
                        cmp = getChronology().compareTo(other.getChronology())
                    }
                }
            }
        }
        return cmp
    }

    /**
     * Checks if the instant of this date-time is before that of the specified date-time.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the instant of the date-time. This is equivalent to using
     * `dateTime1.toInstant().isBefore(dateTime2.toInstant());`.
     *
     *
     * This default implementation performs the comparison based on the epoch-second
     * and nano-of-second.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this point is before the specified date-time
     */
    fun isBefore(other: ChronoZonedDateTime<*>): Boolean {
        val thisEpochSec = toEpochSecond()
        val otherEpochSec = other.toEpochSecond()
        return thisEpochSec < otherEpochSec ||
                thisEpochSec == otherEpochSec && toLocalTime().nano < other.toLocalTime().nano
    }

    /**
     * Checks if the instant of this date-time is after that of the specified date-time.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the instant of the date-time. This is equivalent to using
     * `dateTime1.toInstant().isAfter(dateTime2.toInstant());`.
     *
     *
     * This default implementation performs the comparison based on the epoch-second
     * and nano-of-second.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this is after the specified date-time
     */
    fun isAfter(other: ChronoZonedDateTime<*>): Boolean {
        val thisEpochSec = toEpochSecond()
        val otherEpochSec = other.toEpochSecond()
        return thisEpochSec > otherEpochSec ||
                thisEpochSec == otherEpochSec && toLocalTime().nano > other.toLocalTime().nano
    }

    /**
     * Checks if the instant of this date-time is equal to that of the specified date-time.
     *
     *
     * This method differs from the comparison in [.compareTo] and [.equals]
     * in that it only compares the instant of the date-time. This is equivalent to using
     * `dateTime1.toInstant().equals(dateTime2.toInstant());`.
     *
     *
     * This default implementation performs the comparison based on the epoch-second
     * and nano-of-second.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if the instant equals the instant of the specified date-time
     */
    fun isEqual(other: ChronoZonedDateTime<*>): Boolean {
        return toEpochSecond() == other.toEpochSecond() && toLocalTime().nano == other.toLocalTime().nano
    }
}