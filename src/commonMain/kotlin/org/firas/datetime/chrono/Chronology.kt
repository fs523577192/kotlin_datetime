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

import org.firas.datetime.DateTimeException
import org.firas.datetime.Instant
import org.firas.datetime.LocalTime
import org.firas.datetime.temporal.ChronoField
import org.firas.datetime.temporal.TemporalAccessor
import org.firas.datetime.temporal.TemporalQueries
import org.firas.datetime.util.MathUtils
import org.firas.datetime.zone.ZoneId
import org.firas.datetime.zone.ZoneOffset

/**
 * A calendar system, used to organize and identify dates.
 *
 *
 * The main date and time API is built on the ISO calendar system.
 * The chronology operates behind the scenes to represent the general concept of a calendar system.
 * For example, the Japanese, Minguo, Thai Buddhist and others.
 *
 *
 * Most other calendar systems also operate on the shared concepts of year, month and day,
 * linked to the cycles of the Earth around the Sun, and the Moon around the Earth.
 * These shared concepts are defined by {@link ChronoField} and are available
 * for use by any `Chronology` implementation:
 * <pre>
 *   LocalDate isoDate = ...
 *   ThaiBuddhistDate thaiDate = ...
 *   int isoYear = isoDate.get(ChronoField.YEAR);
 *   int thaiYear = thaiDate.get(ChronoField.YEAR);
 * </pre>
 * As shown, although the date objects are in different calendar systems, represented by different
 * `Chronology` instances, both can be queried using the same constant on `ChronoField`.
 * For a full discussion of the implications of this, see {@link ChronoLocalDate}.
 * In general, the advice is to use the known ISO-based `LocalDate`, rather than
 * `ChronoLocalDate`.
 *
 *
 * While a `Chronology` object typically uses `ChronoField` and is based on
 * an era, year-of-era, month-of-year, day-of-month model of a date, this is not required.
 * A `Chronology` instance may represent a totally different kind of calendar system,
 * such as the Mayan.
 *
 *
 * In practical terms, the `Chronology` instance also acts as a factory.
 * The {@link #of(String)} method allows an instance to be looked up by identifier,
 * while the {@link #ofLocale(Locale)} method allows lookup by locale.
 *
 *
 * The `Chronology` instance provides a set of methods to create `ChronoLocalDate` instances.
 * The date classes are used to manipulate specific dates.
 *
 * * {@link #dateNow() dateNow()}
 * * {@link #dateNow(Clock) dateNow(clock)}
 * * {@link #dateNow(ZoneId) dateNow(zone)}
 * * {@link #date(int, int, int) date(yearProleptic, month, day)}
 * * {@link #date(Era, int, int, int) date(era, yearOfEra, month, day)}
 * * {@link #dateYearDay(int, int) dateYearDay(yearProleptic, dayOfYear)}
 * * {@link #dateYearDay(Era, int, int) dateYearDay(era, yearOfEra, dayOfYear)}
 * * {@link #date(TemporalAccessor) date(TemporalAccessor)}
 *
 *
 * <h3 id="addcalendars">Adding New Calendars</h3>
 * The set of available chronologies can be extended by applications.
 * Adding a new calendar system requires the writing of an implementation of
 * `Chronology`, `ChronoLocalDate` and `Era`.
 * The majority of the logic specific to the calendar system will be in the
 * `ChronoLocalDate` implementation.
 * The `Chronology` implementation acts as a factory.
 *
 *
 *
 * To permit the discovery of additional chronologies, the {@link java.util.ServiceLoader ServiceLoader}
 * is used. A file must be added to the `META-INF/services` directory with the
 * name 'java.time.chrono.Chronology' listing the implementation classes.
 * See the ServiceLoader for more details on service loading.
 * For lookup by id or calendarType, the system provided calendars are found
 * first followed by application provided calendars.
 *
 *
 * Each chronology must define a chronology ID that is unique within the system.
 * If the chronology represents a calendar system defined by the
 * CLDR specification then the calendar type is the concatenation of the
 * CLDR type and, if applicable, the CLDR variant.
 *
 * @implSpec
 * This interface must be implemented with care to ensure other classes operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * Subclasses should be Serializable wherever possible.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
interface Chronology: Comparable<Chronology> {

    companion object {
        /**
         * Obtains an instance of `Chronology` from a temporal object.
         *
         *
         * This obtains a chronology based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `Chronology`.
         *
         *
         * The conversion will obtain the chronology using [TemporalQueries.chronology].
         * If the specified temporal object does not have a chronology, [IsoChronology] is returned.
         *
         *
         * This method matches the signature of the functional interface [TemporalQuery]
         * allowing it to be used as a query via method reference, `Chronology::from`.
         *
         * @param temporal  the temporal to convert, not null
         * @return the chronology, not null
         * @throws DateTimeException if unable to convert to a `Chronology`
         */
        fun from(temporal: TemporalAccessor): Chronology {
            val obj = temporal.query(TemporalQueries.CHRONO)
            return obj?:IsoChronology.INSTANCE
        }
    }

    /**
     * Gets the ID of the chronology.
     *
     *
     * The ID uniquely identifies the `Chronology`.
     * It can be used to lookup the `Chronology` using [.of].
     *
     * @return the chronology ID, not null
     * @see .getCalendarType
     */
    fun getId(): String

    /**
     * Gets the calendar type of the calendar system.
     *
     *
     * The calendar type is an identifier defined by the CLDR and
     * *Unicode Locale Data Markup Language (LDML)* specifications
     * to uniquely identify a calendar.
     * The `getCalendarType` is the concatenation of the CLDR calendar type
     * and the variant, if applicable, is appended separated by "-".
     * The calendar type is used to lookup the `Chronology` using [.of].
     *
     * @return the calendar system type, null if the calendar is not defined by CLDR/LDML
     * @see .getId
     */
    fun getCalendarType(): String

    /**
     * Obtains a local date in this chronology from the proleptic-year,
     * month-of-year and day-of-month fields.
     *
     * @param prolepticYear  the chronology proleptic-year
     * @param month  the chronology month-of-year
     * @param dayOfMonth  the chronology day-of-month
     * @return the local date in this chronology, not null
     * @throws DateTimeException if unable to create the date
     */
    fun date(prolepticYear: Int, month: Int, dayOfMonth: Int): ChronoLocalDate

    /**
     * Obtains a local date in this chronology from the proleptic-year and
     * day-of-year fields.
     *
     * @param prolepticYear  the chronology proleptic-year
     * @param dayOfYear  the chronology day-of-year
     * @return the local date in this chronology, not null
     * @throws DateTimeException if unable to create the date
     */
    fun dateYearDay(prolepticYear: Int, dayOfYear: Int): ChronoLocalDate

    /**
     * Obtains a local date in this chronology from the epoch-day.
     *
     *
     * The definition of [EPOCH_DAY][ChronoField.EPOCH_DAY] is the same
     * for all calendar systems, thus it can be used for conversion.
     *
     * @param epochDay  the epoch day
     * @return the local date in this chronology, not null
     * @throws DateTimeException if unable to create the date
     */
    fun dateEpochDay(epochDay: Long): ChronoLocalDate

    /**
     * Checks if the specified year is a leap year.
     *
     *
     * A leap-year is a year of a longer length than normal.
     * The exact meaning is determined by the chronology according to the following constraints.
     *
     *  * a leap-year must imply a year-length longer than a non leap-year.
     *  * a chronology that does not support the concept of a year must return false.
     *  * the correct result must be returned for all years within the
     * valid range of years for the chronology.
     *
     *
     *
     * Outside the range of valid years an implementation is free to return
     * either a best guess or false.
     * An implementation must not throw an exception, even if the year is
     * outside the range of valid years.
     *
     * @param prolepticYear  the proleptic-year to check, not validated for range
     * @return true if the year is a leap year
     */
    fun isLeapYear(prolepticYear: Long): Boolean

    /**
     * Gets the number of seconds from the epoch of 1970-01-01T00:00:00Z.
     *
     *
     * The number of seconds is calculated using the proleptic-year,
     * month, day-of-month, hour, minute, second, and zoneOffset.
     *
     * @param prolepticYear the chronology proleptic-year
     * @param month the chronology month-of-year
     * @param dayOfMonth the chronology day-of-month
     * @param hour the hour-of-day, from 0 to 23
     * @param minute the minute-of-hour, from 0 to 59
     * @param second the second-of-minute, from 0 to 59
     * @param zoneOffset the zone offset, not null
     * @return the number of seconds relative to 1970-01-01T00:00:00Z, may be negative
     * @throws DateTimeException if any of the values are out of range
     * @since 9
     */
    fun epochSecond(
        prolepticYear: Int, month: Int, dayOfMonth: Int,
        hour: Int, minute: Int, second: Int, zoneOffset: ZoneOffset
    ): Long {
        ChronoField.HOUR_OF_DAY.checkValidValue(hour.toLong())
        ChronoField.MINUTE_OF_HOUR.checkValidValue(minute.toLong())
        ChronoField.SECOND_OF_MINUTE.checkValidValue(second.toLong())
        val daysInSec = MathUtils.multiplyExact(date(prolepticYear, month, dayOfMonth).toEpochDay(), 86400)
        val timeinSec = ((hour * 60 + minute) * 60 + second).toLong()
        return MathUtils.addExact(daysInSec, timeinSec - zoneOffset.totalSeconds)
    }

    /**
     * Obtains a local date in this chronology from another temporal object.
     *
     *
     * This obtains a date in this chronology based on the specified temporal.
     * A `TemporalAccessor` represents an arbitrary set of date and time information,
     * which this factory converts to an instance of `ChronoLocalDate`.
     *
     *
     * The conversion typically uses the {@link ChronoField#EPOCH_DAY EPOCH_DAY}
     * field, which is standardized across calendar systems.
     *
     *
     * This method matches the signature of the functional interface {@link TemporalQuery}
     * allowing it to be used as a query via method reference, `aChronology::date`.
     *
     * @param temporal  the temporal object to convert, not null
     * @return the local date in this chronology, not null
     * @throws DateTimeException if unable to create the date
     * @see ChronoLocalDate#from(TemporalAccessor)
     */
    fun date(temporal: TemporalAccessor): ChronoLocalDate

    /**
     * Obtains a local date-time in this chronology from another temporal object.
     *
     *
     * This obtains a date-time in this chronology based on the specified temporal.
     * A `TemporalAccessor` represents an arbitrary set of date and time information,
     * which this factory converts to an instance of `ChronoLocalDateTime`.
     *
     *
     * The conversion extracts and combines the `ChronoLocalDate` and the
     * `LocalTime` from the temporal object.
     * Implementations are permitted to perform optimizations such as accessing
     * those fields that are equivalent to the relevant objects.
     * The result uses this chronology.
     *
     *
     * This method matches the signature of the functional interface {@link TemporalQuery}
     * allowing it to be used as a query via method reference, `aChronology::localDateTime`.
     *
     * @param temporal  the temporal object to convert, not null
     * @return the local date-time in this chronology, not null
     * @throws DateTimeException if unable to create the date-time
     * @see ChronoLocalDateTime#from(TemporalAccessor)
     */
    fun localDateTime(temporal: TemporalAccessor): ChronoLocalDateTime<out ChronoLocalDate> {
        try {
            return date(temporal).atTime(LocalTime.from(temporal))
        } catch (ex: DateTimeException) {
            throw DateTimeException("Unable to obtain ChronoLocalDateTime from TemporalAccessor: " +
                    temporal.getKClass(), ex)
        }
    }

    /**
     * Obtains a `ChronoZonedDateTime` in this chronology from another temporal object.
     * <p>
     * This obtains a zoned date-time in this chronology based on the specified temporal.
     * A `TemporalAccessor` represents an arbitrary set of date and time information,
     * which this factory converts to an instance of `ChronoZonedDateTime`.
     * <p>
     * The conversion will first obtain a `ZoneId` from the temporal object,
     * falling back to a `ZoneOffset` if necessary. It will then try to obtain
     * an `Instant`, falling back to a `ChronoLocalDateTime` if necessary.
     * The result will be either the combination of `ZoneId` or `ZoneOffset`
     * with `Instant` or `ChronoLocalDateTime`.
     * Implementations are permitted to perform optimizations such as accessing
     * those fields that are equivalent to the relevant objects.
     * The result uses this chronology.
     * <p>
     * This method matches the signature of the functional interface {@link TemporalQuery}
     * allowing it to be used as a query via method reference, `aChronology::zonedDateTime`.
     *
     * @param temporal  the temporal object to convert, not null
     * @return the zoned date-time in this chronology, not null
     * @throws DateTimeException if unable to create the date-time
     * @see ChronoZonedDateTime#from(TemporalAccessor)
     */
    fun zonedDateTime(temporal: TemporalAccessor): ChronoZonedDateTime<out ChronoLocalDate> {
        try {
            val zone = ZoneId.from(temporal)
            // try {
                val instant = Instant.from(temporal)
                return zonedDateTime(instant, zone)

            /*
            } catch (ex1: DateTimeException) {
                ChronoLocalDateTimeImpl<?> cldt = ChronoLocalDateTimeImpl.ensureValid(this, localDateTime(temporal));
                return ChronoZonedDateTimeImpl.ofBest(cldt, zone, null);
            }
            */
        } catch (ex: DateTimeException) {
            throw DateTimeException("Unable to obtain ChronoZonedDateTime from TemporalAccessor: " +
                    temporal.getKClass(), ex)
        }
    }
}