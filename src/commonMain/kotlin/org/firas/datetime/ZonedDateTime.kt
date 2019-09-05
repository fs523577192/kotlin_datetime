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
package org.firas.datetime

import org.firas.datetime.chrono.ChronoLocalDateTime
import org.firas.datetime.chrono.ChronoZonedDateTime
import org.firas.datetime.chrono.Chronology
import org.firas.datetime.temporal.*
import org.firas.datetime.zone.ZoneId
import org.firas.datetime.zone.ZoneOffset
import org.firas.lang.getName
import org.firas.util.Integers
import kotlin.reflect.KClass

/**
 * A date-time with a time-zone in the ISO-8601 calendar system,
 * such as `2007-12-03T10:15:30+01:00 Europe/Paris`.
 *
 *
 * `ZonedDateTime` is an immutable representation of a date-time with a time-zone.
 * This class stores all date and time fields, to a precision of nanoseconds,
 * and a time-zone, with a zone offset used to handle ambiguous local date-times.
 * For example, the value
 * "2nd October 2007 at 13:45.30.123456789 +02:00 in the Europe/Paris time-zone"
 * can be stored in a `ZonedDateTime`.
 *
 *
 * This class handles conversion from the local time-line of `LocalDateTime`
 * to the instant time-line of `Instant`.
 * The difference between the two time-lines is the offset from UTC/Greenwich,
 * represented by a `ZoneOffset`.
 *
 *
 * Converting between the two time-lines involves calculating the offset using the
 * {@link ZoneRules rules} accessed from the `ZoneId`.
 * Obtaining the offset for an instant is simple, as there is exactly one valid
 * offset for each instant. By contrast, obtaining the offset for a local date-time
 * is not straightforward. There are three cases:
 * <ul>
 * <li>Normal, with one valid offset. For the vast majority of the year, the normal
 *  case applies, where there is a single valid offset for the local date-time.</li>
 * <li>Gap, with zero valid offsets. This is when clocks jump forward typically
 *  due to the spring daylight savings change from "winter" to "summer".
 *  In a gap there are local date-time values with no valid offset.</li>
 * <li>Overlap, with two valid offsets. This is when clocks are set back typically
 *  due to the autumn daylight savings change from "summer" to "winter".
 *  In an overlap there are local date-time values with two valid offsets.</li>
 * </ul>
 *
 *
 * Any method that converts directly or implicitly from a local date-time to an
 * instant by obtaining the offset has the potential to be complicated.
 *
 *
 * For Gaps, the general strategy is that if the local date-time falls in the
 * middle of a Gap, then the resulting zoned date-time will have a local date-time
 * shifted forwards by the length of the Gap, resulting in a date-time in the later
 * offset, typically "summer" time.
 *
 *
 * For Overlaps, the general strategy is that if the local date-time falls in the
 * middle of an Overlap, then the previous offset will be retained. If there is no
 * previous offset, or the previous offset is invalid, then the earlier offset is
 * used, typically "summer" time.. Two additional methods,
 * {@link #withEarlierOffsetAtOverlap()} and {@link #withLaterOffsetAtOverlap()},
 * help manage the case of an overlap.
 *
 *
 * In terms of design, this class should be viewed primarily as the combination
 * of a `LocalDateTime` and a `ZoneId`. The `ZoneOffset` is
 * a vital, but secondary, piece of information, used to ensure that the class
 * represents an instant, especially during a daylight savings overlap.
 *
 *
 *
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `ZonedDateTime` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * A `ZonedDateTime` holds state equivalent to three separate objects,
 * a `LocalDateTime`, a `ZoneId` and the resolved `ZoneOffset`.
 * The offset and local date-time are used to define an instant when necessary.
 * The zone ID is used to obtain the rules for how and when the offset changes.
 * The offset cannot be freely set, as the zone controls which offsets are valid.
 *
 *
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class ZonedDateTime(
    /**
     * The local date-time.
     */
    private val dateTime: LocalDateTime,

    /**
     * The offset from UTC/Greenwich.
     */
    private val offset: ZoneOffset,

    /**
     * The time-zone.
     */
    private val zone: ZoneId
): Temporal, ChronoZonedDateTime<LocalDate> {

    companion object {
        /**
         * Obtains an instance of `ZonedDateTime` from a local date and time.
         *
         *
         * This creates a zoned date-time matching the input local date and time as closely as possible.
         * Time-zone rules, such as daylight savings, mean that not every local date-time
         * is valid for the specified zone, thus the local date-time may be adjusted.
         *
         *
         * The local date time and first combined to form a local date-time.
         * The local date-time is then resolved to a single instant on the time-line.
         * This is achieved by finding a valid offset from UTC/Greenwich for the local
         * date-time as defined by the [rules][ZoneRules] of the zone ID.
         *
         *
         * In most cases, there is only one valid offset for a local date-time.
         * In the case of an overlap, when clocks are set back, there are two valid offsets.
         * This method uses the earlier offset typically corresponding to "summer".
         *
         *
         * In the case of a gap, when clocks jump forward, there is no valid offset.
         * Instead, the local date-time is adjusted to be later by the length of the gap.
         * For a typical one hour daylight savings change, the local date-time will be
         * moved one hour later into the offset typically corresponding to "summer".
         *
         * @param date  the local date, not null
         * @param time  the local time, not null
         * @param zone  the time-zone, not null
         * @return the offset date-time, not null
         */
        fun of(date: LocalDate, time: LocalTime, zone: ZoneId): ZonedDateTime {
            return of(LocalDateTime.of(date, time), zone)
        }

        /**
         * Obtains an instance of `ZonedDateTime` from a local date-time.
         *
         *
         * This creates a zoned date-time matching the input local date-time as closely as possible.
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
         * In the case of an overlap, when clocks are set back, there are two valid offsets.
         * This method uses the earlier offset typically corresponding to "summer".
         *
         *
         * In the case of a gap, when clocks jump forward, there is no valid offset.
         * Instead, the local date-time is adjusted to be later by the length of the gap.
         * For a typical one hour daylight savings change, the local date-time will be
         * moved one hour later into the offset typically corresponding to "summer".
         *
         * @param localDateTime  the local date-time, not null
         * @param zone  the time-zone, not null
         * @return the zoned date-time, not null
         */
        fun of(localDateTime: LocalDateTime, zone: ZoneId): ZonedDateTime {
            return ofLocal(localDateTime, zone, null)
        }

        /**
         * Obtains an instance of `ZonedDateTime` from a year, month, day,
         * hour, minute, second, nanosecond and time-zone.
         *
         *
         * This creates a zoned date-time matching the local date-time of the seven
         * specified fields as closely as possible.
         * Time-zone rules, such as daylight savings, mean that not every local date-time
         * is valid for the specified zone, thus the local date-time may be adjusted.
         *
         *
         * The local date-time is resolved to a single instant on the time-line.
         * This is achieved by finding a valid offset from UTC/Greenwich for the local
         * date-time as defined by the {@link ZoneRules rules} of the zone ID.
         *
         *
         * In most cases, there is only one valid offset for a local date-time.
         * In the case of an overlap, when clocks are set back, there are two valid offsets.
         * This method uses the earlier offset typically corresponding to "summer".
         *
         *
         * In the case of a gap, when clocks jump forward, there is no valid offset.
         * Instead, the local date-time is adjusted to be later by the length of the gap.
         * For a typical one hour daylight savings change, the local date-time will be
         * moved one hour later into the offset typically corresponding to "summer".
         *
         *
         * This method exists primarily for writing test cases.
         * Non test-code will typically use other methods to create an offset time.
         * `LocalDateTime` has five additional convenience variants of the
         * equivalent factory method taking fewer arguments.
         * They are not provided here to reduce the footprint of the API.
         *
         * @param year  the year to represent, from MIN_YEAR to MAX_YEAR
         * @param month  the month-of-year to represent, from 1 (January) to 12 (December)
         * @param dayOfMonth  the day-of-month to represent, from 1 to 31
         * @param hour  the hour-of-day to represent, from 0 to 23
         * @param minute  the minute-of-hour to represent, from 0 to 59
         * @param second  the second-of-minute to represent, from 0 to 59
         * @param nanoOfSecond  the nano-of-second to represent, from 0 to 999,999,999
         * @param zone  the time-zone, not null
         * @return the offset date-time, not null
         * @throws DateTimeException if the value of any field is out of range, or
         *  if the day-of-month is invalid for the month-year
         */
        fun of(
            year: Int, month: Int, dayOfMonth: Int,
            hour: Int, minute: Int, second: Int, nanoOfSecond: Int, zone: ZoneId
        ): ZonedDateTime {
            val dt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond)
            return ofLocal(dt, zone, null)
        }

        /**
         * Obtains an instance of `ZonedDateTime` from a local date-time
         * using the preferred offset if possible.
         *
         *
         * The local date-time is resolved to a single instant on the time-line.
         * This is achieved by finding a valid offset from UTC/Greenwich for the local
         * date-time as defined by the {@link ZoneRules rules} of the zone ID.
         *
         *
         * In most cases, there is only one valid offset for a local date-time.
         * In the case of an overlap, where clocks are set back, there are two valid offsets.
         * If the preferred offset is one of the valid offsets then it is used.
         * Otherwise the earlier valid offset is used, typically corresponding to "summer".
         *
         *
         * In the case of a gap, where clocks jump forward, there is no valid offset.
         * Instead, the local date-time is adjusted to be later by the length of the gap.
         * For a typical one hour daylight savings change, the local date-time will be
         * moved one hour later into the offset typically corresponding to "summer".
         *
         * @param localDateTime  the local date-time, not null
         * @param zone  the time-zone, not null
         * @param preferredOffset  the zone offset, null if no preference
         * @return the zoned date-time, not null
         */
        fun ofLocal(localDateTime: LocalDateTime, zone: ZoneId, preferredOffset: ZoneOffset?): ZonedDateTime {
            if (zone is ZoneOffset) {
                return ZonedDateTime(localDateTime, zone, zone)
            }

            var localDateTime = localDateTime
            val rules = zone.getRules()!!
            val validOffsets: List<ZoneOffset> = rules.getValidOffsets(localDateTime)
            val offset: ZoneOffset
            if (validOffsets.size == 1) {
                offset = validOffsets.get(0)
            } else if (validOffsets.isEmpty()) {
                val trans = rules.getTransition(localDateTime)
                localDateTime = localDateTime.plusSeconds(trans!!.getDuration().seconds)
                offset = trans.offsetAfter
            } else {
                offset = if (preferredOffset != null && validOffsets.contains(preferredOffset)) {
                    preferredOffset
                } else {
                    validOffsets.get(0)  // protect against bad ZoneRules
                }
            }
            return ZonedDateTime(localDateTime, offset, zone)
        }

        //-----------------------------------------------------------------------

        /**
         * Obtains an instance of `ZonedDateTime` from an `Instant`.
         *
         *
         * This creates a zoned date-time with the same instant as that specified.
         * Calling [.toInstant] will return an instant equal to the one used here.
         *
         *
         * Converting an instant to a zoned date-time is simple as there is only one valid
         * offset for each instant.
         *
         *
         * @param instant  the instant to create the date-time from, not null
         * @param zone  the time-zone, not null
         * @return the zoned date-time, not null
         * @throws DateTimeException if the result exceeds the supported range
         */

        fun ofInstant(instant: Instant, zone: ZoneId): ZonedDateTime {
            return create(instant.epochSecond, instant.nanos, zone)
        }

        /**
         * Obtains an instance of `ZonedDateTime` from the instant formed by combining
         * the local date-time and offset.
         *
         *
         * This creates a zoned date-time by [combining][LocalDateTime.toInstant]
         * the `LocalDateTime` and `ZoneOffset`.
         * This combination uniquely specifies an instant without ambiguity.
         *
         *
         * Converting an instant to a zoned date-time is simple as there is only one valid
         * offset for each instant. If the valid offset is different to the offset specified,
         * then the date-time and offset of the zoned date-time will differ from those specified.
         *
         *
         * If the `ZoneId` to be used is a `ZoneOffset`, this method is equivalent
         * to [.of].
         *
         * @param localDateTime  the local date-time, not null
         * @param offset  the zone offset, not null
         * @param zone  the time-zone, not null
         * @return the zoned date-time, not null
         */
        fun ofInstant(localDateTime: LocalDateTime, offset: ZoneOffset, zone: ZoneId): ZonedDateTime {
            return if (zone.getRules()!!.isValidOffset(localDateTime, offset)) {
                ZonedDateTime(localDateTime, offset, zone)
            } else {
                create(localDateTime.toEpochSecond(offset), localDateTime.getNano(), zone)
            }
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains an instance of `ZonedDateTime` from a temporal object.
         *
         *
         * This obtains a zoned date-time based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `ZonedDateTime`.
         *
         *
         * The conversion will first obtain a `ZoneId` from the temporal object,
         * falling back to a `ZoneOffset` if necessary. It will then try to obtain
         * an `Instant`, falling back to a `LocalDateTime` if necessary.
         * The result will be either the combination of `ZoneId` or `ZoneOffset`
         * with `Instant` or `LocalDateTime`.
         * Implementations are permitted to perform optimizations such as accessing
         * those fields that are equivalent to the relevant objects.
         *
         *
         * This method matches the signature of the functional interface {@link TemporalQuery}
         * allowing it to be used as a query via method reference, `ZonedDateTime::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the zoned date-time, not null
         * @throws DateTimeException if unable to convert to an `ZonedDateTime`
         */
        fun from(temporal: TemporalAccessor): ZonedDateTime {
            if (temporal is ZonedDateTime) {
                return temporal
            }
            try {
                val zone = ZoneId.from(temporal)
                return if (temporal.isSupported(ChronoField.INSTANT_SECONDS)) {
                    val epochSecond = temporal.getLong(ChronoField.INSTANT_SECONDS)
                    val nanoOfSecond = temporal.get(ChronoField.NANO_OF_SECOND)
                    create(epochSecond, nanoOfSecond, zone)
                } else {
                    val date = LocalDate.from(temporal)
                    val time = LocalTime.from(temporal)
                    of(date, time, zone)
                }
            } catch (ex: DateTimeException) {
                throw DateTimeException("Unable to obtain ZonedDateTime from TemporalAccessor: " +
                        temporal + " of type " + temporal::class.getName(), ex)
            }
        }

        /**
         * Obtains an instance of `ZonedDateTime` using seconds from the
         * epoch of 1970-01-01T00:00:00Z.
         *
         * @param epochSecond  the number of seconds from the epoch of 1970-01-01T00:00:00Z
         * @param nanoOfSecond  the nanosecond within the second, from 0 to 999,999,999
         * @param zone  the time-zone, not null
         * @return the zoned date-time, not null
         * @throws DateTimeException if the result exceeds the supported range
         */
        private fun create(epochSecond: Long, nanoOfSecond: Int, zone: ZoneId): ZonedDateTime {
            val rules = zone.getRules()
            val instant = Instant.ofEpochSecond(epochSecond, nanoOfSecond.toLong())
            // TODO: rules should be queryable by epochSeconds
            val offset = rules!!.getOffset(instant)!!
            val ldt = LocalDateTime.ofEpochSecond(epochSecond, nanoOfSecond, offset)
            return ZonedDateTime(ldt, offset, zone)
        }
    } // companion object

    override fun toLocalDateTime(): LocalDateTime {
        return dateTime
    }

    override fun getOffset(): ZoneOffset {
        return this.offset
    }

    override fun getZone(): ZoneId {
        return this.zone
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the `LocalDate` part of this date-time.
     *
     *
     * This returns a `LocalDate` with the same year, month and day
     * as this date-time.
     *
     * @return the date part of this date-time, not null
     */
    override// override for return type
    fun toLocalDate(): LocalDate {
        return dateTime.getDate()
    }

    /**
     * Gets the year field.
     *
     *
     * This method returns the primitive `int` value for the year.
     *
     *
     * The year returned by this method is proleptic as per `get(YEAR)`.
     * To obtain the year-of-era, use `get(YEAR_OF_ERA)`.
     *
     * @return the year, from MIN_YEAR to MAX_YEAR
     */
    fun getYear(): Int {
        return dateTime.getYear()
    }

    /**
     * Gets the month-of-year field from 1 to 12.
     *
     *
     * This method returns the month as an `int` from 1 to 12.
     * Application code is frequently clearer if the enum [Month]
     * is used by calling [.getMonth].
     *
     * @return the month-of-year, from 1 to 12
     * @see .getMonth
     */
    fun getMonthValue(): Int {
        return dateTime.getMonthValue()
    }

    /**
     * Gets the month-of-year field using the `Month` enum.
     *
     *
     * This method returns the enum [Month] for the month.
     * This avoids confusion as to what `int` values mean.
     * If you need access to the primitive `int` value then the enum
     * provides the [int value][Month.getValue].
     *
     * @return the month-of-year, not null
     * @see .getMonthValue
     */
    fun getMonth(): Month {
        return dateTime.getMonth()
    }

    /**
     * Gets the day-of-month field.
     *
     *
     * This method returns the primitive `int` value for the day-of-month.
     *
     * @return the day-of-month, from 1 to 31
     */
    fun getDayOfMonth(): Int {
        return dateTime.getDayOfMonth()
    }

    /**
     * Gets the day-of-year field.
     *
     *
     * This method returns the primitive `int` value for the day-of-year.
     *
     * @return the day-of-year, from 1 to 365, or 366 in a leap year
     */
    fun getDayOfYear(): Int {
        return dateTime.getDayOfYear()
    }

    /**
     * Gets the day-of-week field, which is an enum `DayOfWeek`.
     *
     *
     * This method returns the enum [DayOfWeek] for the day-of-week.
     * This avoids confusion as to what `int` values mean.
     * If you need access to the primitive `int` value then the enum
     * provides the [int value][DayOfWeek.getValue].
     *
     *
     * Additional information can be obtained from the `DayOfWeek`.
     * This includes textual names of the values.
     *
     * @return the day-of-week, not null
     */
    fun getDayOfWeek(): DayOfWeek {
        return dateTime.getDayOfWeek()
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the `LocalTime` part of this date-time.
     *
     *
     * This returns a `LocalTime` with the same hour, minute, second and
     * nanosecond as this date-time.
     *
     * @return the time part of this date-time, not null
     */
    override fun toLocalTime(): LocalTime {
        return dateTime.getTime()
    }

    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    fun getHour(): Int {
        return dateTime.getHour()
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    fun getMinute(): Int {
        return dateTime.getMinute()
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    fun getSecond(): Int {
        return dateTime.getSecond()
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    fun getNano(): Int {
        return dateTime.getNano()
    }

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
     * @return a `ZonedDateTime` based on this date-time with the earlier offset, not null
     */
    override fun withEarlierOffsetAtOverlap(): ZonedDateTime {
        val trans = getZone().getRules()!!.getTransition(this.dateTime)
        if (trans != null && trans.isOverlap()) {
            val earlierOffset = trans.offsetBefore
            if (earlierOffset != this.offset) {
                return ZonedDateTime(this.dateTime, earlierOffset, this.zone)
            }
        }
        return this
    }

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
     * @return a `ZonedDateTime` based on this date-time with the later offset, not null
     */
    override fun withLaterOffsetAtOverlap(): ZonedDateTime {
        val trans = getZone().getRules()!!.getTransition(toLocalDateTime())
        if (trans != null) {
            val laterOffset = trans.offsetAfter
            if (laterOffset != this.offset) {
                return ZonedDateTime(this.dateTime, laterOffset, this.zone)
            }
        }
        return this
    }

    /**
     * Returns a copy of this date-time with a different time-zone,
     * retaining the local date-time if possible.
     *
     *
     * This method changes the time-zone and retains the local date-time.
     * The local date-time is only changed if it is invalid for the new zone,
     * determined using the same approach as
     * {@link #ofLocal(LocalDateTime, ZoneId, ZoneOffset)}.
     *
     *
     * To change the zone and adjust the local date-time,
     * use {@link #withZoneSameInstant(ZoneId)}.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param zone  the time-zone to change to, not null
     * @return a `ZonedDateTime` based on this date-time with the requested zone, not null
     */
    override fun withZoneSameLocal(zone: ZoneId): ZonedDateTime {
        return if (this.zone == zone) this else ofLocal(this.dateTime, zone, this.offset)
    }

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
     * use {@link #withZoneSameLocal(ZoneId)}.
     *
     * @param zone  the time-zone to change to, not null
     * @return a `ZonedDateTime` based on this date-time with the requested zone, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    override fun withZoneSameInstant(zone: ZoneId): ZonedDateTime {
        return if (this.zone == zone) this else
            create(this.dateTime.toEpochSecond(this.offset), this.dateTime.getNano(), zone)
    }

    /**
     * Returns a copy of this date-time with the zone ID set to the offset.
     *
     *
     * This returns a zoned date-time where the zone ID is the same as {@link #getOffset()}.
     * The local date-time, offset and instant of the result will be the same as in this date-time.
     *
     *
     * Setting the date-time to a fixed single offset means that any future
     * calculations, such as addition or subtraction, have no complex edge cases
     * due to time-zone rules.
     * This might also be useful when sending a zoned date-time across a network,
     * as most protocols, such as ISO-8601, only handle offsets,
     * and not region-based zone IDs.
     *
     *
     * This is equivalent to `ZonedDateTime.of(zdt.toLocalDateTime(), zdt.getOffset())`.
     *
     * @return a `ZonedDateTime` with the zone ID set to the offset, not null
     */
    fun withFixedOffsetZone(): ZonedDateTime {
        return if (this.zone == this.offset) this else ZonedDateTime(this.dateTime, this.offset, this.offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the specified field is supported.
     *
     *
     * This checks if this date-time can be queried for the specified field.
     * If false, then calling the [range][.range],
     * [get][.get] and [.with]
     * methods will throw an exception.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The supported fields are:
     *
     *  * `NANO_OF_SECOND`
     *  * `NANO_OF_DAY`
     *  * `MICRO_OF_SECOND`
     *  * `MICRO_OF_DAY`
     *  * `MILLI_OF_SECOND`
     *  * `MILLI_OF_DAY`
     *  * `SECOND_OF_MINUTE`
     *  * `SECOND_OF_DAY`
     *  * `MINUTE_OF_HOUR`
     *  * `MINUTE_OF_DAY`
     *  * `HOUR_OF_AMPM`
     *  * `CLOCK_HOUR_OF_AMPM`
     *  * `HOUR_OF_DAY`
     *  * `CLOCK_HOUR_OF_DAY`
     *  * `AMPM_OF_DAY`
     *  * `DAY_OF_WEEK`
     *  * `ALIGNED_DAY_OF_WEEK_IN_MONTH`
     *  * `ALIGNED_DAY_OF_WEEK_IN_YEAR`
     *  * `DAY_OF_MONTH`
     *  * `DAY_OF_YEAR`
     *  * `EPOCH_DAY`
     *  * `ALIGNED_WEEK_OF_MONTH`
     *  * `ALIGNED_WEEK_OF_YEAR`
     *  * `MONTH_OF_YEAR`
     *  * `PROLEPTIC_MONTH`
     *  * `YEAR_OF_ERA`
     *  * `YEAR`
     *  * `ERA`
     *  * `INSTANT_SECONDS`
     *  * `OFFSET_SECONDS`
     *
     * All other `ChronoField` instances will return false.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.isSupportedBy(TemporalAccessor)`
     * passing `this` as the argument.
     * Whether the field is supported is determined by the field.
     *
     * @param field  the field to check, null returns false
     * @return true if the field is supported on this date-time, false if not
     */
    override fun isSupported(field: TemporalField): Boolean {
        return field is ChronoField || field.isSupportedBy(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the range of valid values for the specified field.
     *
     *
     * The range object expresses the minimum and maximum valid values for a field.
     * This date-time is used to enhance the accuracy of the returned range.
     * If it is not possible to return the range, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The [supported fields][.isSupported] will return
     * appropriate range instances.
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.rangeRefinedBy(TemporalAccessor)`
     * passing `this` as the argument.
     * Whether the range can be obtained is determined by the field.
     *
     * @param field  the field to query the range for, not null
     * @return the range of valid values for the field, not null
     * @throws DateTimeException if the range for the field cannot be obtained
     * @throws UnsupportedTemporalTypeException if the field is not supported
     */
    override fun range(field: TemporalField): ValueRange {
        return if (field is ChronoField) {
            if (field === ChronoField.INSTANT_SECONDS || field === ChronoField.OFFSET_SECONDS) {
                field.range()
            } else dateTime.range(field)
        } else {
            field.rangeRefinedBy(this)
        }
    }

    /**
     * Gets the value of the specified field from this date-time as an `int`.
     * <p>
     * This queries this date-time for the value of the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     * <p>
     * If the field is a {@link ChronoField} then the query is implemented here.
     * The {@link #isSupported(TemporalField) supported fields} will return valid
     * values based on this date-time, except `NANO_OF_DAY`, `MICRO_OF_DAY`,
     * `EPOCH_DAY`, `PROLEPTIC_MONTH` and `INSTANT_SECONDS` which are too
     * large to fit in an `int` and throw an `UnsupportedTemporalTypeException`.
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     * <p>
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.getFrom(TemporalAccessor)`
     * passing `this` as the argument. Whether the value can be obtained,
     * and what the value represents, is determined by the field.
     *
     * @param field  the field to get, not null
     * @return the value for the field
     * @throws DateTimeException if a value for the field cannot be obtained or
     *         the value is outside the range of valid values for the field
     * @throws UnsupportedTemporalTypeException if the field is not supported or
     *         the range of values exceeds an `int`
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun get(field: TemporalField): Int {
        if (field is ChronoField) {
            return when(field) {
                ChronoField.INSTANT_SECONDS ->
                    throw UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead")
                ChronoField.OFFSET_SECONDS ->
                    getOffset().totalSeconds
                else -> dateTime.get(field)
            }
        }
        return ChronoZonedDateTime.get(this, field)
    }

    /**
     * Gets the value of the specified field from this date-time as a `long`.
     *
     *
     * This queries this date-time for the value of the specified field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The [supported fields][.isSupported] will return valid
     * values based on this date-time.
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.getFrom(TemporalAccessor)`
     * passing `this` as the argument. Whether the value can be obtained,
     * and what the value represents, is determined by the field.
     *
     * @param field  the field to get, not null
     * @return the value for the field
     * @throws DateTimeException if a value for the field cannot be obtained
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun getLong(field: TemporalField): Long {
        if (field is ChronoField) {
            return when (field) {
                ChronoField.INSTANT_SECONDS -> toEpochSecond()
                ChronoField.OFFSET_SECONDS -> getOffset().totalSeconds.toLong()
                else -> dateTime.getLong(field)
            }
        }
        return field.getFrom(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns an adjusted copy of this date-time.
     *
     *
     * This returns a `ZonedDateTime`, based on this one, with the date-time adjusted.
     * The adjustment takes place using the specified adjuster strategy object.
     * Read the documentation of the adjuster to understand what adjustment will be made.
     *
     *
     * A simple adjuster might simply set the one of the fields, such as the year field.
     * A more complex adjuster might set the date to the last day of the month.
     * A selection of common adjustments is provided in
     * [TemporalAdjusters][java.time.temporal.TemporalAdjusters].
     * These include finding the "last day of the month" and "next Wednesday".
     * Key date-time classes also implement the `TemporalAdjuster` interface,
     * such as [Month] and [MonthDay][java.time.MonthDay].
     * The adjuster is responsible for handling special cases, such as the varying
     * lengths of month and leap years.
     *
     *
     * For example this code returns a date on the last day of July:
     * <pre>
     * import static java.time.Month.*;
     * import static java.time.temporal.TemporalAdjusters.*;
     *
     * result = zonedDateTime.with(JULY).with(lastDayOfMonth());
     * </pre>
     *
     *
     * The classes [LocalDate] and [LocalTime] implement `TemporalAdjuster`,
     * thus this method can be used to change the date, time or offset:
     * <pre>
     * result = zonedDateTime.with(date);
     * result = zonedDateTime.with(time);
     * </pre>
     *
     *
     * [ZoneOffset] also implements `TemporalAdjuster` however using it
     * as an argument typically has no effect. The offset of a `ZonedDateTime` is
     * controlled primarily by the time-zone. As such, changing the offset does not generally
     * make sense, because there is only one valid offset for the local date-time and zone.
     * If the zoned date-time is in a daylight savings overlap, then the offset is used
     * to switch between the two valid offsets. In all other cases, the offset is ignored.
     *
     *
     * The result of this method is obtained by invoking the
     * [TemporalAdjuster.adjustInto] method on the
     * specified adjuster passing `this` as the argument.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param adjuster the adjuster to use, not null
     * @return a `ZonedDateTime` based on `this` with the adjustment made, not null
     * @throws DateTimeException if the adjustment cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(adjuster: TemporalAdjuster): ZonedDateTime {
        // optimizations
        return if (adjuster is LocalDate) {
            resolveLocal(LocalDateTime.of(adjuster, dateTime.getTime()))
        } else if (adjuster is LocalTime) {
            resolveLocal(LocalDateTime.of(dateTime.getDate(), adjuster))
        } else if (adjuster is LocalDateTime) {
            resolveLocal(adjuster)
        } else if (adjuster is OffsetDateTime) {
            ofLocal(adjuster.localDateTime, zone, adjuster.offset)
        } else if (adjuster is Instant) {
            create(adjuster.epochSecond, adjuster.nanos, zone)
        } else if (adjuster is ZoneOffset) {
            resolveOffset(adjuster)
        } else {
            adjuster.adjustInto(this) as ZonedDateTime
        }
    }

    /**
     * Returns a copy of this date-time with the specified field set to a new value.
     *
     *
     * This returns a `ZonedDateTime`, based on this one, with the value
     * for the specified field changed.
     * This can be used to change any supported field, such as the year, month or day-of-month.
     * If it is not possible to set the value, because the field is not supported or for
     * some other reason, an exception is thrown.
     *
     *
     * In some cases, changing the specified field can cause the resulting date-time to become invalid,
     * such as changing the month from 31st January to February would make the day-of-month invalid.
     * In cases like this, the field is responsible for resolving the date. Typically it will choose
     * the previous valid date, which would be the last valid day of February in this example.
     *
     *
     * If the field is a [ChronoField] then the adjustment is implemented here.
     *
     *
     * The `INSTANT_SECONDS` field will return a date-time with the specified instant.
     * The zone and nano-of-second are unchanged.
     * The result will have an offset derived from the new instant and original zone.
     * If the new instant value is outside the valid range then a `DateTimeException` will be thrown.
     *
     *
     * The `OFFSET_SECONDS` field will typically be ignored.
     * The offset of a `ZonedDateTime` is controlled primarily by the time-zone.
     * As such, changing the offset does not generally make sense, because there is only
     * one valid offset for the local date-time and zone.
     * If the zoned date-time is in a daylight savings overlap, then the offset is used
     * to switch between the two valid offsets. In all other cases, the offset is ignored.
     * If the new offset value is outside the valid range then a `DateTimeException` will be thrown.
     *
     *
     * The other [supported fields][.isSupported] will behave as per
     * the matching method on [LocalDateTime][LocalDateTime.with].
     * The zone is not part of the calculation and will be unchanged.
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * All other `ChronoField` instances will throw an `UnsupportedTemporalTypeException`.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.adjustInto(Temporal, long)`
     * passing `this` as the argument. In this case, the field determines
     * whether and how to adjust the instant.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param field  the field to set in the result, not null
     * @param newValue  the new value of the field in the result
     * @return a `ZonedDateTime` based on `this` with the specified field set, not null
     * @throws DateTimeException if the field cannot be set
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(field: TemporalField, newValue: Long): ZonedDateTime {
        if (field is ChronoField) {
            return when (field) {
                ChronoField.INSTANT_SECONDS ->
                    create(newValue, getNano(), zone)
                ChronoField.OFFSET_SECONDS -> {
                    val offset = ZoneOffset.ofTotalSeconds(field.checkValidIntValue(newValue))
                    resolveOffset(offset)
                }
                else ->
                    resolveLocal(dateTime.with(field, newValue))
            }
        }
        return field.adjustInto(this, newValue)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `ZonedDateTime` with the year altered.
     *
     *
     * This operates on the local time-line,
     * [changing the year][LocalDateTime.withYear] of the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the result, from MIN_YEAR to MAX_YEAR
     * @return a `ZonedDateTime` based on this date-time with the requested year, not null
     * @throws DateTimeException if the year value is invalid
     */
    fun withYear(year: Int): ZonedDateTime {
        return resolveLocal(dateTime.withYear(year))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the month-of-year altered.
     *
     *
     * This operates on the local time-line,
     * [changing the month][LocalDateTime.withMonth] of the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param month  the month-of-year to set in the result, from 1 (January) to 12 (December)
     * @return a `ZonedDateTime` based on this date-time with the requested month, not null
     * @throws DateTimeException if the month-of-year value is invalid
     */
    fun withMonth(month: Int): ZonedDateTime {
        return resolveLocal(dateTime.withMonth(month))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the day-of-month altered.
     *
     *
     * This operates on the local time-line,
     * [changing the day-of-month][LocalDateTime.withDayOfMonth] of the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the result, from 1 to 28-31
     * @return a `ZonedDateTime` based on this date-time with the requested day, not null
     * @throws DateTimeException if the day-of-month value is invalid,
     * or if the day-of-month is invalid for the month-year
     */
    fun withDayOfMonth(dayOfMonth: Int): ZonedDateTime {
        return resolveLocal(dateTime.withDayOfMonth(dayOfMonth))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the day-of-year altered.
     *
     *
     * This operates on the local time-line,
     * [changing the day-of-year][LocalDateTime.withDayOfYear] of the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the result, from 1 to 365-366
     * @return a `ZonedDateTime` based on this date with the requested day, not null
     * @throws DateTimeException if the day-of-year value is invalid,
     * or if the day-of-year is invalid for the year
     */
    fun withDayOfYear(dayOfYear: Int): ZonedDateTime {
        return resolveLocal(dateTime.withDayOfYear(dayOfYear))
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `ZonedDateTime` with the hour-of-day altered.
     *
     *
     * This operates on the local time-line,
     * [changing the time][LocalDateTime.withHour] of the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to set in the result, from 0 to 23
     * @return a `ZonedDateTime` based on this date-time with the requested hour, not null
     * @throws DateTimeException if the hour value is invalid
     */
    fun withHour(hour: Int): ZonedDateTime {
        return resolveLocal(dateTime.withHour(hour))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the minute-of-hour altered.
     *
     *
     * This operates on the local time-line,
     * [changing the time][LocalDateTime.withMinute] of the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minute  the minute-of-hour to set in the result, from 0 to 59
     * @return a `ZonedDateTime` based on this date-time with the requested minute, not null
     * @throws DateTimeException if the minute value is invalid
     */
    fun withMinute(minute: Int): ZonedDateTime {
        return resolveLocal(dateTime.withMinute(minute))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the second-of-minute altered.
     *
     *
     * This operates on the local time-line,
     * [changing the time][LocalDateTime.withSecond] of the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param second  the second-of-minute to set in the result, from 0 to 59
     * @return a `ZonedDateTime` based on this date-time with the requested second, not null
     * @throws DateTimeException if the second value is invalid
     */
    fun withSecond(second: Int): ZonedDateTime {
        return resolveLocal(dateTime.withSecond(second))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the nano-of-second altered.
     *
     *
     * This operates on the local time-line,
     * [changing the time][LocalDateTime.withNano] of the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to set in the result, from 0 to 999,999,999
     * @return a `ZonedDateTime` based on this date-time with the requested nanosecond, not null
     * @throws DateTimeException if the nano value is invalid
     */
    fun withNano(nanoOfSecond: Int): ZonedDateTime {
        return resolveLocal(dateTime.withNano(nanoOfSecond))
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `ZonedDateTime` with the time truncated.
     *
     *
     * Truncation returns a copy of the original date-time with fields
     * smaller than the specified unit set to zero.
     * For example, truncating with the [minutes][ChronoUnit.MINUTES] unit
     * will set the second-of-minute and nano-of-second field to zero.
     *
     *
     * The unit must have a [duration][TemporalUnit.getDuration]
     * that divides into the length of a standard day without remainder.
     * This includes all supplied time units on [ChronoUnit] and
     * [DAYS][ChronoUnit.DAYS]. Other units throw an exception.
     *
     *
     * This operates on the local time-line,
     * [truncating][LocalDateTime.truncatedTo]
     * the underlying local date-time. This is then converted back to a
     * `ZonedDateTime`, using the zone ID to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param unit  the unit to truncate to, not null
     * @return a `ZonedDateTime` based on this date-time with the time truncated, not null
     * @throws DateTimeException if unable to truncate
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     */
    fun truncatedTo(unit: TemporalUnit): ZonedDateTime {
        return resolveLocal(dateTime.truncatedTo(unit))
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified amount added.
     *
     *
     * This returns a `ZonedDateTime`, based on this one, with the specified amount added.
     * The amount is typically [Period] or [Duration] but may be
     * any other type implementing the [TemporalAmount] interface.
     *
     *
     * The calculation is delegated to the amount object by calling
     * [TemporalAmount.addTo]. The amount implementation is free
     * to implement the addition in any way it wishes, however it typically
     * calls back to [.plus]. Consult the documentation
     * of the amount implementation to determine if it can be successfully added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToAdd  the amount to add, not null
     * @return a `ZonedDateTime` based on this date-time with the addition made, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: TemporalAmount): ZonedDateTime {
        if (amountToAdd is Period) {
            return resolveLocal(dateTime.plus(amountToAdd))
        }
        return amountToAdd.addTo(this) as ZonedDateTime
    }

    /**
     * Returns a copy of this date-time with the specified amount added.
     *
     *
     * This returns a `ZonedDateTime`, based on this one, with the amount
     * in terms of the unit added. If it is not possible to add the amount, because the
     * unit is not supported or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoUnit] then the addition is implemented here.
     * The zone is not part of the calculation and will be unchanged in the result.
     * The calculation for date and time units differ.
     *
     *
     * Date units operate on the local time-line.
     * The period is first added to the local date-time, then converted back
     * to a zoned date-time using the zone ID.
     * The conversion uses [.ofLocal]
     * with the offset before the addition.
     *
     *
     * Time units operate on the instant time-line.
     * The period is first added to the local date-time, then converted back to
     * a zoned date-time using the zone ID.
     * The conversion uses [.ofInstant]
     * with the offset before the addition.
     *
     *
     * If the field is not a `ChronoUnit`, then the result of this method
     * is obtained by invoking `TemporalUnit.addTo(Temporal, long)`
     * passing `this` as the argument. In this case, the unit determines
     * whether and how to perform the addition.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToAdd  the amount of the unit to add to the result, may be negative
     * @param unit  the unit of the amount to add, not null
     * @return a `ZonedDateTime` based on this date-time with the specified amount added, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: Long, unit: TemporalUnit): ZonedDateTime {
        return if (unit is ChronoUnit) {
            if (unit.isDateBased()) {
                resolveLocal(dateTime.plus(amountToAdd, unit))
            } else {
                resolveInstant(dateTime.plus(amountToAdd, unit))
            }
        } else {
            unit.addTo(this, amountToAdd)
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of years added.
     *
     *
     * This operates on the local time-line,
     * [adding years][LocalDateTime.plusYears] to the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, may be negative
     * @return a `ZonedDateTime` based on this date-time with the years added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusYears(years: Long): ZonedDateTime {
        return resolveLocal(dateTime.plusYears(years))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of months added.
     *
     *
     * This operates on the local time-line,
     * [adding months][LocalDateTime.plusMonths] to the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, may be negative
     * @return a `ZonedDateTime` based on this date-time with the months added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusMonths(months: Long): ZonedDateTime {
        return resolveLocal(dateTime.plusMonths(months))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of weeks added.
     *
     *
     * This operates on the local time-line,
     * [adding weeks][LocalDateTime.plusWeeks] to the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to add, may be negative
     * @return a `ZonedDateTime` based on this date-time with the weeks added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusWeeks(weeks: Long): ZonedDateTime {
        return resolveLocal(dateTime.plusWeeks(weeks))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of days added.
     *
     *
     * This operates on the local time-line,
     * [adding days][LocalDateTime.plusDays] to the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, may be negative
     * @return a `ZonedDateTime` based on this date-time with the days added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusDays(days: Long): ZonedDateTime {
        return resolveLocal(dateTime.plusDays(days))
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of hours added.
     *
     *
     * This operates on the instant time-line, such that adding one hour will
     * always be a duration of one hour later.
     * This may cause the local date-time to change by an amount other than one hour.
     * Note that this is a different approach to that used by days, months and years,
     * thus adding one day is not the same as adding 24 hours.
     *
     *
     * For example, consider a time-zone, such as 'Europe/Paris', where the
     * Autumn DST cutover means that the local times 02:00 to 02:59 occur twice
     * changing from offset +02:00 in summer to +01:00 in winter.
     *
     *  * Adding one hour to 01:30+02:00 will result in 02:30+02:00
     * (both in summer time)
     *  * Adding one hour to 02:30+02:00 will result in 02:30+01:00
     * (moving from summer to winter time)
     *  * Adding one hour to 02:30+01:00 will result in 03:30+01:00
     * (both in winter time)
     *  * Adding three hours to 01:30+02:00 will result in 03:30+01:00
     * (moving from summer to winter time)
     *
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, may be negative
     * @return a `ZonedDateTime` based on this date-time with the hours added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusHours(hours: Long): ZonedDateTime {
        return resolveInstant(dateTime.plusHours(hours))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of minutes added.
     *
     *
     * This operates on the instant time-line, such that adding one minute will
     * always be a duration of one minute later.
     * This may cause the local date-time to change by an amount other than one minute.
     * Note that this is a different approach to that used by days, months and years.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add, may be negative
     * @return a `ZonedDateTime` based on this date-time with the minutes added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusMinutes(minutes: Long): ZonedDateTime {
        return resolveInstant(dateTime.plusMinutes(minutes))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of seconds added.
     *
     *
     * This operates on the instant time-line, such that adding one second will
     * always be a duration of one second later.
     * This may cause the local date-time to change by an amount other than one second.
     * Note that this is a different approach to that used by days, months and years.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add, may be negative
     * @return a `ZonedDateTime` based on this date-time with the seconds added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusSeconds(seconds: Long): ZonedDateTime {
        return resolveInstant(dateTime.plusSeconds(seconds))
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of nanoseconds added.
     *
     *
     * This operates on the instant time-line, such that adding one nano will
     * always be a duration of one nano later.
     * This may cause the local date-time to change by an amount other than one nano.
     * Note that this is a different approach to that used by days, months and years.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to add, may be negative
     * @return a `ZonedDateTime` based on this date-time with the nanoseconds added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusNanos(nanos: Long): ZonedDateTime {
        return resolveInstant(dateTime.plusNanos(nanos))
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified amount subtracted.
     *
     *
     * This returns a `ZonedDateTime`, based on this one, with the specified amount subtracted.
     * The amount is typically [Period] or [Duration] but may be
     * any other type implementing the [TemporalAmount] interface.
     *
     *
     * The calculation is delegated to the amount object by calling
     * [TemporalAmount.subtractFrom]. The amount implementation is free
     * to implement the subtraction in any way it wishes, however it typically
     * calls back to [.minus]. Consult the documentation
     * of the amount implementation to determine if it can be successfully subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToSubtract  the amount to subtract, not null
     * @return a `ZonedDateTime` based on this date-time with the subtraction made, not null
     * @throws DateTimeException if the subtraction cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun minus(amountToSubtract: TemporalAmount): ZonedDateTime {
        if (amountToSubtract is Period) {
            return resolveLocal(this.dateTime.minus(amountToSubtract) as LocalDateTime)
        }
        return amountToSubtract.subtractFrom(this) as ZonedDateTime
    }

    /**
     * Returns a copy of this date-time with the specified amount subtracted.
     *
     *
     * This returns a `ZonedDateTime`, based on this one, with the amount
     * in terms of the unit subtracted. If it is not possible to subtract the amount,
     * because the unit is not supported or for some other reason, an exception is thrown.
     *
     *
     * The calculation for date and time units differ.
     *
     *
     * Date units operate on the local time-line.
     * The period is first subtracted from the local date-time, then converted back
     * to a zoned date-time using the zone ID.
     * The conversion uses [.ofLocal]
     * with the offset before the subtraction.
     *
     *
     * Time units operate on the instant time-line.
     * The period is first subtracted from the local date-time, then converted back to
     * a zoned date-time using the zone ID.
     * The conversion uses [.ofInstant]
     * with the offset before the subtraction.
     *
     *
     * This method is equivalent to [.plus] with the amount negated.
     * See that method for a full description of how addition, and thus subtraction, works.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToSubtract  the amount of the unit to subtract from the result, may be negative
     * @param unit  the unit of the amount to subtract, not null
     * @return a `ZonedDateTime` based on this date-time with the specified amount subtracted, not null
     * @throws DateTimeException if the subtraction cannot be made
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun minus(amountToSubtract: Long, unit: TemporalUnit): ZonedDateTime {
        return if (amountToSubtract == Long.MIN_VALUE) {
            plus(Long.MAX_VALUE, unit).plus(1, unit)
        } else {
            plus(-amountToSubtract, unit)
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of years subtracted.
     *
     *
     * This operates on the local time-line,
     * [subtracting years][LocalDateTime.minusYears] to the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, may be negative
     * @return a `ZonedDateTime` based on this date-time with the years subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusYears(years: Long): ZonedDateTime {
        return if (years == Long.MIN_VALUE) {
            plusYears(Long.MAX_VALUE).plusYears(1)
        } else {
            plusYears(-years)
        }
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of months subtracted.
     *
     *
     * This operates on the local time-line,
     * [subtracting months][LocalDateTime.minusMonths] to the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, may be negative
     * @return a `ZonedDateTime` based on this date-time with the months subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusMonths(months: Long): ZonedDateTime {
        return if (months == Long.MIN_VALUE) {
            plusMonths(Long.MAX_VALUE).plusMonths(1)
        } else {
            plusMonths(-months)
        }
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of weeks subtracted.
     *
     *
     * This operates on the local time-line,
     * [subtracting weeks][LocalDateTime.minusWeeks] to the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to subtract, may be negative
     * @return a `ZonedDateTime` based on this date-time with the weeks subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusWeeks(weeks: Long): ZonedDateTime {
        return if (weeks == Long.MIN_VALUE) {
            plusWeeks(Long.MAX_VALUE).plusWeeks(1)
        } else {
            plusWeeks(-weeks)
        }
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of days subtracted.
     *
     *
     * This operates on the local time-line,
     * [subtracting days][LocalDateTime.minusDays] to the local date-time.
     * This is then converted back to a `ZonedDateTime`, using the zone ID
     * to obtain the offset.
     *
     *
     * When converting back to `ZonedDateTime`, if the local date-time is in an overlap,
     * then the offset will be retained if possible, otherwise the earlier offset will be used.
     * If in a gap, the local date-time will be adjusted forward by the length of the gap.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, may be negative
     * @return a `ZonedDateTime` based on this date-time with the days subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusDays(days: Long): ZonedDateTime {
        return if (days == Long.MIN_VALUE) {
            plusDays(Long.MAX_VALUE).plusDays(1)
        } else {
            plusDays(-days)
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of hours subtracted.
     *
     *
     * This operates on the instant time-line, such that subtracting one hour will
     * always be a duration of one hour earlier.
     * This may cause the local date-time to change by an amount other than one hour.
     * Note that this is a different approach to that used by days, months and years,
     * thus subtracting one day is not the same as adding 24 hours.
     *
     *
     * For example, consider a time-zone, such as 'Europe/Paris', where the
     * Autumn DST cutover means that the local times 02:00 to 02:59 occur twice
     * changing from offset +02:00 in summer to +01:00 in winter.
     *
     *  * Subtracting one hour from 03:30+01:00 will result in 02:30+01:00
     * (both in winter time)
     *  * Subtracting one hour from 02:30+01:00 will result in 02:30+02:00
     * (moving from winter to summer time)
     *  * Subtracting one hour from 02:30+02:00 will result in 01:30+02:00
     * (both in summer time)
     *  * Subtracting three hours from 03:30+01:00 will result in 01:30+02:00
     * (moving from winter to summer time)
     *
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, may be negative
     * @return a `ZonedDateTime` based on this date-time with the hours subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusHours(hours: Long): ZonedDateTime {
        return if (hours == Long.MIN_VALUE) {
            plusHours(Long.MAX_VALUE).plusHours(1)
        } else {
            plusHours(-hours)
        }
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of minutes subtracted.
     *
     *
     * This operates on the instant time-line, such that subtracting one minute will
     * always be a duration of one minute earlier.
     * This may cause the local date-time to change by an amount other than one minute.
     * Note that this is a different approach to that used by days, months and years.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to subtract, may be negative
     * @return a `ZonedDateTime` based on this date-time with the minutes subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusMinutes(minutes: Long): ZonedDateTime {
        return if (minutes == Long.MIN_VALUE) {
            plusMinutes(Long.MAX_VALUE).plusMinutes(1)
        } else {
            plusMinutes(-minutes)
        }
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of seconds subtracted.
     *
     *
     * This operates on the instant time-line, such that subtracting one second will
     * always be a duration of one second earlier.
     * This may cause the local date-time to change by an amount other than one second.
     * Note that this is a different approach to that used by days, months and years.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to subtract, may be negative
     * @return a `ZonedDateTime` based on this date-time with the seconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusSeconds(seconds: Long): ZonedDateTime {
        return if (seconds == Long.MIN_VALUE) {
            plusSeconds(Long.MAX_VALUE).plusSeconds(1)
        } else {
            plusSeconds(-seconds)
        }
    }

    /**
     * Returns a copy of this `ZonedDateTime` with the specified number of nanoseconds subtracted.
     *
     *
     * This operates on the instant time-line, such that subtracting one nano will
     * always be a duration of one nano earlier.
     * This may cause the local date-time to change by an amount other than one nano.
     * Note that this is a different approach to that used by days, months and years.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to subtract, may be negative
     * @return a `ZonedDateTime` based on this date-time with the nanoseconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusNanos(nanos: Long): ZonedDateTime {
        return if (nanos == Long.MIN_VALUE) {
            plusNanos(Long.MAX_VALUE).plusNanos(1)
        } else {
            plusNanos(-nanos)
        }
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
        return if (query === TemporalQueries.LOCAL_DATE) {
            toLocalDate() as R
        } else {
            ChronoZonedDateTime.query(this, query)
        }
    }

    /**
     * Calculates the amount of time until another date-time in terms of the specified unit.
     *
     *
     * This calculates the amount of time between two `ZonedDateTime`
     * objects in terms of a single `TemporalUnit`.
     * The start and end points are `this` and the specified date-time.
     * The result will be negative if the end is before the start.
     * For example, the amount in days between two date-times can be calculated
     * using `startDateTime.until(endDateTime, DAYS)`.
     *
     *
     * The `Temporal` passed to this method is converted to a
     * `ZonedDateTime` using [.from].
     * If the time-zone differs between the two zoned date-times, the specified
     * end date-time is normalized to have the same zone as this date-time.
     *
     *
     * The calculation returns a whole number, representing the number of
     * complete units between the two date-times.
     * For example, the amount in months between 2012-06-15T00:00Z and 2012-08-14T23:59Z
     * will only be one month as it is one minute short of two months.
     *
     *
     * There are two equivalent ways of using this method.
     * The first is to invoke this method.
     * The second is to use [TemporalUnit.between]:
     * <pre>
     * // these two lines are equivalent
     * amount = start.until(end, MONTHS);
     * amount = MONTHS.between(start, end);
    </pre> *
     * The choice should be made based on which makes the code more readable.
     *
     *
     * The calculation is implemented in this method for [ChronoUnit].
     * The units `NANOS`, `MICROS`, `MILLIS`, `SECONDS`,
     * `MINUTES`, `HOURS` and `HALF_DAYS`, `DAYS`,
     * `WEEKS`, `MONTHS`, `YEARS`, `DECADES`,
     * `CENTURIES`, `MILLENNIA` and `ERAS` are supported.
     * Other `ChronoUnit` values will throw an exception.
     *
     *
     * The calculation for date and time units differ.
     *
     *
     * Date units operate on the local time-line, using the local date-time.
     * For example, the period from noon on day 1 to noon the following day
     * in days will always be counted as exactly one day, irrespective of whether
     * there was a daylight savings change or not.
     *
     *
     * Time units operate on the instant time-line.
     * The calculation effectively converts both zoned date-times to instants
     * and then calculates the period between the instants.
     * For example, the period from noon on day 1 to noon the following day
     * in hours may be 23, 24 or 25 hours (or some other amount) depending on
     * whether there was a daylight savings change or not.
     *
     *
     * If the unit is not a `ChronoUnit`, then the result of this method
     * is obtained by invoking `TemporalUnit.between(Temporal, Temporal)`
     * passing `this` as the first argument and the converted input temporal
     * as the second argument.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param endExclusive  the end date, exclusive, which is converted to a `ZonedDateTime`, not null
     * @param unit  the unit to measure the amount in, not null
     * @return the amount of time between this date-time and the end date-time
     * @throws DateTimeException if the amount cannot be calculated, or the end
     * temporal cannot be converted to a `ZonedDateTime`
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        var end = ZonedDateTime.from(endExclusive)
        if (unit is ChronoUnit) {
            end = end.withZoneSameInstant(zone)
            return if (unit.isDateBased()) {
                dateTime.until(end.dateTime, unit)
            } else {
                toOffsetDateTime().until(end.toOffsetDateTime(), unit)
            }
        }
        return unit.between(this, end)
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this date-time to an `OffsetDateTime`.
     *
     *
     * This creates an offset date-time using the local date-time and offset.
     * The zone ID is ignored.
     *
     * @return an offset date-time representing the same local date-time and offset, not null
     */
    fun toOffsetDateTime(): OffsetDateTime {
        return OffsetDateTime.of(dateTime, offset)
    }

    //-----------------------------------------------------------------------
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is ZonedDateTime) {
            return this.dateTime == other.dateTime &&
                this.offset == other.offset &&
                this.zone == other.zone
        }
        return false
    }

    override fun hashCode(): Int {
        return this.dateTime.hashCode().xor(this.offset.hashCode())
                .xor(Integers.rotateLeft(this.zone.hashCode(), 3))
    }

    /**
     * Outputs this date-time as a `String`, such as
     * `2007-12-03T10:15:30+01:00[Europe/Paris]`.
     *
     *
     * The format consists of the `LocalDateTime` followed by the `ZoneOffset`.
     * If the `ZoneId` is not the same as the offset, then the ID is output.
     * The output is compatible with ISO-8601 if the offset and ID are the same.
     *
     * @return a string representation of this date-time, not null
     */
    override fun toString(): String {
        val str = this.dateTime.toString() + this.offset.toString()
        return if (this.offset != this.zone) {
            str + '[' + zone.toString() + ']'
        } else {
            str
        }
    }

    override fun getChronology(): Chronology {
        return ChronoZonedDateTime.getChronology(this)
    }

    override fun isSupported(unit: TemporalUnit): Boolean {
        return ChronoZonedDateTime.isSupported(this, unit)
    }

    override fun toInstant(): Instant {
        return ChronoZonedDateTime.toInstant(this)
    }

    override fun toEpochSecond(): Long {
        return ChronoZonedDateTime.toEpochSecond(this)
    }

    override fun compareTo(other: ChronoZonedDateTime<*>): Int {
        return ChronoZonedDateTime.compare(this, other)
    }

    override fun isBefore(other: ChronoZonedDateTime<*>): Boolean {
        return ChronoZonedDateTime.isBefore(this, other)
    }

    override fun isAfter(other: ChronoZonedDateTime<*>): Boolean {
        return ChronoZonedDateTime.isAfter(this, other)
    }

    override fun isEqual(other: ChronoZonedDateTime<*>): Boolean {
        return ChronoZonedDateTime.isEqual(this, other)
    }

    /**
     * Resolves the new local date-time using this zone ID, retaining the offset if possible.
     *
     * @param newDateTime  the new local date-time, not null
     * @return the zoned date-time, not null
     */
    private fun resolveLocal(newDateTime: LocalDateTime): ZonedDateTime {
        return ofLocal(newDateTime, zone, offset)
    }

    /**
     * Resolves the new local date-time using the offset to identify the instant.
     *
     * @param newDateTime  the new local date-time, not null
     * @return the zoned date-time, not null
     */
    private fun resolveInstant(newDateTime: LocalDateTime): ZonedDateTime {
        return ofInstant(newDateTime, offset, zone)
    }

    /**
     * Resolves the offset into this zoned date-time for the with methods.
     * <p>
     * This typically ignores the offset, unless it can be used to switch offset in a DST overlap.
     *
     * @param offset  the offset, not null
     * @return the zoned date-time, not null
     */
    private fun resolveOffset(offset: ZoneOffset): ZonedDateTime {
        return if (offset != this.offset && this.zone.getRules()!!.isValidOffset(this.dateTime, offset)) {
            ZonedDateTime(this.dateTime, offset, this.zone)
        } else this
    }
}