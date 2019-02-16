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

import org.firas.datetime.util.MathUtils

/**
 * An instantaneous point on the time-line.
 * <p>
 * This class models a single instantaneous point on the time-line.
 * This might be used to record event time-stamps in the application.
 * <p>
 * The range of an instant requires the storage of a number larger than a `long`.
 * To achieve this, the class stores a `long` representing epoch-seconds and an
 * `int` representing nanosecond-of-second, which will always be between 0 and 999,999,999.
 * The epoch-seconds are measured from the standard Java epoch of `1970-01-01T00:00:00Z`
 * where instants after the epoch have positive values, and earlier instants have negative values.
 * For both the epoch-second and nanosecond parts, a larger value is always later on the time-line
 * than a smaller value.
 *
 * <h3>Time-scale</h3>
 * <p>
 * The length of the solar day is the standard way that humans measure time.
 * This has traditionally been subdivided into 24 hours of 60 minutes of 60 seconds,
 * forming a 86400 second day.
 * <p>
 * Modern timekeeping is based on atomic clocks which precisely define an SI second
 * relative to the transitions of a Caesium atom. The length of an SI second was defined
 * to be very close to the 86400th fraction of a day.
 * <p>
 * Unfortunately, as the Earth rotates the length of the day varies.
 * In addition, over time the average length of the day is getting longer as the Earth slows.
 * As a result, the length of a solar day in 2012 is slightly longer than 86400 SI seconds.
 * The actual length of any given day and the amount by which the Earth is slowing
 * are not predictable and can only be determined by measurement.
 * The UT1 time-scale captures the accurate length of day, but is only available some
 * time after the day has completed.
 * <p>
 * The UTC time-scale is a standard approach to bundle up all the additional fractions
 * of a second from UT1 into whole seconds, known as <i>leap-seconds</i>.
 * A leap-second may be added or removed depending on the Earth's rotational changes.
 * As such, UTC permits a day to have 86399 SI seconds or 86401 SI seconds where
 * necessary in order to keep the day aligned with the Sun.
 * <p>
 * The modern UTC time-scale was introduced in 1972, introducing the concept of whole leap-seconds.
 * Between 1958 and 1972, the definition of UTC was complex, with minor sub-second leaps and
 * alterations to the length of the notional second. As of 2012, discussions are underway
 * to change the definition of UTC again, with the potential to remove leap seconds or
 * introduce other changes.
 * <p>
 * Given the complexity of accurate timekeeping described above, this Java API defines
 * its own time-scale, the <i>Java Time-Scale</i>.
 * <p>
 * The Java Time-Scale divides each calendar day into exactly 86400
 * subdivisions, known as seconds.  These seconds may differ from the
 * SI second.  It closely matches the de facto international civil time
 * scale, the definition of which changes from time to time.
 * <p>
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
 * <p>
 * For the segment from 1972-11-03 (exact boundary discussed below) until
 * further notice, the consensus international time scale is UTC (with
 * leap seconds).  In this segment, the Java Time-Scale is identical to
 * <a href="http://www.cl.cam.ac.uk/~mgk25/time/utc-sls/">UTC-SLS</a>.
 * This is identical to UTC on days that do not have a leap second.
 * On days that do have a leap second, the leap second is spread equally
 * over the last 1000 seconds of the day, maintaining the appearance of
 * exactly 86400 seconds per day.
 * <p>
 * For the segment prior to 1972-11-03, extending back arbitrarily far,
 * the consensus international time scale is defined to be UT1, applied
 * proleptically, which is equivalent to the (mean) solar time on the
 * prime meridian (Greenwich). In this segment, the Java Time-Scale is
 * identical to the consensus international time scale. The exact
 * boundary between the two segments is the instant where UT1 = UTC
 * between 1972-11-03T00:00 and 1972-11-04T12:00.
 * <p>
 * Implementations of the Java time-scale using the JSR-310 API are not
 * required to provide any clock that is sub-second accurate, or that
 * progresses monotonically or smoothly. Implementations are therefore
 * not required to actually perform the UTC-SLS slew or to otherwise be
 * aware of leap seconds. JSR-310 does, however, require that
 * implementations must document the approach they use when defining a
 * clock representing the current instant.
 * See {@link Clock} for details on the available clocks.
 * <p>
 * The Java time-scale is used for all date-time classes.
 * This includes `Instant`, `LocalDate`, `LocalTime`, `OffsetDateTime`,
 * `ZonedDateTime` and `Duration`.
 *
 * <p>
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
 * @author Wu Yuping
 */
class Instant(val epochSecond: Long, val nanos: Int): Comparable<Instant> {

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
        </pre> *
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

    //-----------------------------------------------------------------------
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
}