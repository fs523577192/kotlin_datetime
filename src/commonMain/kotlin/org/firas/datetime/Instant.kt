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

import org.firas.datetime.LocalTime.Companion.NANOS_PER_SECOND
import org.firas.datetime.LocalTime.Companion.SECONDS_PER_DAY
import org.firas.datetime.LocalTime.Companion.SECONDS_PER_HOUR
import org.firas.datetime.LocalTime.Companion.SECONDS_PER_MINUTE
import org.firas.datetime.temporal.*
import org.firas.datetime.util.MathUtils
import org.firas.lang.getName

/**
 * An instantaneous point on the time-line.
 *
 *
 * This class models a single instantaneous point on the time-line.
 * This might be used to record event time-stamps in the application.
 *
 *
 * The range of an instant requires the storage of a number larger than a `long`.
 * To achieve this, the class stores a `long` representing epoch-seconds and an
 * `int` representing nanosecond-of-second, which will always be between 0 and 999,999,999.
 * The epoch-seconds are measured from the standard Java epoch of `1970-01-01T00:00:00Z`
 * where instants after the epoch have positive values, and earlier instants have negative values.
 * For both the epoch-second and nanosecond parts, a larger value is always later on the time-line
 * than a smaller value.
 *
 * <h3>Time-scale</h3>
 *
 *
 * The length of the solar day is the standard way that humans measure time.
 * This has traditionally been subdivided into 24 hours of 60 minutes of 60 seconds,
 * forming a 86400 second day.
 *
 *
 * Modern timekeeping is based on atomic clocks which precisely define an SI second
 * relative to the transitions of a Caesium atom. The length of an SI second was defined
 * to be very close to the 86400th fraction of a day.
 *
 *
 * Unfortunately, as the Earth rotates the length of the day varies.
 * In addition, over time the average length of the day is getting longer as the Earth slows.
 * As a result, the length of a solar day in 2012 is slightly longer than 86400 SI seconds.
 * The actual length of any given day and the amount by which the Earth is slowing
 * are not predictable and can only be determined by measurement.
 * The UT1 time-scale captures the accurate length of day, but is only available some
 * time after the day has completed.
 *
 *
 * The UTC time-scale is a standard approach to bundle up all the additional fractions
 * of a second from UT1 into whole seconds, known as <i>leap-seconds</i>.
 * A leap-second may be added or removed depending on the Earth's rotational changes.
 * As such, UTC permits a day to have 86399 SI seconds or 86401 SI seconds where
 * necessary in order to keep the day aligned with the Sun.
 *
 *
 * The modern UTC time-scale was introduced in 1972, introducing the concept of whole leap-seconds.
 * Between 1958 and 1972, the definition of UTC was complex, with minor sub-second leaps and
 * alterations to the length of the notional second. As of 2012, discussions are underway
 * to change the definition of UTC again, with the potential to remove leap seconds or
 * introduce other changes.
 *
 *
 * Given the complexity of accurate timekeeping described above, this Java API defines
 * its own time-scale, the <i>Java Time-Scale</i>.
 *
 *
 * The Java Time-Scale divides each calendar day into exactly 86400
 * subdivisions, known as seconds.  These seconds may differ from the
 * SI second.  It closely matches the de facto international civil time
 * scale, the definition of which changes from time to time.
 *
 *
 * The Java Time-Scale has slightly different definitions for different
 * segments of the time-line, each based on the consensus international
 * time scale that is used as the basis for civil time. Whenever the
 * internationally-agreed time scale is modified or replaced, a new
 * segment of the Java Time-Scale must be defined for it.  Each segment
 * must meet these requirements:
 * <ul>
 * <li>the Java Time-Scale shall closely match the underlying international
 *  civil time scale;</li>
 * <li>the Java Time-Scale shall exactly match the international civil
 *  time scale at noon each day;</li>
 * <li>the Java Time-Scale shall have a precisely-defined relationship to
 *  the international civil time scale.</li>
 * </ul>
 * There are currently, as of 2013, two segments in the Java time-scale.
 *
 *
 * For the segment from 1972-11-03 (exact boundary discussed below) until
 * further notice, the consensus international time scale is UTC (with
 * leap seconds).  In this segment, the Java Time-Scale is identical to
 * <a href="http://www.cl.cam.ac.uk/~mgk25/time/utc-sls/">UTC-SLS</a>.
 * This is identical to UTC on days that do not have a leap second.
 * On days that do have a leap second, the leap second is spread equally
 * over the last 1000 seconds of the day, maintaining the appearance of
 * exactly 86400 seconds per day.
 *
 *
 * For the segment prior to 1972-11-03, extending back arbitrarily far,
 * the consensus international time scale is defined to be UT1, applied
 * proleptically, which is equivalent to the (mean) solar time on the
 * prime meridian (Greenwich). In this segment, the Java Time-Scale is
 * identical to the consensus international time scale. The exact
 * boundary between the two segments is the instant where UT1 = UTC
 * between 1972-11-03T00:00 and 1972-11-04T12:00.
 *
 *
 * Implementations of the Java time-scale using the JSR-310 API are not
 * required to provide any clock that is sub-second accurate, or that
 * progresses monotonically or smoothly. Implementations are therefore
 * not required to actually perform the UTC-SLS slew or to otherwise be
 * aware of leap seconds. JSR-310 does, however, require that
 * implementations must document the approach they use when defining a
 * clock representing the current instant.
 * See {@link Clock} for details on the available clocks.
 *
 *
 * The Java time-scale is used for all date-time classes.
 * This includes `Instant`, `LocalDate`, `LocalTime`, `OffsetDateTime`,
 * `ZonedDateTime` and `Duration`.
 *
 *
 *
 * This is a <a href="{@docRoot}/java.base/java/lang/doc-files/ValueBased.html">value-based</a>
 * class; use of identity-sensitive operations (including reference equality
 * (`==`), identity hash code, or synchronization) on instances of
 * `Instant` may have unpredictable results and should be avoided.
 * The `equals` method should be used for comparisons.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class Instant(val epochSecond: Long, val nanos: Int): Temporal, TemporalAdjuster, Comparable<Instant> {

    companion object {
        /**
         * Constant for the 1970-01-01T00:00:00Z epoch instant.
         */
        val EPOCH = Instant(0, 0)
        /**
         * The minimum supported epoch second.
         */
        private const val MIN_SECOND = -31557014167219200L
        /**
         * The maximum supported epoch second.
         */
        private const val MAX_SECOND = 31556889864403199L
        /**
         * The minimum supported `Instant`, '-1000000000-01-01T00:00Z'.
         * This could be used by an application as a "far past" instant.
         *
         *
         * This is one year earlier than the minimum `LocalDateTime`.
         * This provides sufficient values to handle the range of `ZoneOffset`
         * which affect the instant in addition to the local date-time.
         * The value is also chosen such that the value of the year fits in
         * an `int`.
         */
        val MIN = Instant.ofEpochSecond(MIN_SECOND, 0)
        /**
         * The maximum supported `Instant`, '1000000000-12-31T23:59:59.999999999Z'.
         * This could be used by an application as a "far future" instant.
         *
         *
         * This is one year later than the maximum `LocalDateTime`.
         * This provides sufficient values to handle the range of `ZoneOffset`
         * which affect the instant in addition to the local date-time.
         * The value is also chosen such that the value of the year fits in
         * an `int`.
         */
        val MAX = Instant.ofEpochSecond(MAX_SECOND, 999999999)

        /**
         * Serialization version.
         */
        private const val serialVersionUID = -665713676816604388L

        /**
         * This method is not the same as the one in OpenJDK.
         * It uses `Date` instead of `Clock` and therefore
         * the precision of the result is one millisecond
         * instead of one nanosecond
         *
         * @author Wu Yuping
         */
        fun now(): Instant {
            val timestamp = Date().getTime()
            return ofEpochSecond(timestamp / 1000,
                    timestamp % 1000 * 1000000)
        }

        /**
         * Obtains an instance of `Instant` using seconds from the
         * epoch of 1970-01-01T00:00:00Z.
         *
         *
         * The nanosecond field is set to zero.
         *
         * @param epochSecond  the number of seconds from 1970-01-01T00:00:00Z
         * @return an instant, not null
         * @throws DateTimeException if the instant exceeds the maximum or minimum instant
         */
        fun ofEpochSecond(epochSecond: Long): Instant {
            return create(epochSecond, 0)
        }

        /**
         * Obtains an instance of `Instant` using seconds from the
         * epoch of 1970-01-01T00:00:00Z and nanosecond fraction of second.
         *
         *
         * This method allows an arbitrary number of nanoseconds to be passed in.
         * The factory will alter the values of the second and nanosecond in order
         * to ensure that the stored nanosecond is in the range 0 to 999,999,999.
         * For example, the following will result in exactly the same instant:
         * <pre>
         * Instant.ofEpochSecond(3, 1);
         * Instant.ofEpochSecond(4, -999_999_999);
         * Instant.ofEpochSecond(2, 1000_000_001);
         * </pre>
         *
         * @param epochSecond  the number of seconds from 1970-01-01T00:00:00Z
         * @param nanoAdjustment  the nanosecond adjustment to the number of seconds, positive or negative
         * @return an instant, not null
         * @throws DateTimeException if the instant exceeds the maximum or minimum instant
         * @throws ArithmeticException if numeric overflow occurs
         */
        fun ofEpochSecond(epochSecond: Long, nanoAdjustment: Long): Instant {
            val secs = MathUtils.addExact(epochSecond, MathUtils.floorDiv(
                    nanoAdjustment, LocalTime.NANOS_PER_SECOND))
            val nos = MathUtils.floorMod(nanoAdjustment, LocalTime.NANOS_PER_SECOND).toInt()
            return create(secs, nos)
        }

        /**
         * Obtains an instance of `Instant` using milliseconds from the
         * epoch of 1970-01-01T00:00:00Z.
         *
         *
         * The seconds and nanoseconds are extracted from the specified milliseconds.
         *
         * @param epochMilli  the number of milliseconds from 1970-01-01T00:00:00Z
         * @return an instant, not null
         * @throws DateTimeException if the instant exceeds the maximum or minimum instant
         */
        fun ofEpochMilli(epochMilli: Long): Instant {
            val secs = MathUtils.floorDiv(epochMilli, 1000)
            val mos = MathUtils.floorMod(epochMilli, 1000)
            return create(secs, mos.toInt() * 1000000)
        }

        /**
         * Obtains an instance of `Instant` from a temporal object.
         *
         *
         * This obtains an instant based on the specified temporal.
         * A `TemporalAccessor` represents an arbitrary set of date and time information,
         * which this factory converts to an instance of `Instant`.
         *
         *
         * The conversion extracts the {@link ChronoField#INSTANT_SECONDS INSTANT_SECONDS}
         * and {@link ChronoField#NANO_OF_SECOND NANO_OF_SECOND} fields.
         *
         *
         * This method matches the signature of the functional interface [TemporalQuery]
         * allowing it to be used as a query via method reference, `Instant::from`.
         *
         * @param temporal  the temporal object to convert, not null
         * @return the instant, not null
         * @throws DateTimeException if unable to convert to an `Instant`
         */
        fun from(temporal: TemporalAccessor): Instant {
            if (temporal is Instant) {
                return temporal
            }
            try {
                val instantSecs = temporal.getLong(ChronoField.INSTANT_SECONDS)
                val nanoOfSecond = temporal.get(ChronoField.NANO_OF_SECOND)
                return Instant.ofEpochSecond(instantSecs, nanoOfSecond.toLong())
            } catch (ex: DateTimeException) {
                throw DateTimeException ("Unable to obtain Instant from TemporalAccessor: " +
                        temporal + " of type " + temporal::class.getName(), ex)
            }
        }

        /**
         * Obtains an instance of `Instant` using seconds and nanoseconds.
         *
         * @param seconds  the length of the duration in seconds
         * @param nanoOfSecond  the nano-of-second, from 0 to 999,999,999
         * @throws DateTimeException if the instant exceeds the maximum or minimum instant
         */
        private fun create(seconds: Long, nanoOfSecond: Int): Instant {
            if (seconds or nanoOfSecond.toLong() == 0L) {
                return EPOCH
            }
            if (seconds < MIN_SECOND || seconds > MAX_SECOND) {
                throw DateTimeException("Instant exceeds minimum or maximum instant")
            }
            return Instant(seconds, nanoOfSecond)
        }
    } // companion object

    /**
     * Checks if the specified field is supported.
     *
     *
     * This checks if this instant can be queried for the specified field.
     * If false, then calling the [range][.range],
     * [get][.get] and [.with]
     * methods will throw an exception.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The supported fields are:
     *
     *  * `NANO_OF_SECOND`
     *  * `MICRO_OF_SECOND`
     *  * `MILLI_OF_SECOND`
     *  * `INSTANT_SECONDS`
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
     * @return true if the field is supported on this instant, false if not
     */
    override fun isSupported(field: TemporalField): Boolean {
        return if (field is ChronoField) {
            field == ChronoField.INSTANT_SECONDS || field == ChronoField.NANO_OF_SECOND
                    || field == ChronoField.MICRO_OF_SECOND || field == ChronoField.MILLI_OF_SECOND
        } else field.isSupportedBy(this)
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
            unit.isTimeBased() || unit == ChronoUnit.DAYS
        } else unit.isSupportedBy(this)
    }

    /**
     * Gets the value of the specified field from this instant as an `int`.
     *
     *
     * This queries this instant for the value of the specified field.
     * The returned value will always be within the valid range of values for the field.
     * If it is not possible to return the value, because the field is not supported
     * or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the query is implemented here.
     * The [supported fields][.isSupported] will return valid
     * values based on this date-time, except `INSTANT_SECONDS` which is too
     * large to fit in an `int` and throws a `DateTimeException`.
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
     * @throws DateTimeException if a value for the field cannot be obtained or
     * the value is outside the range of valid values for the field
     * @throws UnsupportedTemporalTypeException if the field is not supported or
     * the range of values exceeds an `int`
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun get(field: TemporalField): Int {
        if (field is ChronoField) {
            return when (field) {
                ChronoField.NANO_OF_SECOND -> this.nanos
                ChronoField.MICRO_OF_SECOND -> this.nanos / 1000
                ChronoField.MILLI_OF_SECOND -> this.nanos / 1000000
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
        }
        return range(field).checkValidIntValue(field.getFrom(this), field)
    }

    /**
     * Gets the value of the specified field from this instant as a `long`.
     *
     *
     * This queries this instant for the value of the specified field.
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
                ChronoField.NANO_OF_SECOND -> this.nanos.toLong()
                ChronoField.MICRO_OF_SECOND -> nanos / 1000L
                ChronoField.MILLI_OF_SECOND -> nanos / 1000000L
                ChronoField.INSTANT_SECONDS -> this.epochSecond
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
        }
        return field.getFrom(this)
    }

    /**
     * Returns an adjusted copy of this instant.
     *
     *
     * This returns an `Instant`, based on this one, with the instant adjusted.
     * The adjustment takes place using the specified adjuster strategy object.
     * Read the documentation of the adjuster to understand what adjustment will be made.
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
     * @return an `Instant` based on `this` with the adjustment made, not null
     * @throws DateTimeException if the adjustment cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(adjuster: TemporalAdjuster): Instant {
        return adjuster.adjustInto(this) as Instant
    }

    /**
     * Returns a copy of this instant with the specified field set to a new value.
     *
     *
     * This returns an `Instant`, based on this one, with the value
     * for the specified field changed.
     * If it is not possible to set the value, because the field is not supported or for
     * some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoField] then the adjustment is implemented here.
     * The supported fields behave as follows:
     *
     *  * `NANO_OF_SECOND` -
     * Returns an `Instant` with the specified nano-of-second.
     * The epoch-second will be unchanged.
     *  * `MICRO_OF_SECOND` -
     * Returns an `Instant` with the nano-of-second replaced by the specified
     * micro-of-second multiplied by 1,000. The epoch-second will be unchanged.
     *  * `MILLI_OF_SECOND` -
     * Returns an `Instant` with the nano-of-second replaced by the specified
     * milli-of-second multiplied by 1,000,000. The epoch-second will be unchanged.
     *  * `INSTANT_SECONDS` -
     * Returns an `Instant` with the specified epoch-second.
     * The nano-of-second will be unchanged.
     *
     *
     *
     * In all cases, if the new value is outside the valid range of values for the field
     * then a `DateTimeException` will be thrown.
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
     * @return an `Instant` based on `this` with the specified field set, not null
     * @throws DateTimeException if the field cannot be set
     * @throws UnsupportedTemporalTypeException if the field is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun with(field: TemporalField, newValue: Long): Instant {
        if (field is ChronoField) {
            field.checkValidValue(newValue)
            return when (field) {
                ChronoField.MILLI_OF_SECOND -> {
                    val nval = newValue.toInt() * 1000000
                    if (nval != nanos) create(this.epochSecond, nval) else this
                }
                ChronoField.MICRO_OF_SECOND -> {
                    val nval = newValue.toInt() * 1000
                    if (nval != nanos) create(this.epochSecond, nval) else this
                }
                ChronoField.NANO_OF_SECOND -> if (newValue != this.nanos.toLong())
                        create(this.epochSecond, newValue.toInt()) else this
                ChronoField.INSTANT_SECONDS -> if (newValue != this.epochSecond)
                        create(newValue, nanos) else this
                else -> throw UnsupportedTemporalTypeException("Unsupported field: $field")
            }
        }
        return field.adjustInto(this, newValue)
    }

    /**
     * Converts this instant to the number of milliseconds from the epoch
     * of 1970-01-01T00:00:00Z.
     *
     *
     * If this instant represents a point on the time-line too far in the future
     * or past to fit in a `long` milliseconds, then an exception is thrown.
     *
     *
     * If this instant has greater than millisecond precision, then the conversion
     * will drop any excess precision information as though the amount in nanoseconds
     * was subject to integer division by one million.
     *
     * @return the number of milliseconds since the epoch of 1970-01-01T00:00:00Z
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun toEpochMilli(): Long {
        return if (this.epochSecond < 0 && this.nanos > 0) {
            val millis = MathUtils.multiplyExact(this.epochSecond + 1, 1000)
            val adjustment = this.nanos / 1000000 - 1000
            MathUtils.addExact(millis, adjustment.toLong())
        } else {
            val millis = MathUtils.multiplyExact(this.epochSecond, 1000)
            MathUtils.addExact(millis, nanos.toLong() / 1000000)
        }
    }

    // ----==== Comparison ====----
    /**
     * Compares this instant to the specified instant.
     *
     *
     * The comparison is based on the time-line position of the instants.
     * It is "consistent with equals", as defined by [Comparable].
     *
     * @param other  the other instant to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     * @throws NullPointerException if otherInstant is null
     */
    override fun compareTo(other: Instant): Int {
        return if (this.epochSecond > other.epochSecond) {
            1
        } else if (this.epochSecond < other.epochSecond) {
            -1
        } else {
            nanos - other.nanos
        }
    }

    /**
     * Checks if this instant is after the specified instant.
     *
     *
     * The comparison is based on the time-line position of the instants.
     *
     * @param otherInstant  the other instant to compare to, not null
     * @return true if this instant is after the specified instant
     * @throws NullPointerException if otherInstant is null
     */
    fun isAfter(otherInstant: Instant): Boolean {
        return compareTo(otherInstant) > 0
    }

    /**
     * Checks if this instant is before the specified instant.
     *
     *
     * The comparison is based on the time-line position of the instants.
     *
     * @param otherInstant  the other instant to compare to, not null
     * @return true if this instant is before the specified instant
     * @throws NullPointerException if otherInstant is null
     */
    fun isBefore(otherInstant: Instant): Boolean {
        return compareTo(otherInstant) < 0
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this instant is equal to the specified instant.
     *
     *
     * The comparison is based on the time-line position of the instants.
     *
     * @param otherInstant  the other instant, null returns false
     * @return true if the other instant is equal to this one
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is Instant) {
            return this.epochSecond == other.epochSecond && this.nanos == other.nanos
        }
        return false
    }

    /**
     * Returns a hash code for this instant.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return (epochSecond xor epochSecond.ushr(32)).toInt() + 51 * nanos
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this instant with the specified amount added.
     *
     *
     * This returns an `Instant`, based on this one, with the specified amount added.
     * The amount is typically [Duration] but may be any other type implementing
     * the [TemporalAmount] interface.
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
     * @return an `Instant` based on this instant with the addition made, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws ArithmeticException if numeric overflow occurs
     */
    override operator fun plus(amountToAdd: TemporalAmount): Instant {
        return amountToAdd.addTo(this) as Instant
    }

    /**
     * Returns a copy of this instant with the specified amount added.
     *
     *
     * This returns an `Instant`, based on this one, with the amount
     * in terms of the unit added. If it is not possible to add the amount, because the
     * unit is not supported or for some other reason, an exception is thrown.
     *
     *
     * If the field is a [ChronoUnit] then the addition is implemented here.
     * The supported fields behave as follows:
     *
     *  * `NANOS` -
     * Returns an `Instant` with the specified number of nanoseconds added.
     * This is equivalent to [.plusNanos].
     *  * `MICROS` -
     * Returns an `Instant` with the specified number of microseconds added.
     * This is equivalent to [.plusNanos] with the amount
     * multiplied by 1,000.
     *  * `MILLIS` -
     * Returns an `Instant` with the specified number of milliseconds added.
     * This is equivalent to [.plusNanos] with the amount
     * multiplied by 1,000,000.
     *  * `SECONDS` -
     * Returns an `Instant` with the specified number of seconds added.
     * This is equivalent to [.plusSeconds].
     *  * `MINUTES` -
     * Returns an `Instant` with the specified number of minutes added.
     * This is equivalent to [.plusSeconds] with the amount
     * multiplied by 60.
     *  * `HOURS` -
     * Returns an `Instant` with the specified number of hours added.
     * This is equivalent to [.plusSeconds] with the amount
     * multiplied by 3,600.
     *  * `HALF_DAYS` -
     * Returns an `Instant` with the specified number of half-days added.
     * This is equivalent to [.plusSeconds] with the amount
     * multiplied by 43,200 (12 hours).
     *  * `DAYS` -
     * Returns an `Instant` with the specified number of days added.
     * This is equivalent to [.plusSeconds] with the amount
     * multiplied by 86,400 (24 hours).
     *
     *
     *
     * All other `ChronoUnit` instances will throw an `UnsupportedTemporalTypeException`.
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
     * @return an `Instant` based on this instant with the specified amount added, not null
     * @throws DateTimeException if the addition cannot be made
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun plus(amountToAdd: Long, unit: TemporalUnit): Instant {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.NANOS -> plusNanos(amountToAdd)
                ChronoUnit.MICROS -> plus(amountToAdd / 1000000, amountToAdd % 1000000 * 1000)
                ChronoUnit.MILLIS -> plusMillis(amountToAdd)
                ChronoUnit.SECONDS -> plusSeconds(amountToAdd)
                ChronoUnit.MINUTES -> plusSeconds(MathUtils.multiplyExact(amountToAdd, SECONDS_PER_MINUTE.toLong()))
                ChronoUnit.HOURS -> plusSeconds(MathUtils.multiplyExact(amountToAdd, SECONDS_PER_HOUR.toLong()))
                ChronoUnit.HALF_DAYS -> plusSeconds(MathUtils.multiplyExact(amountToAdd, SECONDS_PER_DAY / 2L))
                ChronoUnit.DAYS -> plusSeconds(MathUtils.multiplyExact(amountToAdd, SECONDS_PER_DAY.toLong()))
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.addTo(this, amountToAdd)
    }

    /**
     * Returns a copy of this instant with the specified duration in seconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondsToAdd  the seconds to add, positive or negative
     * @return an `Instant` based on this instant with the specified seconds added, not null
     * @throws DateTimeException if the result exceeds the maximum or minimum instant
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusSeconds(secondsToAdd: Long): Instant {
        return plus(secondsToAdd, 0)
    }

    /**
     * Returns a copy of this instant with the specified duration in milliseconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param millisToAdd  the milliseconds to add, positive or negative
     * @return an `Instant` based on this instant with the specified milliseconds added, not null
     * @throws DateTimeException if the result exceeds the maximum or minimum instant
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusMillis(millisToAdd: Long): Instant {
        return plus(millisToAdd / 1000, millisToAdd % 1000 * 1000000)
    }

    /**
     * Returns a copy of this instant with the specified duration in nanoseconds added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanosToAdd  the nanoseconds to add, positive or negative
     * @return an `Instant` based on this instant with the specified nanoseconds added, not null
     * @throws DateTimeException if the result exceeds the maximum or minimum instant
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun plusNanos(nanosToAdd: Long): Instant {
        return plus(0, nanosToAdd)
    }

    /**
     * Returns a copy of this instant with the specified duration in seconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondsToSubtract  the seconds to subtract, positive or negative
     * @return an `Instant` based on this instant with the specified seconds subtracted, not null
     * @throws DateTimeException if the result exceeds the maximum or minimum instant
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusSeconds(secondsToSubtract: Long): Instant {
        return if (secondsToSubtract == Long.MIN_VALUE) {
            plusSeconds(Long.MAX_VALUE).plusSeconds(1)
        } else plusSeconds(-secondsToSubtract)
    }

    /**
     * Returns a copy of this instant with the specified duration in milliseconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param millisToSubtract  the milliseconds to subtract, positive or negative
     * @return an `Instant` based on this instant with the specified milliseconds subtracted, not null
     * @throws DateTimeException if the result exceeds the maximum or minimum instant
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusMillis(millisToSubtract: Long): Instant {
        return if (millisToSubtract == Long.MIN_VALUE) {
            plusMillis(Long.MAX_VALUE).plusMillis(1)
        } else plusMillis(-millisToSubtract)
    }

    /**
     * Returns a copy of this instant with the specified duration in nanoseconds subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param nanosToSubtract  the nanoseconds to subtract, positive or negative
     * @return an `Instant` based on this instant with the specified nanoseconds subtracted, not null
     * @throws DateTimeException if the result exceeds the maximum or minimum instant
     * @throws ArithmeticException if numeric overflow occurs
     */
    fun minusNanos(nanosToSubtract: Long): Instant {
        return if (nanosToSubtract == Long.MIN_VALUE) {
            plusNanos(Long.MAX_VALUE).plusNanos(1)
        } else plusNanos(-nanosToSubtract)
    }

    /**
     * Adjusts the specified temporal object to have this instant.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with the instant changed to be the same as this.
     *
     *
     * The adjustment is equivalent to using [Temporal.with]
     * twice, passing [ChronoField.INSTANT_SECONDS] and
     * [ChronoField.NANO_OF_SECOND] as the fields.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * [Temporal.with]:
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * temporal = thisInstant.adjustInto(temporal);
     * temporal = temporal.with(thisInstant);
    </pre> *
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
        return temporal.with(ChronoField.INSTANT_SECONDS, this.epochSecond)
            .with(ChronoField.NANO_OF_SECOND, nanos.toLong())
    }

    /**
     * Calculates the amount of time until another instant in terms of the specified unit.
     *
     *
     * This calculates the amount of time between two `Instant`
     * objects in terms of a single `TemporalUnit`.
     * The start and end points are `this` and the specified instant.
     * The result will be negative if the end is before the start.
     * The calculation returns a whole number, representing the number of
     * complete units between the two instants.
     * The `Temporal` passed to this method is converted to a
     * `Instant` using [.from].
     * For example, the amount in seconds between two dates can be calculated
     * using `startInstant.until(endInstant, SECONDS)`.
     *
     *
     * There are two equivalent ways of using this method.
     * The first is to invoke this method.
     * The second is to use [TemporalUnit.between]:
     * <pre>
     * // these two lines are equivalent
     * amount = start.until(end, SECONDS);
     * amount = SECONDS.between(start, end);
     * </pre>
     * The choice should be made based on which makes the code more readable.
     *
     *
     * The calculation is implemented in this method for [ChronoUnit].
     * The units `NANOS`, `MICROS`, `MILLIS`, `SECONDS`,
     * `MINUTES`, `HOURS`, `HALF_DAYS` and `DAYS`
     * are supported. Other `ChronoUnit` values will throw an exception.
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
     * @param endExclusive  the end date, exclusive, which is converted to an `Instant`, not null
     * @param unit  the unit to measure the amount in, not null
     * @return the amount of time between this instant and the end instant
     * @throws DateTimeException if the amount cannot be calculated, or the end
     * temporal cannot be converted to an `Instant`
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = Instant.from(endExclusive)
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.NANOS -> nanosUntil(end)
                ChronoUnit.MICROS -> nanosUntil(end) / 1000
                ChronoUnit.MILLIS -> MathUtils.subtractExact(end.toEpochMilli(), toEpochMilli())
                ChronoUnit.SECONDS -> secondsUntil(end)
                ChronoUnit.MINUTES -> secondsUntil(end) / SECONDS_PER_MINUTE
                ChronoUnit.HOURS -> secondsUntil(end) / SECONDS_PER_HOUR
                ChronoUnit.HALF_DAYS -> secondsUntil(end) / (12 * SECONDS_PER_HOUR)
                ChronoUnit.DAYS -> secondsUntil(end) / SECONDS_PER_DAY
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.between(this, end)
    }

    /**
     * Returns a copy of this instant with the specified duration added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param secondsToAdd  the seconds to add, positive or negative
     * @param nanosToAdd  the nanos to add, positive or negative
     * @return an `Instant` based on this instant with the specified seconds added, not null
     * @throws DateTimeException if the result exceeds the maximum or minimum instant
     * @throws ArithmeticException if numeric overflow occurs
     */
    private fun plus(secondsToAdd: Long, nanosToAdd: Long): Instant {
        var nanosToAdd = nanosToAdd
        if (secondsToAdd or nanosToAdd == 0L) {
            return this
        }
        var epochSec = MathUtils.addExact(this.epochSecond, secondsToAdd)
        epochSec = MathUtils.addExact(epochSec, nanosToAdd / LocalTime.NANOS_PER_SECOND)
        nanosToAdd %= LocalTime.NANOS_PER_SECOND
        val nanoAdjustment = nanos + nanosToAdd  // safe int+NANOS_PER_SECOND
        return ofEpochSecond(epochSec, nanoAdjustment)
    }

    private fun nanosUntil(end: Instant): Long {
        val secsDiff = MathUtils.subtractExact(end.epochSecond, this.epochSecond)
        val totalNanos = MathUtils.multiplyExact(secsDiff, NANOS_PER_SECOND)
        return MathUtils.addExact(totalNanos, end.nanos.toLong() - this.nanos)
    }

    private fun secondsUntil(end: Instant): Long {
        var secsDiff = MathUtils.subtractExact(end.epochSecond, this.epochSecond)
        val nanosDiff = end.nanos - nanos
        if (secsDiff > 0 && nanosDiff < 0) {
            secsDiff -= 1
        } else if (secsDiff < 0 && nanosDiff > 0) {
            secsDiff += 1
        }
        return secsDiff
    }
}