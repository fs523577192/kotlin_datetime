/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2012, Stephen Colebourne & Michael Nascimento Santos
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

import org.firas.datetime.*
import org.firas.datetime.temporal.ChronoField
import org.firas.datetime.temporal.TemporalAccessor
import org.firas.datetime.util.MathUtils
import org.firas.datetime.zone.ZoneId
import org.firas.datetime.zone.ZoneOffset


/**
 * The ISO calendar system.
 * <p>
 * This chronology defines the rules of the ISO calendar system.
 * This calendar system is based on the ISO-8601 standard, which is the
 * <i>de facto</i> world calendar.
 * <p>
 * The fields are defined as follows:
 * <ul>
 * <li>era - There are two eras, 'Current Era' (CE) and 'Before Current Era' (BCE).
 * <li>year-of-era - The year-of-era is the same as the proleptic-year for the current CE era.
 *  For the BCE era before the ISO epoch the year increases from 1 upwards as time goes backwards.
 * <li>proleptic-year - The proleptic year is the same as the year-of-era for the
 *  current era. For the previous era, years have zero, then negative values.
 * <li>month-of-year - There are 12 months in an ISO year, numbered from 1 to 12.
 * <li>day-of-month - There are between 28 and 31 days in each of the ISO month, numbered from 1 to 31.
 *  Months 4, 6, 9 and 11 have 30 days, Months 1, 3, 5, 7, 8, 10 and 12 have 31 days.
 *  Month 2 has 28 days, or 29 in a leap year.
 * <li>day-of-year - There are 365 days in a standard ISO year and 366 in a leap year.
 *  The days are numbered from 1 to 365 or 1 to 366.
 * <li>leap-year - Leap years occur every 4 years, except where the year is divisble by 100 and not divisble by 400.
 * </ul>
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class IsoChronology private constructor(): Chronology {

    companion object {
        /**
         * Singleton instance of the ISO chronology.
         */
        val INSTANCE = IsoChronology()

        /**
         * Serialization version.
         */
        private const val serialVersionUID = -1440403870442975015L

        private const val DAYS_0000_TO_1970 = 146097 * 5L - (30L * 365L + 7L) // taken from LocalDate
    }

    /**
     * Gets the ID of the chronology - 'ISO'.
     *
     *
     * The ID uniquely identifies the `Chronology`.
     * It can be used to lookup the `Chronology` using [Chronology.of].
     *
     * @return the chronology ID - 'ISO'
     * @see .getCalendarType
     */
    override fun getId(): String {
        return "ISO"
    }

    /**
     * Gets the calendar type of the underlying calendar system - 'iso8601'.
     *
     *
     * The calendar type is an identifier defined by the
     * *Unicode Locale Data Markup Language (LDML)* specification.
     * It can be used to lookup the `Chronology` using [Chronology.of].
     * It can also be used as part of a locale, accessible via
     * [Locale.getUnicodeLocaleType] with the key 'ca'.
     *
     * @return the calendar system type - 'iso8601'
     * @see .getId
     */
    override fun getCalendarType(): String {
        return "iso8601"
    }

    /**
     * Obtains an ISO local date from the proleptic-year, month-of-year
     * and day-of-month fields.
     *
     *
     * This is equivalent to [LocalDate.of].
     *
     * @param prolepticYear  the ISO proleptic-year
     * @param month  the ISO month-of-year
     * @param dayOfMonth  the ISO day-of-month
     * @return the ISO local date, not null
     * @throws DateTimeException if unable to create the date
     */
    override fun date(prolepticYear: Int, month: Int, dayOfMonth: Int): LocalDate {
        return LocalDate.of(prolepticYear, month, dayOfMonth)
    }

    /**
     * Obtains an ISO local date from the proleptic-year and day-of-year fields.
     *
     *
     * This is equivalent to [LocalDate.ofYearDay].
     *
     * @param prolepticYear  the ISO proleptic-year
     * @param dayOfYear  the ISO day-of-year
     * @return the ISO local date, not null
     * @throws DateTimeException if unable to create the date
     */
    override fun dateYearDay(prolepticYear: Int, dayOfYear: Int): LocalDate {
        return LocalDate.ofYearDay(prolepticYear, dayOfYear)
    }

    /**
     * Obtains an ISO local date from the epoch-day.
     *
     *
     * This is equivalent to [LocalDate.ofEpochDay].
     *
     * @param epochDay  the epoch day
     * @return the ISO local date, not null
     * @throws DateTimeException if unable to create the date
     */
    override fun dateEpochDay(epochDay: Long): LocalDate {
        return LocalDate.ofEpochDay(epochDay)
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an ISO local date from another date-time object.
     *
     *
     * This is equivalent to [LocalDate.from].
     *
     * @param temporal  the date-time object to convert, not null
     * @return the ISO local date, not null
     * @throws DateTimeException if unable to create the date
     */
    override fun date(temporal: TemporalAccessor): LocalDate {
        return LocalDate.from(temporal)
    }

    /**
     * Gets the number of seconds from the epoch of 1970-01-01T00:00:00Z.
     *
     *
     * The number of seconds is calculated using the year,
     * month, day-of-month, hour, minute, second, and zoneOffset.
     *
     * @param prolepticYear  the year, from MIN_YEAR to MAX_YEAR
     * @param month  the month-of-year, from 1 to 12
     * @param dayOfMonth  the day-of-month, from 1 to 31
     * @param hour  the hour-of-day, from 0 to 23
     * @param minute  the minute-of-hour, from 0 to 59
     * @param second  the second-of-minute, from 0 to 59
     * @param zoneOffset the zone offset, not null
     * @return the number of seconds relative to 1970-01-01T00:00:00Z, may be negative
     * @throws DateTimeException if the value of any argument is out of range,
     * or if the day-of-month is invalid for the month-of-year
     * @since Java 9
     */
    override fun epochSecond(
        prolepticYear: Int, month: Int, dayOfMonth: Int,
        hour: Int, minute: Int, second: Int, zoneOffset: ZoneOffset
    ): Long {
        ChronoField.YEAR.checkValidValue(prolepticYear.toLong())
        ChronoField.MONTH_OF_YEAR.checkValidValue(month.toLong())
        ChronoField.DAY_OF_MONTH.checkValidValue(dayOfMonth.toLong())
        ChronoField.HOUR_OF_DAY.checkValidValue(hour.toLong())
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute.toLong())
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second.toLong())
        if (dayOfMonth > 28) {
            val dom = numberOfDaysOfMonth(prolepticYear, month)
            if (dayOfMonth > dom) {
                if (dayOfMonth == 29) {
                    throw DateTimeException("Invalid date 'February 29' as '$prolepticYear' is not a leap year")
                } else {
                    throw DateTimeException("Invalid date '" + Month.of(month).name + " " + dayOfMonth + "'")
                }
            }
        }

        var totalDays: Long = 0
        totalDays += 365L * prolepticYear
        if (prolepticYear >= 0) {
            totalDays += (prolepticYear + 3L) / 4 - (prolepticYear + 99L) / 100 + (prolepticYear + 399L) / 400
        } else {
            totalDays -= (prolepticYear / -4 - prolepticYear / -100 + prolepticYear / -400).toLong()
        }
        totalDays += ((367 * month - 362) / 12).toLong()
        totalDays += (dayOfMonth - 1).toLong()
        if (month > 2) {
            totalDays -= 1
            if (!IsoChronology.INSTANCE.isLeapYear(prolepticYear.toLong())) {
                totalDays -= 1
            }
        }
        totalDays -= DAYS_0000_TO_1970
        val timeinSec = (hour * 60 + minute) * 60 + second
        return MathUtils.addExact(MathUtils.multiplyExact(totalDays, 86400L),
                (timeinSec - zoneOffset.totalSeconds).toLong())
    }

    /**
     * Obtains an ISO local date-time from another date-time object.
     *
     *
     * This is equivalent to [LocalDateTime.from].
     *
     * @param temporal  the date-time object to convert, not null
     * @return the ISO local date-time, not null
     * @throws DateTimeException if unable to create the date-time
     */
    override fun localDateTime(temporal: TemporalAccessor): LocalDateTime {
        return LocalDateTime.from(temporal)
    }

    /**
     * Obtains an ISO zoned date-time from another date-time object.
     *
     *
     * This is equivalent to [ZonedDateTime.from].
     *
     * @param temporal  the date-time object to convert, not null
     * @return the ISO zoned date-time, not null
     * @throws DateTimeException if unable to create the date-time
     */
    override fun zonedDateTime(temporal: TemporalAccessor): ZonedDateTime {
        return ZonedDateTime.from(temporal)
    }

    /**
     * Obtains an ISO zoned date-time in this chronology from an `Instant`.
     *
     *
     * This is equivalent to [ZonedDateTime.ofInstant].
     *
     * @param instant  the instant to create the date-time from, not null
     * @param zone  the time-zone, not null
     * @return the zoned date-time, not null
     * @throws DateTimeException if the result exceeds the supported range
     */
    override fun zonedDateTime(instant: Instant, zone: ZoneId): ZonedDateTime {
        return ZonedDateTime.ofInstant(instant, zone)
    }

    /**
     * Checks if the year is a leap year, according to the ISO proleptic
     * calendar system rules.
     *
     *
     * This method applies the current rules for leap years across the whole time-line.
     * In general, a year is a leap year if it is divisible by four without
     * remainder. However, years divisible by 100, are not leap years, with
     * the exception of years divisible by 400 which are.
     *
     *
     * For example, 1904 is a leap year it is divisible by 4.
     * 1900 was not a leap year as it is divisible by 100, however 2000 was a
     * leap year as it is divisible by 400.
     *
     *
     * The calculation is proleptic - applying the same rules into the far future and far past.
     * This is historically inaccurate, but is correct for the ISO-8601 standard.
     *
     * @param prolepticYear  the ISO proleptic year to check
     * @return true if the year is leap, false otherwise
     */
    override fun isLeapYear(prolepticYear: Long): Boolean {
        return prolepticYear and 3 == 0L && (prolepticYear % 100 != 0L || prolepticYear % 400 == 0L)
        // prolepticYear % 4 == 0
    }

    /**
     * Compares this chronology to another chronology.
     *
     *
     * The comparison order first by the chronology ID string, then by any
     * additional information specific to the subclass.
     * It is "consistent with equals", as defined by [Comparable].
     *
     * @implSpec
     * This implementation compares the chronology ID.
     * Subclasses must compare any additional state that they store.
     *
     * @param other  the other chronology to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    override fun compareTo(other: Chronology): Int {
        return getId().compareTo(other.getId())
    }

    /**
     * Checks if this chronology is equal to another chronology.
     *
     *
     * The comparison is based on the entire state of the object.
     *
     * @implSpec
     * This implementation checks the type and calls
     * [.compareTo].
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other chronology
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other is IsoChronology) compareTo(other) == 0 else false
    }

    /**
     * A hash code for this chronology.
     *
     *
     * The hash code should be based on the entire state of the object.
     *
     * @implSpec
     * This implementation is based on the chronology ID and class.
     * Subclasses should add any additional state that they store.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return IsoChronology::class.hashCode() xor getId().hashCode()
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this chronology as a `String`, using the chronology ID.
     *
     * @return a string representation of this chronology, not null
     */
    override fun toString(): String {
        return getId()
    }

    /**
     * Gets the number of days for the given month in the given year.
     *
     * @param year the year to represent, from MIN_YEAR to MAX_YEAR
     * @param month the month-of-year to represent, from 1 to 12
     * @return the number of days for the given month in the given year
     */
    private fun numberOfDaysOfMonth(year: Int, month: Int): Int {
        return when (month) {
            2 -> (if (IsoChronology.INSTANCE.isLeapYear(year.toLong())) 29 else 28)
            4, 6, 9, 11 -> 30
            else -> 31
        }
    }
}