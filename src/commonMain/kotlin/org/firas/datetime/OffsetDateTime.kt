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
package org.firas.datetime

import org.firas.datetime.chrono.IsoChronology
import org.firas.datetime.temporal.*
import org.firas.datetime.zone.ZoneId
import org.firas.datetime.zone.ZoneOffset
import org.firas.datetime.zone.getSystemZoneOffset
import org.firas.lang.getName
import kotlin.reflect.KClass

/**
 * A date-time with an offset from UTC/Greenwich in the ISO-8601 calendar system,
 * such as `2007-12-03T10:15:30+01:00`.
 * <p>
 * `OffsetDateTime` is an immutable representation of a date-time with an offset.
 * This class stores all date and time fields, to a precision of nanoseconds,
 * as well as the offset from UTC/Greenwich. For example, the value
 * "2nd October 2007 at 13:45:30.123456789 +02:00" can be stored in an `OffsetDateTime`.
 * <p>
 * `OffsetDateTime`, {@link java.time.ZonedDateTime} and {@link java.time.Instant} all store an instant
 * on the time-line to nanosecond precision.
 * `Instant` is the simplest, simply representing the instant.
 * `OffsetDateTime` adds to the instant the offset from UTC/Greenwich, which allows
 * the local date-time to be obtained.
 * `ZonedDateTime` adds full time-zone rules.
 * <p>
 * It is intended that `ZonedDateTime` or `Instant` is used to model data
 * in simpler applications. This class may be used when modeling date-time concepts in
 * more detail, or when communicating to a database or in a network protocol.
 *
 * <p>
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `OffsetDateTime` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class OffsetDateTime private constructor(
    val localDateTime: LocalDateTime,
    val offset: ZoneOffset
): Temporal, TemporalAdjuster, Comparable<OffsetDateTime> {

    companion object {
        /**
         * The minimum supported `OffsetDateTime`, '-999999999-01-01T00:00:00+18:00'.
         * This is the local date-time of midnight at the start of the minimum date
         * in the maximum offset (larger offsets are earlier on the time-line).
         * This combines [LocalDateTime.MIN] and [ZoneOffset.MAX].
         * This could be used by an application as a "far past" date-time.
         */
        val MIN = LocalDateTime.MIN.atOffset(ZoneOffset.MAX)
        /**
         * The maximum supported `OffsetDateTime`, '+999999999-12-31T23:59:59.999999999-18:00'.
         * This is the local date-time just before midnight at the end of the maximum date
         * in the minimum offset (larger negative offsets are later on the time-line).
         * This combines [LocalDateTime.MAX] and [ZoneOffset.MIN].
         * This could be used by an application as a "far future" date-time.
         */
        val MAX = LocalDateTime.MAX.atOffset(ZoneOffset.MIN)

        /**
         * Compares this `OffsetDateTime` to another date-time.
         * The comparison is based on the instant.
         *
         * @param datetime1  the first date-time to compare, not null
         * @param datetime2  the other date-time to compare to, not null
         * @return the comparator value, negative if less, positive if greater
         */
        val timeLineOrder = {
            datetime1: OffsetDateTime, datetime2: OffsetDateTime ->
            if (datetime1.offset == datetime2.offset) {
                datetime1.localDateTime.compareTo(datetime2.localDateTime)
            } else {
                val a = datetime1.toEpochSecond()
                val b = datetime2.toEpochSecond()
                if (a > b) 1 else if (a < b) -1 else
                        datetime1.localDateTime.getNano() - datetime2.localDateTime.getNano()
            }
        } as Comparator<OffsetDateTime>

        /**
         * Serialization version.
         */
        private const val serialVersionUID = 2287754244819255394L

        fun now(): OffsetDateTime {
            return OffsetDateTime.of(LocalDateTime.now(), getSystemZoneOffset())
        }

        //-----------------------------------------------------------------------
        /**
         * Obtains an instance of `OffsetDateTime` from a date, time and offset.
         *
         *
         * This creates an offset date-time with the specified local date, time and offset.
         *
         * @param date  the local date, not null
         * @param time  the local time, not null
         * @param offset  the zone offset, not null
         * @return the offset date-time, not null
         */
        fun of(date: LocalDate, time: LocalTime, offset: ZoneOffset): OffsetDateTime {
            val dt = LocalDateTime.of(date, time)
            return OffsetDateTime(dt, offset)
        }

        /**
         * Obtains an instance of `OffsetDateTime` from a date-time and offset.
         *
         *
         * This creates an offset date-time with the specified local date-time and offset.
         *
         * @param dateTime  the local date-time, not null
         * @param offset  the zone offset, not null
         * @return the offset date-time, not null
         */
        fun of(dateTime: LocalDateTime, offset: ZoneOffset): OffsetDateTime {
            return OffsetDateTime(dateTime, offset)
        }

        /**
         * Obtains an instance of `OffsetDateTime` from a year, month, day,
         * hour, minute, second, nanosecond and offset.
         *
         *
         * This creates an offset date-time with the seven specified fields.
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
         * @param offset  the zone offset, not null
         * @return the offset date-time, not null
         * @throws DateTimeException if the value of any field is out of range, or
         * if the day-of-month is invalid for the month-year
         */
        fun of(
            year: Int, month: Int, dayOfMonth: Int,
            hour: Int, minute: Int, second: Int, nanoOfSecond: Int, offset: ZoneOffset
        ): OffsetDateTime {
            val dt = LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanoOfSecond)
            return OffsetDateTime(dt, offset)
        }

        /**
         * Obtains an instance of `OffsetDateTime` from an `Instant` and zone ID.
         *
         *
         * This creates an offset date-time with the same instant as that specified.
         * Finding the offset from UTC/Greenwich is simple as there is only one valid
         * offset for each instant.
         *
         * @param instant  the instant to create the date-time from, not null
         * @param zone  the time-zone, which may be an offset, not null
         * @return the offset date-time, not null
         * @throws DateTimeException if the result exceeds the supported range
         */
        fun ofInstant(instant: Instant, zone: ZoneId): OffsetDateTime {
            val rules = zone.getRules()
            val offset = rules!!.getOffset(instant)!!
            val ldt = LocalDateTime.ofEpochSecond(instant.epochSecond, instant.nanos, offset)
            return OffsetDateTime(ldt, offset)
        }

        /**
         * Obtains an instance of `OffsetDateTime` from a temporal object.
         * <p>
         * This obtains an offset date-time based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `OffsetDateTime`.
         * <p>
         * The conversion will first obtain a `ZoneOffset` from the temporal object.
         * It will then try to obtain a `LocalDateTime`, falling back to an `Instant` if necessary.
         * The result will be the combination of `ZoneOffset` with either
         * with `LocalDateTime` or `Instant`.
         * Implementations are permitted to perform optimizations such as accessing
         * those fields that are equivalent to the relevant objects.
         * <p>
         * This method matches the signature of the functional interface {@link TemporalQuery}
         * allowing it to be used as a query via method reference, `OffsetDateTime::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the offset date-time, not null
         * @throws DateTimeException if unable to convert to an `OffsetDateTime`
         */
        fun from(temporal: TemporalAccessor): OffsetDateTime {
            if (temporal is OffsetDateTime) {
                return temporal
            }
            try {
                val offset = ZoneOffset.from(temporal)
                val date = temporal.query(TemporalQueries.LOCAL_DATE)
                val time = temporal.query(TemporalQueries.LOCAL_TIME)
                return if (date != null && time != null) {
                    OffsetDateTime.of(date, time, offset)
                } else {
                    val instant = Instant.from(temporal)
                    OffsetDateTime.ofInstant(instant, offset)
                }
            } catch (ex: DateTimeException) {
                throw DateTimeException("Unable to obtain OffsetDateTime from TemporalAccessor: " +
                        temporal + " of type " + temporal::class.getName(), ex)
            }
        }

        /**
         * Compares this `OffsetDateTime` to another date-time.
         * The comparison is based on the instant.
         *
         * @param datetime1  the first date-time to compare, not null
         * @param datetime2  the other date-time to compare to, not null
         * @return the comparator value, negative if less, positive if greater
         */
        fun compareInstant(datetime1: OffsetDateTime, datetime2: OffsetDateTime): Int {
            if (datetime1.offset.equals(datetime2.offset)) {
                return datetime1.localDateTime.compareTo(datetime2.localDateTime)
            }
            val a = datetime1.toEpochSecond()
            val b = datetime2.toEpochSecond()
            return if (a > b) 1 else if (a < b) -1 else
                datetime1.toLocalTime().nano - datetime2.toLocalTime().nano
        }
    } // companion object

    /**
     * Converts this date-time to an `Instant`.
     *
     *
     * This returns an `Instant` representing the same point on the
     * time-line as this date-time.
     *
     * @return an `Instant` representing the same instant, not null
     */
    fun toInstant(): Instant {
        return localDateTime.toInstant(offset)
    }

    /**
     * Converts this date-time to the number of seconds from the epoch of 1970-01-01T00:00:00Z.
     *
     *
     * This allows this date-time to be converted to a value of the
     * [epoch-seconds][ChronoField.INSTANT_SECONDS] field. This is primarily
     * intended for low-level conversions rather than general application usage.
     *
     * @return the number of seconds from the epoch of 1970-01-01T00:00:00Z
     */
    fun toEpochSecond(): Long {
        return localDateTime.toEpochSecond(offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified offset ensuring
     * that the result is at the same instant.
     *
     *
     * This method returns an object with the specified `ZoneOffset` and a `LocalDateTime`
     * adjusted by the difference between the two offsets.
     * This will result in the old and new objects representing the same instant.
     * This is useful for finding the local time in a different offset.
     * For example, if this time represents `2007-12-03T10:30+02:00` and the offset specified is
     * `+03:00`, then this method will return `2007-12-03T11:30+03:00`.
     *
     *
     * To change the offset without adjusting the local time use [.withOffsetSameLocal].
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param offset  the zone offset to change to, not null
     * @return an `OffsetDateTime` based on this date-time with the requested offset, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun withOffsetSameInstant(offset: ZoneOffset): OffsetDateTime {
        if (offset == this.offset) {
            return this
        }
        val difference = offset.totalSeconds - this.offset.totalSeconds
        val adjusted = localDateTime.plusSeconds(difference.toLong())
        return OffsetDateTime(adjusted, offset)
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
    fun toLocalDate(): LocalDate {
        return localDateTime.getDate()
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
        return localDateTime.getYear()
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
        return localDateTime.getMonthValue()
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
        return localDateTime.getMonth()
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
        return localDateTime.getDayOfMonth()
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
        return localDateTime.getDayOfYear()
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
        return localDateTime.getDayOfWeek()
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
    fun toLocalTime(): LocalTime {
        return localDateTime.getTime()
    }

    /**
     * Gets the hour-of-day field.
     *
     * @return the hour-of-day, from 0 to 23
     */
    fun getHour(): Int {
        return localDateTime.getHour()
    }

    /**
     * Gets the minute-of-hour field.
     *
     * @return the minute-of-hour, from 0 to 59
     */
    fun getMinute(): Int {
        return localDateTime.getMinute()
    }

    /**
     * Gets the second-of-minute field.
     *
     * @return the second-of-minute, from 0 to 59
     */
    fun getSecond(): Int {
        return localDateTime.getSecond()
    }

    /**
     * Gets the nano-of-second field.
     *
     * @return the nano-of-second, from 0 to 999,999,999
     */
    fun getNano(): Int {
        return localDateTime.getNano()
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `OffsetDateTime` with the year altered.
     *
     *
     * The time and offset do not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param year  the year to set in the result, from MIN_YEAR to MAX_YEAR
     * @return an `OffsetDateTime` based on this date-time with the requested year, not null
     * @throws DateTimeException if the year value is invalid
     */
    fun withYear(year: Int): OffsetDateTime {
        return with(localDateTime.withYear(year), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the month-of-year altered.
     *
     *
     * The time and offset do not affect the calculation and will be the same in the result.
     * If the day-of-month is invalid for the year, it will be changed to the last valid day of the month.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param month  the month-of-year to set in the result, from 1 (January) to 12 (December)
     * @return an `OffsetDateTime` based on this date-time with the requested month, not null
     * @throws DateTimeException if the month-of-year value is invalid
     */
    fun withMonth(month: Int): OffsetDateTime {
        return with(localDateTime.withMonth(month), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the day-of-month altered.
     *
     *
     * If the resulting `OffsetDateTime` is invalid, an exception is thrown.
     * The time and offset do not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfMonth  the day-of-month to set in the result, from 1 to 28-31
     * @return an `OffsetDateTime` based on this date-time with the requested day, not null
     * @throws DateTimeException if the day-of-month value is invalid,
     * or if the day-of-month is invalid for the month-year
     */
    fun withDayOfMonth(dayOfMonth: Int): OffsetDateTime {
        return with(localDateTime.withDayOfMonth(dayOfMonth), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the day-of-year altered.
     *
     *
     * The time and offset do not affect the calculation and will be the same in the result.
     * If the resulting `OffsetDateTime` is invalid, an exception is thrown.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param dayOfYear  the day-of-year to set in the result, from 1 to 365-366
     * @return an `OffsetDateTime` based on this date with the requested day, not null
     * @throws DateTimeException if the day-of-year value is invalid,
     * or if the day-of-year is invalid for the year
     */
    fun withDayOfYear(dayOfYear: Int): OffsetDateTime {
        return with(localDateTime.withDayOfYear(dayOfYear), offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `OffsetDateTime` with the hour-of-day altered.
     *
     *
     * The date and offset do not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hour  the hour-of-day to set in the result, from 0 to 23
     * @return an `OffsetDateTime` based on this date-time with the requested hour, not null
     * @throws DateTimeException if the hour value is invalid
     */
    fun withHour(hour: Int): OffsetDateTime {
        return with(localDateTime.withHour(hour), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the minute-of-hour altered.
     *
     *
     * The date and offset do not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minute  the minute-of-hour to set in the result, from 0 to 59
     * @return an `OffsetDateTime` based on this date-time with the requested minute, not null
     * @throws DateTimeException if the minute value is invalid
     */
    fun withMinute(minute: Int): OffsetDateTime {
        return with(localDateTime.withMinute(minute), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the second-of-minute altered.
     *
     *
     * The date and offset do not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param second  the second-of-minute to set in the result, from 0 to 59
     * @return an `OffsetDateTime` based on this date-time with the requested second, not null
     * @throws DateTimeException if the second value is invalid
     */
    fun withSecond(second: Int): OffsetDateTime {
        return with(localDateTime.withSecond(second), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the nano-of-second altered.
     *
     *
     * The date and offset do not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanoOfSecond  the nano-of-second to set in the result, from 0 to 999,999,999
     * @return an `OffsetDateTime` based on this date-time with the requested nanosecond, not null
     * @throws DateTimeException if the nano value is invalid
     */
    fun withNano(nanoOfSecond: Int): OffsetDateTime {
        return with(localDateTime.withNano(nanoOfSecond), offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `OffsetDateTime` with the time truncated.
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
     * The offset does not affect the calculation and will be the same in the result.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param unit  the unit to truncate to, not null
     * @return an `OffsetDateTime` based on this date-time with the time truncated, not null
     * @throws DateTimeException if unable to truncate
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     */
    fun truncatedTo(unit: TemporalUnit): OffsetDateTime {
        return with(this.localDateTime.truncatedTo(unit), this.offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified amount added.
     *
     *
     * This returns an `OffsetDateTime`, based on this one, with the specified amount added.
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
     * @return an `OffsetDateTime` based on this date-time with the addition made, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: TemporalAmount): OffsetDateTime {
        return amountToAdd.addTo(this) as OffsetDateTime
    }

    /**
     * Returns a copy of this date-time with the specified amount added.
     *
     *
     * This returns an `OffsetDateTime`, based on this one, with the amount
     * in terms of the unit added. If it is not possible to add the amount, because the
     * unit is not supported or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoUnit] then the addition is implemented by
     * [LocalDateTime.plus].
     * The offset is not part of the calculation and will be unchanged in the result.
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
     * @return an `OffsetDateTime` based on this date-time with the specified amount added, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: Long, unit: TemporalUnit): OffsetDateTime {
        return if (unit is ChronoUnit) {
            with(this.localDateTime.plus(amountToAdd, unit), this.offset)
        } else {
            unit.addTo(this, amountToAdd)
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of years added.
     *
     *
     * This method adds the specified amount to the years field in three steps:
     *
     *  1. Add the input years to the year field
     *  1. Check if the resulting date would be invalid
     *  1. Adjust the day-of-month to the last valid day if necessary
     *
     *
     *
     * For example, 2008-02-29 (leap year) plus one year would result in the
     * invalid date 2009-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2009-02-28, is selected instead.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the years added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusYears(years: Long): OffsetDateTime {
        return with(localDateTime.plusYears(years), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of months added.
     *
     *
     * This method adds the specified amount to the months field in three steps:
     *
     *  1. Add the input months to the month-of-year field
     *  1. Check if the resulting date would be invalid
     *  1. Adjust the day-of-month to the last valid day if necessary
     *
     *
     *
     * For example, 2007-03-31 plus one month would result in the invalid date
     * 2007-04-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-04-30, is selected instead.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the months added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusMonths(months: Long): OffsetDateTime {
        return with(localDateTime.plusMonths(months), offset)
    }

    /**
     * Returns a copy of this OffsetDateTime with the specified number of weeks added.
     *
     *
     * This method adds the specified amount in weeks to the days field incrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     *
     *
     * For example, 2008-12-31 plus one week would result in 2009-01-07.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the weeks added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusWeeks(weeks: Long): OffsetDateTime {
        return with(localDateTime.plusWeeks(weeks), offset)
    }

    /**
     * Returns a copy of this OffsetDateTime with the specified number of days added.
     *
     *
     * This method adds the specified amount to the days field incrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     *
     *
     * For example, 2008-12-31 plus one day would result in 2009-01-01.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the days added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusDays(days: Long): OffsetDateTime {
        return with(localDateTime.plusDays(days), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of hours added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the hours added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusHours(hours: Long): OffsetDateTime {
        return with(localDateTime.plusHours(hours), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of minutes added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the minutes added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusMinutes(minutes: Long): OffsetDateTime {
        return with(localDateTime.plusMinutes(minutes), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of seconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the seconds added, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun plusSeconds(seconds: Long): OffsetDateTime {
        return with(localDateTime.plusSeconds(seconds), offset)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of nanoseconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to add, may be negative
     * @return an `OffsetDateTime` based on this date-time with the nanoseconds added, not null
     * @throws DateTimeException if the unit cannot be added to this type
     */
    fun plusNanos(nanos: Long): OffsetDateTime {
        return with(localDateTime.plusNanos(nanos), offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of years subtracted.
     *
     *
     * This method subtracts the specified amount from the years field in three steps:
     *
     *  1. Subtract the input years from the year field
     *  1. Check if the resulting date would be invalid
     *  1. Adjust the day-of-month to the last valid day if necessary
     *
     *
     *
     * For example, 2008-02-29 (leap year) minus one year would result in the
     * invalid date 2007-02-29 (standard year). Instead of returning an invalid
     * result, the last valid day of the month, 2007-02-28, is selected instead.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param years  the years to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the years subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusYears(years: Long): OffsetDateTime {
        return if (years == Long.MIN_VALUE) plusYears(Long.MAX_VALUE).plusYears(1)
                else plusYears(-years)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of months subtracted.
     *
     *
     * This method subtracts the specified amount from the months field in three steps:
     *
     *  1. Subtract the input months from the month-of-year field
     *  1. Check if the resulting date would be invalid
     *  1. Adjust the day-of-month to the last valid day if necessary
     *
     *
     *
     * For example, 2007-03-31 minus one month would result in the invalid date
     * 2007-02-31. Instead of returning an invalid result, the last valid day
     * of the month, 2007-02-28, is selected instead.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param months  the months to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the months subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusMonths(months: Long): OffsetDateTime {
        return if (months == Long.MIN_VALUE) plusMonths(Long.MAX_VALUE).plusMonths(1)
                else plusMonths(-months)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of weeks subtracted.
     *
     *
     * This method subtracts the specified amount in weeks from the days field decrementing
     * the month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     *
     *
     * For example, 2009-01-07 minus one week would result in 2008-12-31.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param weeks  the weeks to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the weeks subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusWeeks(weeks: Long): OffsetDateTime {
        return if (weeks == Long.MIN_VALUE) plusWeeks(Long.MAX_VALUE).plusWeeks(1)
                else plusWeeks(-weeks)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of days subtracted.
     *
     *
     * This method subtracts the specified amount from the days field decrementing the
     * month and year fields as necessary to ensure the result remains valid.
     * The result is only invalid if the maximum/minimum year is exceeded.
     *
     *
     * For example, 2009-01-01 minus one day would result in 2008-12-31.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param days  the days to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the days subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusDays(days: Long): OffsetDateTime {
        return if (days == Long.MIN_VALUE) plusDays(Long.MAX_VALUE).plusDays(1)
                else plusDays(-days)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of hours subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param hours  the hours to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the hours subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusHours(hours: Long): OffsetDateTime {
        return if (hours == Long.MIN_VALUE) plusHours(Long.MAX_VALUE).plusHours(1)
                else plusHours(-hours)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of minutes subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param minutes  the minutes to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the minutes subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusMinutes(minutes: Long): OffsetDateTime {
        return if (minutes == Long.MIN_VALUE) plusMinutes(Long.MAX_VALUE).plusMinutes(1)
                else plusMinutes(-minutes)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of seconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param seconds  the seconds to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the seconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusSeconds(seconds: Long): OffsetDateTime {
        return if (seconds == Long.MIN_VALUE) plusSeconds(Long.MAX_VALUE).plusSeconds(1)
                else plusSeconds(-seconds)
    }

    /**
     * Returns a copy of this `OffsetDateTime` with the specified number of nanoseconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanos  the nanos to subtract, may be negative
     * @return an `OffsetDateTime` based on this date-time with the nanoseconds subtracted, not null
     * @throws DateTimeException if the result exceeds the supported date range
     */
    fun minusNanos(nanos: Long): OffsetDateTime {
        return if (nanos == Long.MIN_VALUE) plusNanos(Long.MAX_VALUE).plusNanos(1)
                else plusNanos(-nanos)
    }

    // ----==== Comparison ====----
    /**
     * Compares this date-time to another date-time.
     *
     * The comparison is based on the instant then on the local date-time.
     * It is "consistent with equals", as defined by {@link Comparable}.
     *
     * For example, the following is the comparator order:
     *
     * * `2008-12-03T10:30+01:00}`
     * * `2008-12-03T11:00+01:00}`
     * * `2008-12-03T12:00+02:00}`
     * * `2008-12-03T11:30+01:00}`
     * * `2008-12-03T12:00+01:00}`
     * * `2008-12-03T12:30+01:00}`
     *
     *
     * Values #2 and #3 represent the same instant on the time-line.
     * When two values represent the same instant, the local date-time is compared
     * to distinguish them. This step is needed to make the ordering
     * consistent with `.equals()`.
     *
     * @param other  the other date-time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    override fun compareTo(other: OffsetDateTime): Int {
        val cmp = compareInstant(this, other)
        return if (cmp == 0) {
            this.localDateTime.compareTo(other.localDateTime)
        } else cmp
    }

    /**
     * Checks if the instant of this date-time is after that of the specified date-time.
     *
     *
     * This method differs from the comparison in [.compareTo] and [.equals] in that it
     * only compares the instant of the date-time. This is equivalent to using
     * `dateTime1.toInstant().isAfter(dateTime2.toInstant());`.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this is after the instant of the specified date-time
     */
    fun isAfter(other: OffsetDateTime): Boolean {
        val thisEpochSec = this.toEpochSecond()
        val otherEpochSec = other.toEpochSecond()
        return thisEpochSec > otherEpochSec ||
                thisEpochSec == otherEpochSec && this.toLocalTime().nano > other.toLocalTime().nano
    }

    /**
     * Checks if the instant of this date-time is before that of the specified date-time.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the instant of the date-time. This is equivalent to using
     * `dateTime1.toInstant().isBefore(dateTime2.toInstant());`.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this is before the instant of the specified date-time
     */
    fun isBefore(other: OffsetDateTime): Boolean {
        val thisEpochSec = this.toEpochSecond()
        val otherEpochSec = other.toEpochSecond()
        return thisEpochSec < otherEpochSec ||
                thisEpochSec == otherEpochSec && this.toLocalTime().nano < other.toLocalTime().nano
    }

    /**
     * Checks if the instant of this date-time is equal to that of the specified date-time.
     *
     *
     * This method differs from the comparison in [.compareTo] and [.equals]
     * in that it only compares the instant of the date-time. This is equivalent to using
     * `dateTime1.toInstant().equals(dateTime2.toInstant());`.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if the instant equals the instant of the specified date-time
     */
    fun isEqual(other: OffsetDateTime): Boolean {
        return this.toEpochSecond() == other.toEpochSecond() &&
                this.toLocalTime().nano == other.toLocalTime().nano
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

    /**
     * Checks if the specified unit is supported.
     *
     *
     * This checks if the specified unit can be added to, or subtracted from, this date-time.
     * If false, then calling the [.plus] and
     * [minus][.minus] methods will throw an exception.
     *
     *
     * If the unit is a [ChronoUnit] then the query is implemented here.
     * The supported units are:
     *
     *  * `NANOS`
     *  * `MICROS`
     *  * `MILLIS`
     *  * `SECONDS`
     *  * `MINUTES`
     *  * `HOURS`
     *  * `HALF_DAYS`
     *  * `DAYS`
     *  * `WEEKS`
     *  * `MONTHS`
     *  * `YEARS`
     *  * `DECADES`
     *  * `CENTURIES`
     *  * `MILLENNIA`
     *  * `ERAS`
     *
     * All other `ChronoUnit` instances will return false.
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
        } else {
            unit.isSupportedBy(this)
        }
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
            } else {
                this.localDateTime.range(field)
            }
        } else field.rangeRefinedBy(this)
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
            return when (field) {
                ChronoField.INSTANT_SECONDS ->
                    throw UnsupportedTemporalTypeException("Invalid field 'InstantSeconds' for get() method, use getLong() instead")
                ChronoField.OFFSET_SECONDS ->
                    return this.offset.totalSeconds
                else -> this.localDateTime.get(field)
            }
        }
        TODO("return Temporal.super.get(field)")
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
                ChronoField.OFFSET_SECONDS -> offset.totalSeconds.toLong()
                else -> this.localDateTime.getLong(field)
            }
        }
        return field.getFrom(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this date-time with the specified field set to a new value.
     *
     *
     * This returns an `OffsetDateTime`, based on this one, with the value
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
     * The offset and nano-of-second are unchanged.
     * If the new instant value is outside the valid range then a `DateTimeException` will be thrown.
     *
     *
     * The `OFFSET_SECONDS` field will return a date-time with the specified offset.
     * The local date-time is unaltered. If the new offset value is outside the valid range
     * then a `DateTimeException` will be thrown.
     *
     *
     * The other [supported fields][.isSupported] will behave as per
     * the matching method on [LocalDateTime][LocalDateTime.with].
     * In this case, the offset is not part of the calculation and will be unchanged.
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
     * @return an `OffsetDateTime` based on `this` with the specified field set, not null
     * @throws DateTimeException if the field cannot be set
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(field: TemporalField, newValue: Long): OffsetDateTime {
        if (field is ChronoField) {
            return when (field) {
                ChronoField.INSTANT_SECONDS ->
                    ofInstant(Instant.ofEpochSecond(newValue, getNano().toLong()), offset)
                ChronoField.OFFSET_SECONDS ->
                    with(localDateTime, ZoneOffset.ofTotalSeconds(field.checkValidIntValue(newValue)))
                else ->
                    with(localDateTime.with(field, newValue), offset)
            }
        }
        return field.adjustInto(this, newValue)
    }

    /**
     * Returns an adjusted copy of this date-time.
     *
     *
     * This returns an `OffsetDateTime`, based on this one, with the date-time adjusted.
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
     * result = offsetDateTime.with(JULY).with(lastDayOfMonth());
     * </pre>
     *
     *
     * The classes [LocalDate], [LocalTime] and [ZoneOffset] implement
     * `TemporalAdjuster`, thus this method can be used to change the date, time or offset:
     * <pre>
     * result = offsetDateTime.with(date);
     * result = offsetDateTime.with(time);
     * result = offsetDateTime.with(offset);
     * </pre>
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
     * @return an `OffsetDateTime` based on `this` with the adjustment made, not null
     * @throws DateTimeException if the adjustment cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(adjuster: TemporalAdjuster): OffsetDateTime {
        // optimizations
        return if (adjuster is LocalDate || adjuster is LocalTime || adjuster is LocalDateTime) {
            with(this.localDateTime.with(adjuster), offset)
        } else if (adjuster is Instant) {
            ofInstant(adjuster, offset)
        } else if (adjuster is ZoneOffset) {
            with(this.localDateTime, adjuster)
        } else if (adjuster is OffsetDateTime) {
            adjuster
        } else {
            adjuster.adjustInto(this) as OffsetDateTime
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
     * [TemporalQuery#queryFrom(TemporalAccessor)] method on the
     * specified query passing `this` as the argument.
     *
     * @param <R> the type of the result
     * @param query  the query to invoke, not null
     * @return the query result, null may be returned (defined by the query)
     * @throws DateTimeException if unable to query (defined by the query)
     * @throws ArithmeticException if numeric overflow occurs (defined by the query)
     */
    override fun <R> query(query: TemporalQuery<R>): R? {
        if (query == TemporalQueries.OFFSET || query == TemporalQueries.ZONE) {
            return offset as R
        } else if (query == TemporalQueries.ZONE_ID) {
            return null
        } else if (query == TemporalQueries.LOCAL_DATE) {
            return toLocalDate() as R
        } else if (query == TemporalQueries.LOCAL_TIME) {
            return toLocalTime() as R
        } else if (query == TemporalQueries.CHRONO) {
            return IsoChronology.INSTANCE as R
        } else if (query == TemporalQueries.PRECISION) {
            return ChronoUnit.NANOS as R
        }
        // inline TemporalAccessor.super.query(query) as an optimization
        // non-JDK classes are not permitted to make this optimization
        return query.queryFrom(this)
    }

    /**
     * Adjusts the specified temporal object to have the same offset, date
     * and time as this object.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with the offset, date and time changed to be the same as this.
     *
     *
     * The adjustment is equivalent to using [Temporal#with(TemporalField, long)]
     * three times, passing [ChronoField#EPOCH_DAY],
     * [ChronoField#NANO_OF_DAY] and [ChronoField#OFFSET_SECONDS] as the fields.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * {@link Temporal#with(TemporalAdjuster)}:
     * <pre>
     *   // these two lines are equivalent, but the second approach is recommended
     *   temporal = thisOffsetDateTime.adjustInto(temporal);
     *   temporal = temporal.with(thisOffsetDateTime);
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
        // OffsetDateTime is treated as three separate fields, not an instant
        // this produces the most consistent set of results overall
        // the offset is set after the date and time, as it is typically a small
        // tweak to the result, with ZonedDateTime frequently ignoring the offset
        return temporal
                .with(ChronoField.EPOCH_DAY, toLocalDate().toEpochDay())
                .with(ChronoField.NANO_OF_DAY, toLocalTime().toNanoOfDay())
                .with(ChronoField.OFFSET_SECONDS, this.offset.totalSeconds.toLong())
    }

    /**
     * Calculates the amount of time until another date-time in terms of the specified unit.
     *
     *
     * This calculates the amount of time between two `OffsetDateTime`
     * objects in terms of a single `TemporalUnit`.
     * The start and end points are `this` and the specified date-time.
     * The result will be negative if the end is before the start.
     * For example, the amount in days between two date-times can be calculated
     * using `startDateTime.until(endDateTime, DAYS)`.
     *
     *
     * The `Temporal` passed to this method is converted to a
     * `OffsetDateTime` using [.from].
     * If the offset differs between the two date-times, the specified
     * end date-time is normalized to have the same offset as this date-time.
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
     * </pre>
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
     * If the unit is not a `ChronoUnit`, then the result of this method
     * is obtained by invoking `TemporalUnit.between(Temporal, Temporal)`
     * passing `this` as the first argument and the converted input temporal
     * as the second argument.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param endExclusive  the end date, exclusive, which is converted to an `OffsetDateTime`, not null
     * @param unit  the unit to measure the amount in, not null
     * @return the amount of time between this date-time and the end date-time
     * @throws DateTimeException if the amount cannot be calculated, or the end
     * temporal cannot be converted to an `OffsetDateTime`
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        var end = OffsetDateTime.from(endExclusive)
        if (unit is ChronoUnit) {
            end = end.withOffsetSameInstant(this.offset)
            return this.localDateTime.until(end.localDateTime, unit)
        }
        return unit.between(this, end)
    }

    //-----------------------------------------------------------------------
    /**
     * Combines this date-time with a time-zone to create a `ZonedDateTime`
     * ensuring that the result has the same instant.
     *
     *
     * This returns a `ZonedDateTime` formed from this date-time and the specified time-zone.
     * This conversion will ignore the visible local date-time and use the underlying instant instead.
     * This avoids any problems with local time-line gaps or overlaps.
     * The result might have different values for fields such as hour, minute an even day.
     *
     *
     * To attempt to retain the values of the fields, use [.atZoneSimilarLocal].
     * To use the offset as the zone ID, use [.toZonedDateTime].
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date-time, not null
     */
    fun atZoneSameInstant(zone: ZoneId): ZonedDateTime {
        return ZonedDateTime.ofInstant(this.localDateTime, this.offset, zone)
    }

    /**
     * Combines this date-time with a time-zone to create a `ZonedDateTime`
     * trying to keep the same local date and time.
     *
     *
     * This returns a `ZonedDateTime` formed from this date-time and the specified time-zone.
     * Where possible, the result will have the same local date-time as this object.
     *
     *
     * Time-zone rules, such as daylight savings, mean that not every time on the
     * local time-line exists. If the local date-time is in a gap or overlap according to
     * the rules then a resolver is used to determine the resultant local time and offset.
     * This method uses [ZonedDateTime.ofLocal]
     * to retain the offset from this instance if possible.
     *
     *
     * Finer control over gaps and overlaps is available in two ways.
     * If you simply want to use the later offset at overlaps then call
     * [ZonedDateTime.withLaterOffsetAtOverlap] immediately after this method.
     *
     *
     * To create a zoned date-time at the same instant irrespective of the local time-line,
     * use [.atZoneSameInstant].
     * To use the offset as the zone ID, use [.toZonedDateTime].
     *
     * @param zone  the time-zone to use, not null
     * @return the zoned date-time formed from this date and the earliest valid time for the zone, not null
     */
    fun atZoneSimilarLocal(zone: ZoneId): ZonedDateTime {
        return ZonedDateTime.ofLocal(this.localDateTime, zone, this.offset)
    }

    //-----------------------------------------------------------------------
    /**
     * Converts this date-time to an `OffsetTime`.
     *
     *
     * This returns an offset time with the same local time and offset.
     *
     * @return an OffsetTime representing the time and offset, not null
     */
    fun toOffsetTime(): OffsetTime {
        return OffsetTime.of(this.localDateTime.getTime(), offset)
    }

    //-----------------------------------------------------------------------
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is OffsetDateTime) {
            return this.localDateTime == other.localDateTime && this.offset == other.offset
        }
        return false
    }

    override fun hashCode(): Int {
        return this.localDateTime.hashCode() xor this.offset.hashCode()
    }

    override fun toString(): String {
        return this.localDateTime.toString() + offset.toString()
    }

    /**
     * Returns a new date-time based on this one, returning `this` where possible.
     *
     * @param dateTime  the date-time to create with, not null
     * @param offset  the zone offset to create with, not null
     */
    private fun with(dateTime: LocalDateTime, offset: ZoneOffset): OffsetDateTime {
        return if (this.localDateTime == dateTime && this.offset == offset) this
                else OffsetDateTime(dateTime, offset)
    }
}