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
 * Copyright (c) 2011-2012, Stephen Colebourne & Michael Nascimento Santos
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
package org.firas.datetime.temporal

import org.firas.datetime.DateTimeException
import org.firas.datetime.DayOfWeek
import org.firas.datetime.chrono.ChronoLocalDate
import org.firas.datetime.chrono.Chronology
import org.firas.datetime.format.ResolverStyle
import org.firas.datetime.util.MathUtils
import org.firas.util.concurrent.getConcurrentHashMap
import org.firas.util.putIfAbsent
import kotlin.jvm.JvmStatic
import kotlin.jvm.Transient

/**
 * Localized definitions of the day-of-week, week-of-month and week-of-year fields.
 * <p>
 * A standard week is seven days long, but cultures have different definitions for some
 * other aspects of a week. This class represents the definition of the week, for the
 * purpose of providing {@link TemporalField} instances.
 * <p>
 * WeekFields provides five fields,
 * {@link #dayOfWeek()}, {@link #weekOfMonth()}, {@link #weekOfYear()},
 * {@link #weekOfWeekBasedYear()}, and {@link #weekBasedYear()}
 * that provide access to the values from any {@linkplain Temporal temporal object}.
 * <p>
 * The computations for day-of-week, week-of-month, and week-of-year are based
 * on the  {@linkplain ChronoField#YEAR proleptic-year},
 * {@linkplain ChronoField#MONTH_OF_YEAR month-of-year},
 * {@linkplain ChronoField#DAY_OF_MONTH day-of-month}, and
 * {@linkplain ChronoField#DAY_OF_WEEK ISO day-of-week} which are based on the
 * {@linkplain ChronoField#EPOCH_DAY epoch-day} and the chronology.
 * The values may not be aligned with the {@linkplain ChronoField#YEAR_OF_ERA year-of-Era}
 * depending on the Chronology.
 * <p>A week is defined by:
 * <ul>
 * <li>The first day-of-week.
 * For example, the ISO-8601 standard considers Monday to be the first day-of-week.
 * <li>The minimal number of days in the first week.
 * For example, the ISO-8601 standard counts the first week as needing at least 4 days.
 * </ul>
 * Together these two values allow a year or month to be divided into weeks.
 *
 * <h3>Week of Month</h3>
 * One field is used: week-of-month.
 * The calculation ensures that weeks never overlap a month boundary.
 * The month is divided into periods where each period starts on the defined first day-of-week.
 * The earliest period is referred to as week 0 if it has less than the minimal number of days
 * and week 1 if it has at least the minimal number of days.
 *
 * <table class=striped style="text-align: left">
 * <caption>Examples of WeekFields</caption>
 * <thead>
 * <tr><th scope="col">Date</th><th scope="col">Day-of-week</th>
 *  <th scope="col">First day: Monday<br>Minimal days: 4</th><th scope="col">First day: Monday<br>Minimal days: 5</th></tr>
 * </thead>
 * <tbody>
 * <tr><th scope="row">2008-12-31</th><td>Wednesday</td>
 *  <td>Week 5 of December 2008</td><td>Week 5 of December 2008</td></tr>
 * <tr><th scope="row">2009-01-01</th><td>Thursday</td>
 *  <td>Week 1 of January 2009</td><td>Week 0 of January 2009</td></tr>
 * <tr><th scope="row">2009-01-04</th><td>Sunday</td>
 *  <td>Week 1 of January 2009</td><td>Week 0 of January 2009</td></tr>
 * <tr><th scope="row">2009-01-05</th><td>Monday</td>
 *  <td>Week 2 of January 2009</td><td>Week 1 of January 2009</td></tr>
 * </tbody>
 * </table>
 *
 * <h3>Week of Year</h3>
 * One field is used: week-of-year.
 * The calculation ensures that weeks never overlap a year boundary.
 * The year is divided into periods where each period starts on the defined first day-of-week.
 * The earliest period is referred to as week 0 if it has less than the minimal number of days
 * and week 1 if it has at least the minimal number of days.
 *
 * <h3>Week Based Year</h3>
 * Two fields are used for week-based-year, one for the
 * {@link #weekOfWeekBasedYear() week-of-week-based-year} and one for
 * {@link #weekBasedYear() week-based-year}.  In a week-based-year, each week
 * belongs to only a single year.  Week 1 of a year is the first week that
 * starts on the first day-of-week and has at least the minimum number of days.
 * The first and last weeks of a year may contain days from the
 * previous calendar year or next calendar year respectively.
 *
 * <table class=striped style="text-align: left;">
 * <caption>Examples of WeekFields for week-based-year</caption>
 * <thead>
 * <tr><th scope="col">Date</th><th scope="col">Day-of-week</th>
 *  <th scope="col">First day: Monday<br>Minimal days: 4</th><th scope="col">First day: Monday<br>Minimal days: 5</th></tr>
 * </thead>
 * <tbody>
 * <tr><th scope="row">2008-12-31</th><td>Wednesday</td>
 *  <td>Week 1 of 2009</td><td>Week 53 of 2008</td></tr>
 * <tr><th scope="row">2009-01-01</th><td>Thursday</td>
 *  <td>Week 1 of 2009</td><td>Week 53 of 2008</td></tr>
 * <tr><th scope="row">2009-01-04</th><td>Sunday</td>
 *  <td>Week 1 of 2009</td><td>Week 53 of 2008</td></tr>
 * <tr><th scope="row">2009-01-05</th><td>Monday</td>
 *  <td>Week 2 of 2009</td><td>Week 1 of 2009</td></tr>
 * </tbody>
 * </table>
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate from OpenJDK 1.8)
 */
class WeekFields private constructor(
    /**
     * The first day-of-week.
     */
    val firstDayOfWeek: DayOfWeek,

    /**
     * The minimal number of days in the first week.
     */
    val minimalDaysInFirstWeek: Int
) {
    // implementation notes
    // querying week-of-month or week-of-year should return the week value bound within the month/year
    // however, setting the week value should be lenient (use plus/minus weeks)
    // allow week-of-month outer range [0 to 6]
    // allow week-of-year outer range [0 to 54]
    // this is because callers shouldn't be expected to know the details of validity

    init {
        if (this.minimalDaysInFirstWeek < 1 || this.minimalDaysInFirstWeek > 7) {
            throw IllegalArgumentException("Minimal number of days is invalid")
        }
    }

    companion object {
        /**
         * The cache of rules by firstDayOfWeek plus minimalDays.
         * Initialized first to be available for definition of ISO, etc.
         */
        @JvmStatic
        private val CACHE = getConcurrentHashMap(String::class, WeekFields::class, 4, 0.75f, 2)

        /**
         * The ISO-8601 definition, where a week starts on Monday and the first week
         * has a minimum of 4 days.
         *
         *
         * The ISO-8601 standard defines a calendar system based on weeks.
         * It uses the week-based-year and week-of-week-based-year concepts to split
         * up the passage of days instead of the standard year/month/day.
         *
         *
         * Note that the first week may start in the previous calendar year.
         * Note also that the first few days of a calendar year may be in the
         * week-based-year corresponding to the previous calendar year.
         */
        @JvmStatic
        val ISO = WeekFields(DayOfWeek.MONDAY, 4)

        /**
         * The common definition of a week that starts on Sunday and the first week
         * has a minimum of 1 day.
         *
         *
         * Defined as starting on Sunday and with a minimum of 1 day in the month.
         * This week definition is in use in the US and other European countries.
         */
        @JvmStatic
        val SUNDAY_START = WeekFields.of(DayOfWeek.SUNDAY, 1)

        /**
         * Obtains an instance of {@code WeekFields} from the first day-of-week and minimal days.
         *
         *
         * The first day-of-week defines the ISO {@code DayOfWeek} that is day 1 of the week.
         * The minimal number of days in the first week defines how many days must be present
         * in a month or year, starting from the first day-of-week, before the week is counted
         * as the first week. A value of 1 will count the first day of the month or year as part
         * of the first week, whereas a value of 7 will require the whole seven days to be in
         * the new month or year.
         *
         *
         * WeekFields instances are singletons; for each unique combination
         * of {@code firstDayOfWeek} and {@code minimalDaysInFirstWeek}
         * the same instance will be returned.
         *
         * @param firstDayOfWeek  the first day of the week, not null
         * @param minimalDaysInFirstWeek  the minimal number of days in the first week, from 1 to 7
         * @return the week-definition, not null
         * @throws IllegalArgumentException if the minimal days value is less than one
         *      or greater than 7
         */
        fun of(firstDayOfWeek: DayOfWeek, minimalDaysInFirstWeek: Int): WeekFields {
            val key = firstDayOfWeek.toString() + minimalDaysInFirstWeek
            var rules = CACHE.get(key)
            if (rules == null) {
                rules = WeekFields(firstDayOfWeek, minimalDaysInFirstWeek)
                CACHE.putIfAbsent(key, rules)
                rules = CACHE.get(key)!!
            }
            return rules
        }

        //-----------------------------------------------------------------------
        /**
         * Field type that computes DayOfWeek, WeekOfMonth, and WeekOfYear
         * based on a WeekFields.
         * A separate Field instance is required for each different WeekFields;
         * combination of start of week and minimum number of days.
         * Constructors are provided to create fields for DayOfWeek, WeekOfMonth,
         * and WeekOfYear.
         */
        class ComputedDayOfField(
            private val name: String,
            private val weekDef: WeekFields,
            private val baseUnit: TemporalUnit,
            private val rangeUnit: TemporalUnit,
            private val range: ValueRange
        ): TemporalField {

            companion object {
                @JvmStatic
                private val DAY_OF_WEEK_RANGE = ValueRange.of(1, 7)

                @JvmStatic
                private val WEEK_OF_MONTH_RANGE = ValueRange.of(0, 1, 4, 6)

                @JvmStatic
                private val WEEK_OF_YEAR_RANGE = ValueRange.of(0, 1, 52, 54)

                @JvmStatic
                private val WEEK_OF_WEEK_BASED_YEAR_RANGE = ValueRange.of(1, 52, 53)

                /**
                 * Returns a field to access the day of week,
                 * computed based on a WeekFields.
                 *
                 *
                 * The WeekDefintion of the first day of the week is used with
                 * the ISO DAY_OF_WEEK field to compute week boundaries.
                 */
                internal fun ofDayOfWeekField(weekDef: WeekFields): ComputedDayOfField {
                    return ComputedDayOfField("DayOfWeek", weekDef, ChronoUnit.DAYS, ChronoUnit.WEEKS, DAY_OF_WEEK_RANGE)
                }

                /**
                 * Returns a field to access the week of month,
                 * computed based on a WeekFields.
                 * @see WeekFields.weekOfMonth
                 */
                internal fun ofWeekOfMonthField(weekDef: WeekFields): ComputedDayOfField {
                    return ComputedDayOfField("WeekOfMonth", weekDef,
                        ChronoUnit.WEEKS, ChronoUnit.MONTHS, WEEK_OF_MONTH_RANGE)
                }

                /**
                 * Returns a field to access the week of year,
                 * computed based on a WeekFields.
                 * @see WeekFields.weekOfYear
                 */
                internal fun ofWeekOfYearField(weekDef: WeekFields): ComputedDayOfField {
                    return ComputedDayOfField("WeekOfYear", weekDef, ChronoUnit.WEEKS,
                        ChronoUnit.YEARS, WEEK_OF_YEAR_RANGE)
                }

                /**
                 * Returns a field to access the week of week-based-year,
                 * computed based on a WeekFields.
                 * @see WeekFields.weekOfWeekBasedYear
                 */
                internal fun ofWeekOfWeekBasedYearField(weekDef: WeekFields): ComputedDayOfField {
                    return ComputedDayOfField(
                        "WeekOfWeekBasedYear",
                        weekDef,
                        ChronoUnit.WEEKS,
                        IsoFields.WEEK_BASED_YEARS,
                        WEEK_OF_WEEK_BASED_YEAR_RANGE
                    )
                }

                /**
                 * Returns a field to access the week of week-based-year,
                 * computed based on a WeekFields.
                 * @see WeekFields.weekBasedYear
                 */
                internal fun ofWeekBasedYearField(weekDef: WeekFields): ComputedDayOfField {
                    return ComputedDayOfField(
                        "WeekBasedYear",
                        weekDef,
                        IsoFields.WEEK_BASED_YEARS,
                        ChronoUnit.FOREVER,
                        ChronoField.YEAR.range()
                    )
                }
            } // ComputedDayOfField.Companion

            override fun getFrom(temporal: TemporalAccessor): Long {
                return if (rangeUnit === ChronoUnit.WEEKS) {  // day-of-week
                    localizedDayOfWeek(temporal).toLong()
                } else if (rangeUnit === ChronoUnit.MONTHS) {  // week-of-month
                    localizedWeekOfMonth(temporal)
                } else if (rangeUnit === ChronoUnit.YEARS) {  // week-of-year
                    localizedWeekOfYear(temporal)
                } else if (rangeUnit === IsoFields.WEEK_BASED_YEARS) {
                    localizedWeekOfWeekBasedYear(temporal).toLong()
                } else if (rangeUnit === ChronoUnit.FOREVER) {
                    localizedWeekBasedYear(temporal).toLong()
                } else {
                    throw IllegalStateException("unreachable, rangeUnit: $rangeUnit, this: $this")
                }
            }

            override fun <R : Temporal> adjustInto(temporal: R, newValue: Long): R {
                // Check the new value and get the old value of the field
                val newVal = range.checkValidIntValue(newValue, this)  // lenient check range
                val currentVal = temporal.get(this)
                if (newVal == currentVal) {
                    return temporal
                }

                if (rangeUnit == ChronoUnit.FOREVER) {     // replace year of WeekBasedYear
                    // Create a new date object with the same chronology,
                    // the desired year and the same week and dow.
                    val idow = temporal.get(weekDef.dayOfWeek)
                    val wowby = temporal.get(weekDef.weekOfWeekBasedYear)
                    return ofWeekBasedYear(Chronology.from(temporal), newValue.toInt(), wowby, idow) as R
                } else {
                    // Compute the difference and add that using the base unit of the field
                    return temporal.plus(newVal.toLong() - currentVal, baseUnit) as R
                }
            }

            override fun resolve(
                fieldValues: MutableMap<TemporalField, Long>,
                partialTemporal: TemporalAccessor,
                resolverStyle: ResolverStyle
            ): TemporalAccessor? {
                val value = fieldValues.get(this)!!
                val newValue = MathUtils.toIntExact(value)  // broad limit makes overflow checking lighter
                // first convert localized day-of-week to ISO day-of-week
                // doing this first handles case where both ISO and localized were parsed and might mismatch
                // day-of-week is always strict as two different day-of-week values makes lenient complex
                if (rangeUnit == ChronoUnit.WEEKS) {  // day-of-week
                    val checkedValue = range.checkValidIntValue(value, this)  // no leniency as too complex
                    val startDow = weekDef.firstDayOfWeek.getValue()
                    val isoDow = MathUtils.floorMod((startDow - 1) + (checkedValue - 1), 7) + 1L
                    fieldValues.remove(this)
                    fieldValues.put(ChronoField.DAY_OF_WEEK, isoDow)
                    return null
                }

                // can only build date if ISO day-of-week is present
                if (!fieldValues.containsKey(ChronoField.DAY_OF_WEEK)) {
                    return null
                }
                val isoDow = ChronoField.DAY_OF_WEEK.checkValidIntValue(fieldValues.get(ChronoField.DAY_OF_WEEK)!!)
                val dow = localizedDayOfWeek(isoDow)

                // build date
                val chrono = Chronology.from(partialTemporal)
                if (fieldValues.containsKey(ChronoField.YEAR)) {
                    val year = ChronoField.YEAR.checkValidIntValue(fieldValues.get(ChronoField.YEAR)!!)  // validate
                    if (rangeUnit == ChronoUnit.MONTHS && fieldValues.containsKey(ChronoField.MONTH_OF_YEAR)) {  // week-of-month
                        val month = fieldValues.get(ChronoField.MONTH_OF_YEAR)!!  // not validated yet
                        return resolveWoM(fieldValues, chrono, year, month, newValue.toLong(), dow, resolverStyle)
                    }
                    if (rangeUnit == ChronoUnit.YEARS) {  // week-of-year
                        return resolveWoY(fieldValues, chrono, year, newValue.toLong(), dow, resolverStyle)
                    }
                } else if ((rangeUnit == IsoFields.WEEK_BASED_YEARS || rangeUnit == ChronoUnit.FOREVER) &&
                        fieldValues.containsKey(weekDef.weekBasedYear) &&
                        fieldValues.containsKey(weekDef.weekOfWeekBasedYear)) { // week-of-week-based-year and year-of-week-based-year
                    return resolveWBY(fieldValues, chrono, dow, resolverStyle)
                }
                return null
            }

            override fun getBaseUnit(): TemporalUnit {
                return this.baseUnit
            }

            override fun getRangeUnit(): TemporalUnit {
                return this.rangeUnit
            }

            override fun isDateBased(): Boolean {
                return true
            }

            override fun isTimeBased(): Boolean {
                return false
            }

            override fun range(): ValueRange {
                return this.range
            }

            //-----------------------------------------------------------------------
            override fun isSupportedBy(temporal: TemporalAccessor): Boolean {
                if (temporal.isSupported(ChronoField.DAY_OF_WEEK)) {
                    if (rangeUnit === ChronoUnit.WEEKS) {  // day-of-week
                        return true
                    } else if (rangeUnit === ChronoUnit.MONTHS) {  // week-of-month
                        return temporal.isSupported(ChronoField.DAY_OF_MONTH)
                    } else if (rangeUnit === ChronoUnit.YEARS) {  // week-of-year
                        return temporal.isSupported(ChronoField.DAY_OF_YEAR)
                    } else if (rangeUnit === IsoFields.WEEK_BASED_YEARS) {
                        return temporal.isSupported(ChronoField.DAY_OF_YEAR)
                    } else if (rangeUnit === ChronoUnit.FOREVER) {
                        return temporal.isSupported(ChronoField.YEAR)
                    }
                }
                return false
            }

            override fun rangeRefinedBy(temporal: TemporalAccessor): ValueRange {
                return if (rangeUnit === ChronoUnit.WEEKS) {  // day-of-week
                    range
                } else if (rangeUnit === ChronoUnit.MONTHS) {  // week-of-month
                    rangeByWeek(temporal, ChronoField.DAY_OF_MONTH)
                } else if (rangeUnit === ChronoUnit.YEARS) {  // week-of-year
                    rangeByWeek(temporal, ChronoField.DAY_OF_YEAR)
                } else if (rangeUnit === IsoFields.WEEK_BASED_YEARS) {
                    rangeWeekOfWeekBasedYear(temporal)
                } else if (rangeUnit === ChronoUnit.FOREVER) {
                    ChronoField.YEAR.range()
                } else {
                    throw IllegalStateException("unreachable, rangeUnit: $rangeUnit, this: $this")
                }
            }

            override fun toString(): String {
                return this.name + "[" + this.weekDef.toString() + "]"
            }

            private fun localizedDayOfWeek(temporal: TemporalAccessor): Int {
                val sow = weekDef.firstDayOfWeek.getValue()
                val isoDow = temporal[ChronoField.DAY_OF_WEEK]
                return MathUtils.floorMod(isoDow - sow, 7) + 1
            }

            private fun localizedDayOfWeek(isoDow: Int): Int {
                val sow = weekDef.firstDayOfWeek.getValue()
                return MathUtils.floorMod(isoDow - sow, 7) + 1
            }

            private fun localizedWeekOfMonth(temporal: TemporalAccessor): Long {
                val dow = localizedDayOfWeek(temporal)
                val dom = temporal[ChronoField.DAY_OF_MONTH]
                val offset = startOfWeekOffset(dom, dow)
                return computeWeek(offset, dom).toLong()
            }

            private fun localizedWeekOfYear(temporal: TemporalAccessor): Long {
                val dow = localizedDayOfWeek(temporal)
                val doy = temporal[ChronoField.DAY_OF_YEAR]
                val offset = startOfWeekOffset(doy, dow)
                return computeWeek(offset, doy).toLong()
            }

            /**
             * Returns the year of week-based-year for the temporal.
             * The year can be the previous year, the current year, or the next year.
             * @param temporal a date of any chronology, not null
             * @return the year of week-based-year for the date
             */
            private fun localizedWeekBasedYear(temporal: TemporalAccessor): Int {
                val dow = localizedDayOfWeek(temporal)
                val year = temporal[ChronoField.YEAR]
                val doy = temporal[ChronoField.DAY_OF_YEAR]
                val offset = startOfWeekOffset(doy, dow)
                val week = computeWeek(offset, doy)
                if (week == 0) {
                    // Day is in end of week of previous year; return the previous year
                    return year - 1
                } else {
                    // If getting close to end of year, use higher precision logic
                    // Check if date of year is in partial week associated with next year
                    val dayRange = temporal.range(ChronoField.DAY_OF_YEAR)
                    val yearLen = dayRange.getMaximum().toInt()
                    val newYearWeek = computeWeek(offset, yearLen + weekDef.minimalDaysInFirstWeek)
                    if (week >= newYearWeek) {
                        return year + 1
                    }
                }
                return year
            }

            /**
             * Returns the week of week-based-year for the temporal.
             * The week can be part of the previous year, the current year,
             * or the next year depending on the week start and minimum number
             * of days.
             * @param temporal  a date of any chronology
             * @return the week of the year
             * @see .localizedWeekBasedYear
             */
            private fun localizedWeekOfWeekBasedYear(temporal: TemporalAccessor): Int {
                val dow = localizedDayOfWeek(temporal)
                val doy = temporal[ChronoField.DAY_OF_YEAR]
                val offset = startOfWeekOffset(doy, dow)
                var week = computeWeek(offset, doy)
                if (week == 0) {
                    // Day is in end of week of previous year
                    // Recompute from the last day of the previous year
                    var date = Chronology.from(temporal).date(temporal)
                    date = date.minus(doy.toLong(), ChronoUnit.DAYS) as ChronoLocalDate   // Back down into previous year
                    return localizedWeekOfWeekBasedYear(date)
                } else if (week > 50) {
                    // If getting close to end of year, use higher precision logic
                    // Check if date of year is in partial week associated with next year
                    val dayRange = temporal.range(ChronoField.DAY_OF_YEAR)
                    val yearLen = dayRange.getMaximum().toInt()
                    val newYearWeek = computeWeek(offset, yearLen + weekDef.minimalDaysInFirstWeek)
                    if (week >= newYearWeek) {
                        // Overlaps with week of following year; reduce to week in following year
                        week = week - newYearWeek + 1
                    }
                }
                return week
            }

            /**
             * Returns an offset to align week start with a day of month or day of year.
             *
             * @param day  the day; 1 through infinity
             * @param dow  the day of the week of that day; 1 through 7
             * @return  an offset in days to align a day with the start of the first 'full' week
             */
            private fun startOfWeekOffset(day: Int, dow: Int): Int {
                // offset of first day corresponding to the day of week in first 7 days (zero origin)
                val weekStart = MathUtils.floorMod(day - dow, 7)
                var offset = -weekStart
                if (weekStart + 1 > weekDef.minimalDaysInFirstWeek) {
                    // The previous week has the minimum days in the current month to be a 'week'
                    offset = 7 - weekStart
                }
                return offset
            }

            /**
             * Returns the week number computed from the reference day and reference dayOfWeek.
             *
             * @param offset the offset to align a date with the start of week
             * from [.startOfWeekOffset].
             * @param day  the day for which to compute the week number
             * @return the week number where zero is used for a partial week and 1 for the first full week
             */
            private fun computeWeek(offset: Int, day: Int): Int {
                return (7 + offset + (day - 1)) / 7
            }

            private fun resolveWoM(
                fieldValues: MutableMap<TemporalField, Long>,
                chrono: Chronology,
                year: Int,
                month: Long,
                wom: Long,
                localDow: Int,
                resolverStyle: ResolverStyle
            ): ChronoLocalDate {
                var date: ChronoLocalDate
                if (resolverStyle === ResolverStyle.LENIENT) {
                    date = chrono.date(year, 1, 1).plus(
                            MathUtils.subtractExact(month, 1), ChronoUnit.MONTHS) as ChronoLocalDate
                    val weeks = MathUtils.subtractExact(wom, localizedWeekOfMonth(date))
                    val days = localDow - localizedDayOfWeek(date)  // safe from overflow
                    date = date.plus(MathUtils.addExact(MathUtils.multiplyExact(weeks, 7),
                            days.toLong()), ChronoUnit.DAYS) as ChronoLocalDate
                } else {
                    val monthValid = ChronoField.MONTH_OF_YEAR.checkValidIntValue(month)  // validate
                    date = chrono.date(year, monthValid, 1)
                    val womInt = range.checkValidIntValue(wom, this)  // validate
                    val weeks = (womInt - localizedWeekOfMonth(date)).toInt()  // safe from overflow
                    val days = localDow - localizedDayOfWeek(date)  // safe from overflow
                    date = date.plus(weeks * 7L + days, ChronoUnit.DAYS) as ChronoLocalDate
                    if (resolverStyle === ResolverStyle.STRICT && date.getLong(ChronoField.MONTH_OF_YEAR) != month) {
                        throw DateTimeException("Strict mode rejected resolved date as it is in a different month")
                    }
                }
                fieldValues.remove(this)
                fieldValues.remove(ChronoField.YEAR)
                fieldValues.remove(ChronoField.MONTH_OF_YEAR)
                fieldValues.remove(ChronoField.DAY_OF_WEEK)
                return date
            }

            private fun resolveWoY(
                fieldValues: MutableMap<TemporalField, Long>,
                chrono: Chronology,
                year: Int,
                woy: Long,
                localDow: Int,
                resolverStyle: ResolverStyle
            ): ChronoLocalDate {
                var date = chrono.date(year, 1, 1)
                if (resolverStyle === ResolverStyle.LENIENT) {
                    val weeks = MathUtils.subtractExact(woy, localizedWeekOfYear(date))
                    val days = localDow - localizedDayOfWeek(date)  // safe from overflow
                    date = date.plus(MathUtils.addExact(MathUtils.multiplyExact(weeks, 7),
                            days.toLong()), ChronoUnit.DAYS) as ChronoLocalDate
                } else {
                    val womInt = range.checkValidIntValue(woy, this)  // validate
                    val weeks = (womInt - localizedWeekOfYear(date)).toInt()  // safe from overflow
                    val days = localDow - localizedDayOfWeek(date)  // safe from overflow
                    date = date.plus(weeks * 7L + days, ChronoUnit.DAYS) as ChronoLocalDate
                    if (resolverStyle === ResolverStyle.STRICT && date.getLong(ChronoField.YEAR) != year.toLong()) {
                        throw DateTimeException("Strict mode rejected resolved date as it is in a different year")
                    }
                }
                fieldValues.remove(this)
                fieldValues.remove(ChronoField.YEAR)
                fieldValues.remove(ChronoField.DAY_OF_WEEK)
                return date
            }

            private fun resolveWBY(
                    fieldValues: MutableMap<TemporalField, Long>,
                    chrono: Chronology, localDow: Int, resolverStyle: ResolverStyle
            ): ChronoLocalDate {
                val yowby = weekDef.weekBasedYear.range().checkValidIntValue(
                        fieldValues.get(weekDef.weekBasedYear)!!, weekDef.weekBasedYear)
                var date: ChronoLocalDate
                if (resolverStyle == ResolverStyle.LENIENT) {
                    date = ofWeekBasedYear(chrono, yowby, 1, localDow)
                    val wowby = fieldValues.get(weekDef.weekOfWeekBasedYear)!!
                    val weeks = MathUtils.subtractExact(wowby, 1)
                    date = date.plus(weeks, ChronoUnit.WEEKS) as ChronoLocalDate
                } else {
                    val wowby = weekDef.weekOfWeekBasedYear.range().checkValidIntValue(
                            fieldValues.get(weekDef.weekOfWeekBasedYear)!!, weekDef.weekOfWeekBasedYear)  // validate
                    date = ofWeekBasedYear(chrono, yowby, wowby, localDow)
                    if (resolverStyle == ResolverStyle.STRICT && localizedWeekBasedYear(date) != yowby) {
                        throw DateTimeException("Strict mode rejected resolved date as it is in a different week-based-year")
                    }
                }
                fieldValues.remove(this)
                fieldValues.remove(weekDef.weekBasedYear)
                fieldValues.remove(weekDef.weekOfWeekBasedYear)
                fieldValues.remove(ChronoField.DAY_OF_WEEK)
                return date
            }

            /**
             * Map the field range to a week range
             * @param temporal the temporal
             * @param field the field to get the range of
             * @return the ValueRange with the range adjusted to weeks.
             */
            private fun rangeByWeek(temporal: TemporalAccessor, field: TemporalField): ValueRange {
                val dow = localizedDayOfWeek(temporal)
                val offset = startOfWeekOffset(temporal[field], dow)
                val fieldRange = temporal.range(field)
                return ValueRange.of(
                    computeWeek(offset, fieldRange.getMinimum().toInt()).toLong(),
                    computeWeek(offset, fieldRange.getMaximum().toInt()).toLong()
                )
            }

            /**
             * Map the field range to a week range of a week year.
             * @param temporal  the temporal
             * @return the ValueRange with the range adjusted to weeks.
             */
            private fun rangeWeekOfWeekBasedYear(temporal: TemporalAccessor): ValueRange {
                if (!temporal.isSupported(ChronoField.DAY_OF_YEAR)) {
                    return WEEK_OF_YEAR_RANGE
                }
                val dow = localizedDayOfWeek(temporal)
                val doy = temporal[ChronoField.DAY_OF_YEAR]
                val offset = startOfWeekOffset(doy, dow)
                val week = computeWeek(offset, doy)
                if (week == 0) {
                    // Day is in end of week of previous year
                    // Recompute from the last day of the previous year
                    var date = Chronology.from(temporal).date(temporal)
                    date = date.minus(doy + 7L, ChronoUnit.DAYS) as ChronoLocalDate  // Back down into previous year
                    return rangeWeekOfWeekBasedYear(date)
                }
                // Check if day of year is in partial week associated with next year
                val dayRange = temporal.range(ChronoField.DAY_OF_YEAR)
                val yearLen = dayRange.getMaximum().toInt()
                val newYearWeek = computeWeek(offset, yearLen + weekDef.minimalDaysInFirstWeek)

                if (week >= newYearWeek) {
                    // Overlaps with weeks of following year; recompute from a week in following year
                    var date = Chronology.from(temporal).date(temporal)
                    date = date.plus(yearLen - doy + 1L + 7L, ChronoUnit.DAYS) as ChronoLocalDate
                    return rangeWeekOfWeekBasedYear(date)
                }
                return ValueRange.of(1, (newYearWeek - 1).toLong())
            }

            /**
             * Return a new week-based-year date of the Chronology, year, week-of-year,
             * and dow of week.
             * @param chrono The chronology of the new date
             * @param yowby the year of the week-based-year
             * @param wowby the week of the week-based-year
             * @param dow the day of the week
             * @return a ChronoLocalDate for the requested year, week of year, and day of week
             */
            private fun ofWeekBasedYear(
                chrono: Chronology,
                yowby: Int, wowby: Int, dow: Int
            ): ChronoLocalDate {
                var _wowby = wowby
                val date = chrono.date(yowby, 1, 1)
                val ldow = localizedDayOfWeek(date)
                val offset = startOfWeekOffset(1, ldow)

                // Clamp the week of year to keep it in the same year
                val yearLen = date.lengthOfYear()
                val newYearWeek = computeWeek(offset, yearLen + weekDef.minimalDaysInFirstWeek)
                _wowby = minOf(_wowby, newYearWeek - 1)

                val days = -offset + (dow - 1) + (_wowby - 1) * 7L
                return date.plus(days, ChronoUnit.DAYS) as ChronoLocalDate
            }
        } // class ComputedDayOfField

        /**
         * Serialization version.
         */
        private const val serialVersionUID = -1177360819670808121L
    }

    /**
     * The field used to access the computed DayOfWeek.
     */
    @Transient
    private val dayOfWeek = ComputedDayOfField.ofDayOfWeekField(this)

    /**
     * The field used to access the computed WeekOfMonth.
     */
    @Transient
    private val weekOfMonth = ComputedDayOfField.ofWeekOfMonthField(this)

    /**
     * The field used to access the computed WeekOfYear.
     */
    @Transient
    private val weekOfYear = ComputedDayOfField.ofWeekOfYearField(this)

    /**
     * The field that represents the week-of-week-based-year.
     *
     *
     * This field allows the week of the week-based-year value to be queried and set.
     *
     *
     * This unit is an immutable and thread-safe singleton.
     */
    @Transient
    private val weekOfWeekBasedYear = ComputedDayOfField.ofWeekOfWeekBasedYearField(this)

    /**
     * The field that represents the week-based-year.
     *
     *
     * This field allows the week-based-year value to be queried and set.
     *
     *
     * This unit is an immutable and thread-safe singleton.
     */
    @Transient
    private val weekBasedYear = ComputedDayOfField.ofWeekBasedYearField(this)

    //-----------------------------------------------------------------------
    /**
     * Returns a field to access the day of week based on this `WeekFields`.
     *
     *
     * This is similar to [ChronoField.DAY_OF_WEEK] but uses values for
     * the day-of-week based on this `WeekFields`.
     * The days are numbered from 1 to 7 where the
     * [first day-of-week][.getFirstDayOfWeek] is assigned the value 1.
     *
     *
     * For example, if the first day-of-week is Sunday, then that will have the
     * value 1, with other days ranging from Monday as 2 to Saturday as 7.
     *
     *
     * In the resolving phase of parsing, a localized day-of-week will be converted
     * to a standardized `ChronoField` day-of-week.
     * The day-of-week must be in the valid range 1 to 7.
     * Other fields in this class build dates using the standardized day-of-week.
     *
     * @return a field providing access to the day-of-week with localized numbering, not null
     */
    fun dayOfWeek(): TemporalField {
        return dayOfWeek
    }

    /**
     * Returns a field to access the week of month based on this `WeekFields`.
     *
     *
     * This represents the concept of the count of weeks within the month where weeks
     * start on a fixed day-of-week, such as Monday.
     * This field is typically used with [WeekFields.dayOfWeek].
     *
     *
     * Week one (1) is the week starting on the [WeekFields.getFirstDayOfWeek]
     * where there are at least [WeekFields.getMinimalDaysInFirstWeek] days in the month.
     * Thus, week one may start up to `minDays` days before the start of the month.
     * If the first week starts after the start of the month then the period before is week zero (0).
     *
     *
     * For example:<br></br>
     * - if the 1st day of the month is a Monday, week one starts on the 1st and there is no week zero<br></br>
     * - if the 2nd day of the month is a Monday, week one starts on the 2nd and the 1st is in week zero<br></br>
     * - if the 4th day of the month is a Monday, week one starts on the 4th and the 1st to 3rd is in week zero<br></br>
     * - if the 5th day of the month is a Monday, week two starts on the 5th and the 1st to 4th is in week one<br></br>
     *
     *
     * This field can be used with any calendar system.
     *
     *
     * In the resolving phase of parsing, a date can be created from a year,
     * week-of-month, month-of-year and day-of-week.
     *
     *
     * In [strict mode][ResolverStyle.STRICT], all four fields are
     * validated against their range of valid values. The week-of-month field
     * is validated to ensure that the resulting month is the month requested.
     *
     *
     * In [smart mode][ResolverStyle.SMART], all four fields are
     * validated against their range of valid values. The week-of-month field
     * is validated from 0 to 6, meaning that the resulting date can be in a
     * different month to that specified.
     *
     *
     * In [lenient mode][ResolverStyle.LENIENT], the year and day-of-week
     * are validated against the range of valid values. The resulting date is calculated
     * equivalent to the following four stage approach.
     * First, create a date on the first day of the first week of January in the requested year.
     * Then take the month-of-year, subtract one, and add the amount in months to the date.
     * Then take the week-of-month, subtract one, and add the amount in weeks to the date.
     * Finally, adjust to the correct day-of-week within the localized week.
     *
     * @return a field providing access to the week-of-month, not null
     */
    fun weekOfMonth(): TemporalField {
        return weekOfMonth
    }

    /**
     * Returns a field to access the week of year based on this `WeekFields`.
     *
     *
     * This represents the concept of the count of weeks within the year where weeks
     * start on a fixed day-of-week, such as Monday.
     * This field is typically used with [WeekFields.dayOfWeek].
     *
     *
     * Week one(1) is the week starting on the [WeekFields.getFirstDayOfWeek]
     * where there are at least [WeekFields.getMinimalDaysInFirstWeek] days in the year.
     * Thus, week one may start up to `minDays` days before the start of the year.
     * If the first week starts after the start of the year then the period before is week zero (0).
     *
     *
     * For example:<br></br>
     * - if the 1st day of the year is a Monday, week one starts on the 1st and there is no week zero<br></br>
     * - if the 2nd day of the year is a Monday, week one starts on the 2nd and the 1st is in week zero<br></br>
     * - if the 4th day of the year is a Monday, week one starts on the 4th and the 1st to 3rd is in week zero<br></br>
     * - if the 5th day of the year is a Monday, week two starts on the 5th and the 1st to 4th is in week one<br></br>
     *
     *
     * This field can be used with any calendar system.
     *
     *
     * In the resolving phase of parsing, a date can be created from a year,
     * week-of-year and day-of-week.
     *
     *
     * In [strict mode][ResolverStyle.STRICT], all three fields are
     * validated against their range of valid values. The week-of-year field
     * is validated to ensure that the resulting year is the year requested.
     *
     *
     * In [smart mode][ResolverStyle.SMART], all three fields are
     * validated against their range of valid values. The week-of-year field
     * is validated from 0 to 54, meaning that the resulting date can be in a
     * different year to that specified.
     *
     *
     * In [lenient mode][ResolverStyle.LENIENT], the year and day-of-week
     * are validated against the range of valid values. The resulting date is calculated
     * equivalent to the following three stage approach.
     * First, create a date on the first day of the first week in the requested year.
     * Then take the week-of-year, subtract one, and add the amount in weeks to the date.
     * Finally, adjust to the correct day-of-week within the localized week.
     *
     * @return a field providing access to the week-of-year, not null
     */
    fun weekOfYear(): TemporalField {
        return weekOfYear
    }

    /**
     * Returns a field to access the week of a week-based-year based on this `WeekFields`.
     *
     *
     * This represents the concept of the count of weeks within the year where weeks
     * start on a fixed day-of-week, such as Monday and each week belongs to exactly one year.
     * This field is typically used with [WeekFields.dayOfWeek] and
     * [WeekFields.weekBasedYear].
     *
     *
     * Week one(1) is the week starting on the [WeekFields.getFirstDayOfWeek]
     * where there are at least [WeekFields.getMinimalDaysInFirstWeek] days in the year.
     * If the first week starts after the start of the year then the period before
     * is in the last week of the previous year.
     *
     *
     * For example:<br></br>
     * - if the 1st day of the year is a Monday, week one starts on the 1st<br></br>
     * - if the 2nd day of the year is a Monday, week one starts on the 2nd and
     * the 1st is in the last week of the previous year<br></br>
     * - if the 4th day of the year is a Monday, week one starts on the 4th and
     * the 1st to 3rd is in the last week of the previous year<br></br>
     * - if the 5th day of the year is a Monday, week two starts on the 5th and
     * the 1st to 4th is in week one<br></br>
     *
     *
     * This field can be used with any calendar system.
     *
     *
     * In the resolving phase of parsing, a date can be created from a week-based-year,
     * week-of-year and day-of-week.
     *
     *
     * In [strict mode][ResolverStyle.STRICT], all three fields are
     * validated against their range of valid values. The week-of-year field
     * is validated to ensure that the resulting week-based-year is the
     * week-based-year requested.
     *
     *
     * In [smart mode][ResolverStyle.SMART], all three fields are
     * validated against their range of valid values. The week-of-week-based-year field
     * is validated from 1 to 53, meaning that the resulting date can be in the
     * following week-based-year to that specified.
     *
     *
     * In [lenient mode][ResolverStyle.LENIENT], the year and day-of-week
     * are validated against the range of valid values. The resulting date is calculated
     * equivalent to the following three stage approach.
     * First, create a date on the first day of the first week in the requested week-based-year.
     * Then take the week-of-week-based-year, subtract one, and add the amount in weeks to the date.
     * Finally, adjust to the correct day-of-week within the localized week.
     *
     * @return a field providing access to the week-of-week-based-year, not null
     */
    fun weekOfWeekBasedYear(): TemporalField {
        return weekOfWeekBasedYear
    }

    /**
     * Returns a field to access the year of a week-based-year based on this `WeekFields`.
     *
     *
     * This represents the concept of the year where weeks start on a fixed day-of-week,
     * such as Monday and each week belongs to exactly one year.
     * This field is typically used with [WeekFields.dayOfWeek] and
     * [WeekFields.weekOfWeekBasedYear].
     *
     *
     * Week one(1) is the week starting on the [WeekFields.getFirstDayOfWeek]
     * where there are at least [WeekFields.getMinimalDaysInFirstWeek] days in the year.
     * Thus, week one may start before the start of the year.
     * If the first week starts after the start of the year then the period before
     * is in the last week of the previous year.
     *
     *
     * This field can be used with any calendar system.
     *
     *
     * In the resolving phase of parsing, a date can be created from a week-based-year,
     * week-of-year and day-of-week.
     *
     *
     * In [strict mode][ResolverStyle.STRICT], all three fields are
     * validated against their range of valid values. The week-of-year field
     * is validated to ensure that the resulting week-based-year is the
     * week-based-year requested.
     *
     *
     * In [smart mode][ResolverStyle.SMART], all three fields are
     * validated against their range of valid values. The week-of-week-based-year field
     * is validated from 1 to 53, meaning that the resulting date can be in the
     * following week-based-year to that specified.
     *
     *
     * In [lenient mode][ResolverStyle.LENIENT], the year and day-of-week
     * are validated against the range of valid values. The resulting date is calculated
     * equivalent to the following three stage approach.
     * First, create a date on the first day of the first week in the requested week-based-year.
     * Then take the week-of-week-based-year, subtract one, and add the amount in weeks to the date.
     * Finally, adjust to the correct day-of-week within the localized week.
     *
     * @return a field providing access to the week-based-year, not null
     */
    fun weekBasedYear(): TemporalField {
        return weekBasedYear
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this `WeekFields` is equal to the specified object.
     *
     *
     * The comparison is based on the entire state of the rules, which is
     * the first day-of-week and minimal days.
     *
     * @param other  the other rules to compare to, null returns false
     * @return true if this is equal to the specified rules
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other is WeekFields) hashCode() == other.hashCode()
                else false
    }

    /**
     * A hash code for this `WeekFields`.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return this.firstDayOfWeek.ordinal * 7 + this.minimalDaysInFirstWeek
    }

    //-----------------------------------------------------------------------
    /**
     * A string representation of this `WeekFields` instance.
     *
     * @return the string representation, not null
     */
    override fun toString(): String {
        return "WeekFields[$firstDayOfWeek,$minimalDaysInFirstWeek]"
    }
}