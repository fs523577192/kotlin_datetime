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

import org.firas.datetime.zone.ZoneOffset
import org.firas.datetime.Instant
import org.firas.datetime.LocalTime
import org.firas.datetime.temporal.ChronoField



/**
 * A date-time without a time-zone in an arbitrary chronology, intended
 * for advanced globalization use cases.
 * <p>
 * <b>Most applications should declare method signatures, fields and variables
 * as {@link LocalDateTime}, not this interface.</b>
 * <p>
 * A {@code ChronoLocalDateTime} is the abstract representation of a local date-time
 * where the {@code Chronology chronology}, or calendar system, is pluggable.
 * The date-time is defined in terms of fields expressed by {@link TemporalField},
 * where most common implementations are defined in {@link ChronoField}.
 * The chronology defines how the calendar system operates and the meaning of
 * the standard fields.
 *
 * <h3>When to use this interface</h3>
 * The design of the API encourages the use of {@code LocalDateTime} rather than this
 * interface, even in the case where the application needs to deal with multiple
 * calendar systems. The rationale for this is explored in detail in {@link ChronoLocalDate}.
 * <p>
 * Ensure that the discussion in {@code ChronoLocalDate} has been read and understood
 * before using this interface.
 *
 * @implSpec
 * This interface must be implemented with care to ensure other classes operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * Subclasses should be Serializable wherever possible.
 *
 * @param <D> the concrete type for the date of this date-time
 * @since Java 1.8
 * @author Wu Yuping
 */
interface ChronoLocalDateTime<D: ChronoLocalDate> {

    /**
     * renamed from ~toLocalDate`
     */
    fun getDate(): D

    /**
     * renamed from `toLocalTime`
     */
    fun getTime(): LocalTime

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
        return getDate().getChronology()
    }

    /**
     * Converts this date-time to an `Instant`.
     *
     *
     * This combines this local date-time and the specified offset to form
     * an `Instant`.
     *
     *
     * This default implementation calculates from the epoch-day of the date and the
     * second-of-day of the time.
     *
     * @param offset  the offset to use for the conversion, not null
     * @return an `Instant` representing the same instant, not null
     */
    fun toInstant(offset: ZoneOffset): Instant {
        return Instant.ofEpochSecond(toEpochSecond(offset), getTime().nano.toLong())
    }

    /**
     * Converts this date-time to the number of seconds from the epoch
     * of 1970-01-01T00:00:00Z.
     *
     *
     * This combines this local date-time and the specified offset to calculate the
     * epoch-second value, which is the number of elapsed seconds from 1970-01-01T00:00:00Z.
     * Instants on the time-line after the epoch are positive, earlier are negative.
     *
     *
     * This default implementation calculates from the epoch-day of the date and the
     * second-of-day of the time.
     *
     * @param offset  the offset to use for the conversion, not null
     * @return the number of seconds from the epoch of 1970-01-01T00:00:00Z
     */
    fun toEpochSecond(offset: ZoneOffset): Long {
        val epochDay = getDate().toEpochDay()
        var secs = epochDay * 86400 + getTime().toSecondOfDay()
        secs -= offset.totalSeconds.toLong()
        return secs
    }

    /**
     * Compares this date-time to another date-time, including the chronology.
     *
     *
     * The comparison is based first on the underlying time-line date-time, then
     * on the chronology.
     * It is "consistent with equals", as defined by [Comparable].
     *
     *
     * For example, the following is the comparator order:
     *
     *  1. `2012-12-03T12:00 (ISO)`
     *  1. `2012-12-04T12:00 (ISO)`
     *  1. `2555-12-04T12:00 (ThaiBuddhist)`
     *  1. `2012-12-05T12:00 (ISO)`
     *
     * Values #2 and #3 represent the same date-time on the time-line.
     * When two values represent the same date-time, the chronology ID is compared to distinguish them.
     * This step is needed to make the ordering "consistent with equals".
     *
     *
     * If all the date-time objects being compared are in the same chronology, then the
     * additional chronology stage is not required and only the local date-time is used.
     *
     *
     * This default implementation performs the comparison defined above.
     *
     * @param other  the other date-time to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    operator fun compareTo(other: ChronoLocalDateTime<*>): Int {
        var cmp = getDate().compareTo(other.getDate())
        if (cmp == 0) {
            cmp = getTime().compareTo(other.getTime())
            if (cmp == 0) {
                cmp = getChronology().compareTo(other.getChronology())
            }
        }
        return cmp
    }

    /**
     * Checks if this date-time is after the specified date-time ignoring the chronology.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the underlying date-time and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     *
     *
     * This default implementation performs the comparison based on the epoch-day
     * and nano-of-day.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this is after the specified date-time
     */
    fun isAfter(other: ChronoLocalDateTime<*>): Boolean {
        val thisEpDay = this.getDate().toEpochDay()
        val otherEpDay = other.getDate().toEpochDay()
        return thisEpDay > otherEpDay ||
                thisEpDay == otherEpDay && this.getTime().toNanoOfDay() > other.getTime().toNanoOfDay()
    }

    /**
     * Checks if this date-time is before the specified date-time ignoring the chronology.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the underlying date-time and not the chronology.
     * This allows dates in different calendar systems to be compared based
     * on the time-line position.
     *
     *
     * This default implementation performs the comparison based on the epoch-day
     * and nano-of-day.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if this is before the specified date-time
     */
    fun isBefore(other: ChronoLocalDateTime<*>): Boolean {
        val thisEpDay = this.getDate().toEpochDay()
        val otherEpDay = other.getDate().toEpochDay()
        return thisEpDay < otherEpDay ||
                thisEpDay == otherEpDay && this.getTime().toNanoOfDay() < other.getTime().toNanoOfDay()
    }

    /**
     * Checks if this date-time is equal to the specified date-time ignoring the chronology.
     *
     *
     * This method differs from the comparison in [.compareTo] in that it
     * only compares the underlying date and time and not the chronology.
     * This allows date-times in different calendar systems to be compared based
     * on the time-line position.
     *
     *
     * This default implementation performs the comparison based on the epoch-day
     * and nano-of-day.
     *
     * @param other  the other date-time to compare to, not null
     * @return true if the underlying date-time is equal to the specified date-time on the timeline
     */
    fun isEqual(other: ChronoLocalDateTime<*>): Boolean {
        // Do the time check first, it is cheaper than computing EPOCH day.
        return this.getTime().toNanoOfDay() == other.getTime().toNanoOfDay() &&
                this.getDate().toEpochDay() == other.getDate().toEpochDay()
    }
}