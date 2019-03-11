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
import org.firas.datetime.LocalTime
import org.firas.datetime.temporal.*

/**
 * A date without time-of-day or time-zone in an arbitrary chronology, intended
 * for advanced globalization use cases.
 * <p>
 * <b>Most applications should declare method signatures, fields and variables
 * as {@link LocalDate}, not this interface.</b>
 * <p>
 * A `ChronoLocalDate` is the abstract representation of a date where the
 * `Chronology chronology`, or calendar system, is pluggable.
 * The date is defined in terms of fields expressed by {@link TemporalField},
 * where most common implementations are defined in {@link ChronoField}.
 * The chronology defines how the calendar system operates and the meaning of
 * the standard fields.
 *
 * <h3>When to use this interface</h3>
 * The design of the API encourages the use of `LocalDate` rather than this
 * interface, even in the case where the application needs to deal with multiple
 * calendar systems.
 * <p>
 * This concept can seem surprising at first, as the natural way to globalize an
 * application might initially appear to be to abstract the calendar system.
 * However, as explored below, abstracting the calendar system is usually the wrong
 * approach, resulting in logic errors and hard to find bugs.
 * As such, it should be considered an application-wide architectural decision to choose
 * to use this interface as opposed to `LocalDate`.
 *
 * <h3>Architectural issues to consider</h3>
 * These are some of the points that must be considered before using this interface
 * throughout an application.
 * <p>
 * 1) Applications using this interface, as opposed to using just `LocalDate`,
 * face a significantly higher probability of bugs. This is because the calendar system
 * in use is not known at development time. A key cause of bugs is where the developer
 * applies assumptions from their day-to-day knowledge of the ISO calendar system
 * to code that is intended to deal with any arbitrary calendar system.
 * The section below outlines how those assumptions can cause problems
 * The primary mechanism for reducing this increased risk of bugs is a strong code review process.
 * This should also be considered a extra cost in maintenance for the lifetime of the code.
 * <p>
 * 2) This interface does not enforce immutability of implementations.
 * While the implementation notes indicate that all implementations must be immutable
 * there is nothing in the code or type system to enforce this. Any method declared
 * to accept a `ChronoLocalDate` could therefore be passed a poorly or
 * maliciously written mutable implementation.
 * <p>
 * 3) Applications using this interface  must consider the impact of eras.
 * `LocalDate` shields users from the concept of eras, by ensuring that `getYear()`
 * returns the proleptic year. That decision ensures that developers can think of
 * `LocalDate` instances as consisting of three fields - year, month-of-year and day-of-month.
 * By contrast, users of this interface must think of dates as consisting of four fields -
 * era, year-of-era, month-of-year and day-of-month. The extra era field is frequently
 * forgotten, yet it is of vital importance to dates in an arbitrary calendar system.
 * For example, in the Japanese calendar system, the era represents the reign of an Emperor.
 * Whenever one reign ends and another starts, the year-of-era is reset to one.
 * <p>
 * 4) The only agreed international standard for passing a date between two systems
 * is the ISO-8601 standard which requires the ISO calendar system. Using this interface
 * throughout the application will inevitably lead to the requirement to pass the date
 * across a network or component boundary, requiring an application specific protocol or format.
 * <p>
 * 5) Long term persistence, such as a database, will almost always only accept dates in the
 * ISO-8601 calendar system (or the related Julian-Gregorian). Passing around dates in other
 * calendar systems increases the complications of interacting with persistence.
 * <p>
 * 6) Most of the time, passing a `ChronoLocalDate` throughout an application
 * is unnecessary, as discussed in the last section below.
 *
 * <h3>False assumptions causing bugs in multi-calendar system code</h3>
 * As indicated above, there are many issues to consider when try to use and manipulate a
 * date in an arbitrary calendar system. These are some of the key issues.
 * <p>
 * Code that queries the day-of-month and assumes that the value will never be more than
 * 31 is invalid. Some calendar systems have more than 31 days in some months.
 * <p>
 * Code that adds 12 months to a date and assumes that a year has been added is invalid.
 * Some calendar systems have a different number of months, such as 13 in the Coptic or Ethiopic.
 * <p>
 * Code that adds one month to a date and assumes that the month-of-year value will increase
 * by one or wrap to the next year is invalid. Some calendar systems have a variable number
 * of months in a year, such as the Hebrew.
 * <p>
 * Code that adds one month, then adds a second one month and assumes that the day-of-month
 * will remain close to its original value is invalid. Some calendar systems have a large difference
 * between the length of the longest month and the length of the shortest month.
 * For example, the Coptic or Ethiopic have 12 months of 30 days and 1 month of 5 days.
 * <p>
 * Code that adds seven days and assumes that a week has been added is invalid.
 * Some calendar systems have weeks of other than seven days, such as the French Revolutionary.
 * <p>
 * Code that assumes that because the year of `date1` is greater than the year of `date2`
 * then `date1` is after `date2` is invalid. This is invalid for all calendar systems
 * when referring to the year-of-era, and especially untrue of the Japanese calendar system
 * where the year-of-era restarts with the reign of every new Emperor.
 * <p>
 * Code that treats month-of-year one and day-of-month one as the start of the year is invalid.
 * Not all calendar systems start the year when the month value is one.
 * <p>
 * In general, manipulating a date, and even querying a date, is wide open to bugs when the
 * calendar system is unknown at development time. This is why it is essential that code using
 * this interface is subjected to additional code reviews. It is also why an architectural
 * decision to avoid this interface type is usually the correct one.
 *
 * <h3>Using LocalDate instead</h3>
 * The primary alternative to using this interface throughout your application is as follows.
 * <ul>
 * <li>Declare all method signatures referring to dates in terms of `LocalDate`.
 * <li>Either store the chronology (calendar system) in the user profile or lookup
 *  the chronology from the user locale
 * <li>Convert the ISO `LocalDate` to and from the user's preferred calendar system during
 *  printing and parsing
 * </ul>
 * This approach treats the problem of globalized calendar systems as a localization issue
 * and confines it to the UI layer. This approach is in keeping with other localization
 * issues in the java platform.
 * <p>
 * As discussed above, performing calculations on a date where the rules of the calendar system
 * are pluggable requires skill and is not recommended.
 * Fortunately, the need to perform calculations on a date in an arbitrary calendar system
 * is extremely rare. For example, it is highly unlikely that the business rules of a library
 * book rental scheme will allow rentals to be for one month, where meaning of the month
 * is dependent on the user's preferred calendar system.
 * <p>
 * A key use case for calculations on a date in an arbitrary calendar system is producing
 * a month-by-month calendar for display and user interaction. Again, this is a UI issue,
 * and use of this interface solely within a few methods of the UI layer may be justified.
 * <p>
 * In any other part of the system, where a date must be manipulated in a calendar system
 * other than ISO, the use case will generally specify the calendar system to use.
 * For example, an application may need to calculate the next Islamic or Hebrew holiday
 * which may require manipulating the date.
 * This kind of use case can be handled as follows:
 * <ul>
 * <li>start from the ISO `LocalDate` being passed to the method
 * <li>convert the date to the alternate calendar system, which for this use case is known
 *  rather than arbitrary
 * <li>perform the calculation
 * <li>convert back to `LocalDate`
 * </ul>
 * Developers writing low-level frameworks or libraries should also avoid this interface.
 * Instead, one of the two general purpose access interfaces should be used.
 * Use {@link TemporalAccessor} if read-only access is required, or use {@link Temporal}
 * if read-write access is required.
 *
 * @implSpec
 * This interface must be implemented with care to ensure other classes operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * Subclasses should be Serializable wherever possible.
 * <p>
 * Additional calendar systems may be added to the system.
 * See {@link Chronology} for more details.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
interface ChronoLocalDate: Temporal, Comparable<ChronoLocalDate> {

    companion object {
        /**
         * Gets a comparator that compares `ChronoLocalDate` in
         * time-line order ignoring the chronology.
         *
         *
         * This comparator differs from the comparison in [.compareTo] in that it
         * only compares the underlying date and not the chronology.
         * This allows dates in different calendar systems to be compared based
         * on the position of the date on the local time-line.
         * The underlying comparison is equivalent to comparing the epoch-day.
         *
         * @return a comparator that compares in time-line order ignoring the chronology
         * @see .isAfter
         *
         * @see .isBefore
         *
         * @see .isEqual
         */
        val timeLineOrder: Comparator<ChronoLocalDate> =
            { date1: ChronoLocalDate, date2: ChronoLocalDate ->
                val a = date1.toEpochDay()
                val b = date2.toEpochDay()
                if (a > b) 1 else if (a < b) -1 else 0
            } as Comparator<ChronoLocalDate>

        /**
         * Obtains an instance of `ChronoLocalDate` from a temporal object.
         *
         *
         * This obtains a local date based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `ChronoLocalDate`.
         *
         *
         * The conversion extracts and combines the chronology and the date
         * from the temporal object. The behavior is equivalent to using
         * {@link Chronology#date(TemporalAccessor)} with the extracted chronology.
         * Implementations are permitted to perform optimizations such as accessing
         * those fields that are equivalent to the relevant objects.
         *
         *
         *
         * This method matches the signature of the functional interface [TemporalQuery]
         * allowing it to be used as a query via method reference, `ChronoLocalDate::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the date, not null
         * @throws DateTimeException if unable to convert to a `ChronoLocalDate`
         * @see Chronology#date(TemporalAccessor)
         */
        fun from(temporal: TemporalAccessor): ChronoLocalDate {
            if (temporal is ChronoLocalDate) {
                return temporal
            }
            val chrono = temporal.query(TemporalQueries.CHRONO)
            if (chrono == null) {
                throw DateTimeException("Unable to obtain ChronoLocalDate from TemporalAccessor: " +
                        temporal.getKClass())
            }
            return chrono.date(temporal)
        }
    } // companion object

    /**
     * Gets the chronology of this date.
     *
     *
     * The `Chronology` represents the calendar system in use.
     * The era and other fields in [ChronoField] are defined by the chronology.
     *
     * @return the chronology, not null
     */
    fun getChronology(): Chronology

    /**
     * Checks if the year is a leap year, as defined by the calendar system.
     * <p>
     * A leap-year is a year of a longer length than normal.
     * The exact meaning is determined by the chronology with the constraint that
     * a leap-year must imply a year-length longer than a non leap-year.
     * <p>
     * This default implementation uses {@link Chronology#isLeapYear(long)}.
     *
     * @return true if this date is in a leap year, false otherwise
     */
    fun isLeapYear(): Boolean

    /**
     * Returns the length of the month represented by this date, as defined by the calendar system.
     *
     *
     * This returns the length of the month in days.
     *
     * @return the length of the month in days
     */
    fun lengthOfMonth(): Int

    /**
     * Returns the length of the year represented by this date, as defined by the calendar system.
     * <p>
     * This returns the length of the year in days.
     * <p>
     * The default implementation uses {@link #isLeapYear()} and returns 365 or 366.
     *
     * @return the length of the year in days
     */
    fun lengthOfYear(): Int {
        return if (isLeapYear()) 366 else 365
    }

    /**
     * Converts this date to the Epoch Day.
     * <p>
     * The {@link ChronoField#EPOCH_DAY Epoch Day count} is a simple
     * incrementing count of days where day 0 is 1970-01-01 (ISO).
     * This definition is the same for all chronologies, enabling conversion.
     * <p>
     * This default implementation queries the `EPOCH_DAY` field.
     *
     * @return the Epoch Day equivalent to this date
     */
    fun toEpochDay(): Long

    /**
     * Checks if the specified field is supported.
     *
     *
     * This checks if the specified field can be queried on this date.
     * If false, then calling the [range][.range],
     * [get][.get] and [.with]
     * methods will throw an exception.
     *
     *
     * The set of supported fields is defined by the chronology and normally includes
     * all `ChronoField` date fields.
     *
     *
     * If the field is not a `ChronoField`, then the result of this method
     * is obtained by invoking `TemporalField.isSupportedBy(TemporalAccessor)`
     * passing `this` as the argument.
     * Whether the field is supported is determined by the field.
     *
     * @param field  the field to check, null returns false
     * @return true if the field can be queried, false if not
     */
    override fun isSupported(field: TemporalField): Boolean {
        return if (field is ChronoField) {
            field.isDateBased()
        } else field.isSupportedBy(this)
    }

    /**
     * Checks if the specified unit is supported.
     *
     *
     * This checks if the specified unit can be added to or subtracted from this date.
     * If false, then calling the [.plus] and
     * [minus][.minus] methods will throw an exception.
     *
     *
     * The set of supported units is defined by the chronology and normally includes
     * all `ChronoUnit` date units except `FOREVER`.
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
            unit.isDateBased()
        } else unit.isSupportedBy(this)
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this date to another date, including the chronology.
     *
     *
     * The comparison is based first on the underlying time-line date, then
     * on the chronology.
     * It is "consistent with equals", as defined by [Comparable].
     *
     *
     * For example, the following is the comparator order:
     *
     *  1. `2012-12-03 (ISO)`
     *  1. `2012-12-04 (ISO)`
     *  1. `2555-12-04 (ThaiBuddhist)`
     *  1. `2012-12-05 (ISO)`
     *
     * Values #2 and #3 represent the same date on the time-line.
     * When two values represent the same date, the chronology ID is compared to distinguish them.
     * This step is needed to make the ordering "consistent with equals".
     *
     *
     * If all the date objects being compared are in the same chronology, then the
     * additional chronology stage is not required and only the local date is used.
     * To compare the dates of two `TemporalAccessor` instances, including dates
     * in two different chronologies, use [ChronoField.EPOCH_DAY] as a comparator.
     *
     *
     * This default implementation performs the comparison defined above.
     *
     * @param other  the other date to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    override operator fun compareTo(other: ChronoLocalDate): Int {
        val a = this.toEpochDay()
        val b = other.toEpochDay()
        return if (a > b) 1 else if (a < b) -1 else this.getChronology().compareTo(other.getChronology())
    }

    /**
     * Checks if this date is after the specified date ignoring the chronology.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the underlying date and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     * This is equivalent to using `date1.toEpochDay() > date2.toEpochDay()`.
     *
     *
     * This default implementation performs the comparison based on the epoch-day.
     *
     * @param other  the other date to compare to, not null
     * @return true if this is after the specified date
     */
    fun isAfter(other: ChronoLocalDate): Boolean {
        return this.toEpochDay() > other.toEpochDay()
    }

    /**
     * Checks if this date is before the specified date ignoring the chronology.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the underlying date and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     * This is equivalent to using `date1.toEpochDay() < date2.toEpochDay()`.
     *
     *
     * This default implementation performs the comparison based on the epoch-day.
     *
     * @param other  the other date to compare to, not null
     * @return true if this is before the specified date
     */
    fun isBefore(other: ChronoLocalDate): Boolean {
        return this.toEpochDay() < other.toEpochDay()
    }

    /**
     * Checks if this date is equal to the specified date ignoring the chronology.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the underlying date and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     * This is equivalent to using `date1.toEpochDay() == date2.toEpochDay()`.
     *
     *
     * This default implementation performs the comparison based on the epoch-day.
     *
     * @param other  the other date to compare to, not null
     * @return true if the underlying date is equal to the specified date
     */
    fun isEqual(other: ChronoLocalDate): Boolean {
        return this.toEpochDay() == other.toEpochDay()
    }

    /**
     * Combines this date with a time to create a `ChronoLocalDateTime`.
     *
     *
     * This returns a `ChronoLocalDateTime` formed from this date at the specified time.
     * All possible combinations of date and time are valid.
     *
     * @param localTime  the local time to use, not null
     * @return the local date-time formed from this date and the specified time, not null
     */
    fun atTime(localTime: LocalTime): ChronoLocalDateTime<*>
}