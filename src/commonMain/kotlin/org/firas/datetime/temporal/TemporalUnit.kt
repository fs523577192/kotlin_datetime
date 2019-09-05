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
package org.firas.datetime.temporal

import org.firas.datetime.Duration
import org.firas.datetime.LocalTime
import org.firas.datetime.chrono.ChronoLocalDate
import org.firas.datetime.chrono.ChronoLocalDateTime
import org.firas.datetime.chrono.ChronoZonedDateTime
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * A unit of date-time, such as Days or Hours.
 *
 * Measurement of time is built on units, such as years, months, days, hours, minutes and seconds.
 * Implementations of this interface represent those units.
 *
 *
 * An instance of this interface represents the unit itself, rather than an amount of the unit.
 * See [Period] for a class that represents an amount in terms of the common units.
 *
 *
 * The most commonly used units are defined in [ChronoUnit].
 * Further units are supplied in {@link IsoFields}.
 * Units can also be written by application code by implementing this interface.
 *
 *
 * The unit works using double dispatch. Client code calls methods on a date-time like
 * `LocalDateTime` which check if the unit is a `ChronoUnit`.
 * If it is, then the date-time must handle it.
 * Otherwise, the method call is re-dispatched to the matching method in this interface.
 *
 * @implSpec
 * This interface must be implemented with care to ensure other classes operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * It is recommended to use an enum where possible.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
interface TemporalUnit {

    companion object {
        // Write default implementation here because
        // 1. the default implementation mechanism in Kotlin is different from
        //    the default implementation mechanism in Java;
        // 2. Kotlin does not support calling implementation of a base class
        //    when the derived class has an overridden implementation

        @JvmStatic
        @JsName("isSupportedBy")
        fun isSupportedBy(temporalUnit: TemporalUnit, temporal: Temporal): Boolean {
            if (temporal is LocalTime) {
                return temporalUnit.isTimeBased()
            }
            if (temporal is ChronoLocalDate) {
                return temporalUnit.isDateBased()
            }
            if (temporal is ChronoLocalDateTime<*> || temporal is ChronoZonedDateTime<*>) {
                return true
            }
            return try {
                temporal.plus(1, temporalUnit)
                true
            } catch (ex: UnsupportedTemporalTypeException) {
                false
            } catch (ex: RuntimeException) {
                try {
                    temporal.plus(-1, temporalUnit)
                    true
                } catch (ex2: RuntimeException) {
                    false
                }
            }
        }
    }

    /**
     * Gets the duration of this unit, which may be an estimate.
     *
     *
     * All units return a duration measured in standard nanoseconds from this method.
     * The duration will be positive and non-zero.
     * For example, an hour has a duration of `60 * 60 * 1,000,000,000ns`.
     *
     *
     * Some units may return an accurate duration while others return an estimate.
     * For example, days have an estimated duration due to the possibility of
     * daylight saving time changes.
     * To determine if the duration is an estimate, use [.isDurationEstimated].
     *
     * @return the duration of this unit, which may be an estimate, not null
     */
    @JsName("getDuration")
    fun getDuration(): Duration

    /**
     * Checks if the duration of the unit is an estimate.
     *
     *
     * All units have a duration, however the duration is not always accurate.
     * For example, days have an estimated duration due to the possibility of
     * daylight saving time changes.
     * This method returns true if the duration is an estimate and false if it is
     * accurate. Note that accurate/estimated ignores leap seconds.
     *
     * @return true if the duration is estimated, false if accurate
     */
    @JsName("isDurationEstimated")
    fun isDurationEstimated(): Boolean

    //-----------------------------------------------------------------------
    /**
     * Checks if this unit represents a component of a date.
     *
     *
     * A date is time-based if it can be used to imply meaning from a date.
     * It must have a [duration][.getDuration] that is an integral
     * multiple of the length of a standard day.
     * Note that it is valid for both `isDateBased()` and `isTimeBased()`
     * to return false, such as when representing a unit like 36 hours.
     *
     * @return true if this unit is a component of a date
     */
    @JsName("isDateBased")
    fun isDateBased(): Boolean

    /**
     * Checks if this unit represents a component of a time.
     *
     *
     * A unit is time-based if it can be used to imply meaning from a time.
     * It must have a [duration][.getDuration] that divides into
     * the length of a standard day without remainder.
     * Note that it is valid for both `isDateBased()` and `isTimeBased()`
     * to return false, such as when representing a unit like 36 hours.
     *
     * @return true if this unit is a component of a time
     */
    @JsName("isTimeBased")
    fun isTimeBased(): Boolean

    //-----------------------------------------------------------------------
    /**
     * Checks if this unit is supported by the specified temporal object.
     *
     *
     * This checks that the implementing date-time can add/subtract this unit.
     * This can be used to avoid throwing an exception.
     *
     *
     * This default implementation derives the value using
     * [Temporal.plus].
     *
     * @param temporal  the temporal object to check, not null
     * @return true if the unit is supported
     */
    @JsName("isSupportedBy")
    fun isSupportedBy(temporal: Temporal): Boolean

    /**
     * Returns a copy of the specified temporal object with the specified period added.
     *
     *
     * The period added is a multiple of this unit. For example, this method
     * could be used to add "3 days" to a date by calling this method on the
     * instance representing "days", passing the date and the period "3".
     * The period to be added may be negative, which is equivalent to subtraction.
     *
     *
     * There are two equivalent ways of using this method.
     * The first is to invoke this method directly.
     * The second is to use [Temporal.plus]:
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * temporal = thisUnit.addTo(temporal);
     * temporal = temporal.plus(thisUnit);
     * </pre>
     * It is recommended to use the second approach, `plus(TemporalUnit)`,
     * as it is a lot clearer to read in code.
     *
     *
     * Implementations should perform any queries or calculations using the units
     * available in [ChronoUnit] or the fields available in [ChronoField].
     * If the unit is not supported an `UnsupportedTemporalTypeException` must be thrown.
     *
     *
     * Implementations must not alter the specified temporal object.
     * Instead, an adjusted copy of the original must be returned.
     * This provides equivalent, safe behavior for immutable and mutable implementations.
     *
     * @param <R>  the type of the Temporal object
     * @param temporal  the temporal object to adjust, not null
     * @param amount  the amount of this unit to add, positive or negative
     * @return the adjusted temporal object, not null
     * @throws DateTimeException if the amount cannot be added
     * @throws UnsupportedTemporalTypeException if the unit is not supported by the temporal
     */
    @JsName("addTo")
    fun <R: Temporal> addTo(temporal: R, amount: Long): R

    //-----------------------------------------------------------------------
    /**
     * Calculates the amount of time between two temporal objects.
     *
     *
     * This calculates the amount in terms of this unit. The start and end
     * points are supplied as temporal objects and must be of compatible types.
     * The implementation will convert the second type to be an instance of the
     * first type before the calculating the amount.
     * The result will be negative if the end is before the start.
     * For example, the amount in hours between two temporal objects can be
     * calculated using `HOURS.between(startTime, endTime)`.
     *
     *
     * The calculation returns a whole number, representing the number of
     * complete units between the two temporals.
     * For example, the amount in hours between the times 11:30 and 13:29
     * will only be one hour as it is one minute short of two hours.
     *
     *
     * There are two equivalent ways of using this method.
     * The first is to invoke this method directly.
     * The second is to use [Temporal.until]:
     * <pre>
     * // these two lines are equivalent
     * between = thisUnit.between(start, end);
     * between = start.until(end, thisUnit);
     * </pre>
     * The choice should be made based on which makes the code more readable.
     *
     *
     * For example, this method allows the number of days between two dates to
     * be calculated:
     * <pre>
     * long daysBetween = DAYS.between(start, end);
     * // or alternatively
     * long daysBetween = start.until(end, DAYS);
     * </pre>
     *
     *
     * Implementations should perform any queries or calculations using the units
     * available in [ChronoUnit] or the fields available in [ChronoField].
     * If the unit is not supported an `UnsupportedTemporalTypeException` must be thrown.
     * Implementations must not alter the specified temporal objects.
     *
     * @implSpec
     * Implementations must begin by checking to if the two temporals have the
     * same type using `getClass()`. If they do not, then the result must be
     * obtained by calling `temporal1Inclusive.until(temporal2Exclusive, this)`.
     *
     * @param temporal1Inclusive  the base temporal object, not null
     * @param temporal2Exclusive  the other temporal object, exclusive, not null
     * @return the amount of time between temporal1Inclusive and temporal2Exclusive
     * in terms of this unit; positive if temporal2Exclusive is later than
     * temporal1Inclusive, negative if earlier
     * @throws DateTimeException if the amount cannot be calculated, or the end
     * temporal cannot be converted to the same type as the start temporal
     * @throws UnsupportedTemporalTypeException if the unit is not supported by the temporal
     * @throws ArithmeticException if numeric overflow occurs
     */
    @JsName("between")
    fun between(temporal1Inclusive: Temporal, temporal2Exclusive: Temporal): Long
}