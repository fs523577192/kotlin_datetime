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
package org.firas.datetime.zone

import org.firas.datetime.*
import org.firas.datetime.temporal.TemporalAdjusters.Companion.nextOrSame
import org.firas.datetime.temporal.TemporalAdjusters.Companion.previousOrSame

/**
 * A rule expressing how to create a transition.
 *
 *
 * This class allows rules for identifying future transitions to be expressed.
 * A rule might be written in many forms:
 *
 *
 * * the 16th March
 * * the Sunday on or after the 16th March
 * * the Sunday on or before the 16th March
 * * the last Sunday in February
 *
 *
 * These different rule types can be expressed and queried.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class ZoneOffsetTransitionRule(
    /**
     * The month of the month-day of the first day of the cutover week.
     * The actual date will be adjusted by the dowChange field.
     */
    private val month: Month,

    /**
     * The day-of-month of the month-day of the cutover week.
     * If positive, it is the start of the week where the cutover can occur.
     * If negative, it represents the end of the week where cutover can occur.
     * The value is the number of days from the end of the month, such that
     * `-1` is the last day of the month, `-2` is the second
     * to last day, and so on.
     */
    private val dom: Byte,

    /**
     * The cutover day-of-week, null to retain the day-of-month.
     */
    private val dow: DayOfWeek?,

    /**
     * The cutover time in the 'before' offset.
     */
    private val time: LocalTime,

    /**
     * Whether the cutover time is midnight at the end of day.
     */
    private val timeEndOfDay: Boolean,

    /**
     * The definition of how the local time should be interpreted.
     */
    private val timeDefinition: TimeDefinition,

    /**
     * The standard offset at the cutover.
     */
    private val standardOffset: ZoneOffset,

    /**
     * The offset before the cutover.
     */
    private val offsetBefore: ZoneOffset,

    /**
     * The offset after the cutover.
     */
    private val offsetAfter: ZoneOffset
) {
    init {
        if (time.nano != 0) {
            throw AssertionError()
        }
    }

    companion object {
        /**
         * Serialization version.
         */
        private const val serialVersionUID = 6889046316657758795L

        /**
         * Obtains an instance defining the yearly rule to create transitions between two offsets.
         *
         *
         * Applications should normally obtain an instance from [ZoneRules].
         * This factory is only intended for use when creating [ZoneRules].
         *
         * @param month  the month of the month-day of the first day of the cutover week, not null
         * @param dayOfMonthIndicator  the day of the month-day of the cutover week, positive if the week is that
         *  day or later, negative if the week is that day or earlier, counting from the last day of the month,
         *  from -28 to 31 excluding 0
         * @param dayOfWeek  the required day-of-week, null if the month-day should not be changed
         * @param time  the cutover time in the 'before' offset, not null
         * @param timeEndOfDay  whether the time is midnight at the end of day
         * @param timeDefnition  how to interpret the cutover
         * @param standardOffset  the standard offset in force at the cutover, not null
         * @param offsetBefore  the offset before the cutover, not null
         * @param offsetAfter  the offset after the cutover, not null
         * @return the rule, not null
         * @throws IllegalArgumentException if the day of month indicator is invalid
         * @throws IllegalArgumentException if the end of day flag is true when the time is not midnight
         * @throws IllegalArgumentException if `time.nano` returns non-zero value
         */
        fun of(
            month: Month,
            dayOfMonthIndicator: Int,
            dayOfWeek: DayOfWeek,
            time: LocalTime,
            timeEndOfDay: Boolean,
            timeDefnition: TimeDefinition,
            standardOffset: ZoneOffset,
            offsetBefore: ZoneOffset,
            offsetAfter: ZoneOffset
        ): ZoneOffsetTransitionRule {
            if (dayOfMonthIndicator < -28 || dayOfMonthIndicator > 31 || dayOfMonthIndicator == 0) {
                throw IllegalArgumentException("Day of month indicator must be between -28 and 31 inclusive excluding zero")
            }
            if (timeEndOfDay && time != LocalTime.MIDNIGHT) {
                throw IllegalArgumentException("Time must be midnight when end of day flag is true")
            }
            if (time.nano != 0) {
                throw IllegalArgumentException("Time's nano-of-second must be zero")
            }
            return ZoneOffsetTransitionRule(
                month, dayOfMonthIndicator.toByte(), dayOfWeek,
                time, timeEndOfDay, timeDefnition, standardOffset, offsetBefore, offsetAfter
            )
        }

        //-----------------------------------------------------------------------
        /**
         * A definition of the way a local time can be converted to the actual
         * transition date-time.
         *
         *
         * Time zone rules are expressed in one of three ways:
         *
         *  * Relative to UTC
         *  * Relative to the standard offset in force
         *  * Relative to the wall offset (what you would see on a clock on the wall)
         *
         */
        enum class TimeDefinition {
            /** The local date-time is expressed in terms of the UTC offset.  */
            UTC,
            /** The local date-time is expressed in terms of the wall offset.  */
            WALL,
            /** The local date-time is expressed in terms of the standard offset.  */
            STANDARD;

            /**
             * Converts the specified local date-time to the local date-time actually
             * seen on a wall clock.
             *
             *
             * This method converts using the type of this enum.
             * The output is defined relative to the 'before' offset of the transition.
             *
             *
             * The UTC type uses the UTC offset.
             * The STANDARD type uses the standard offset.
             * The WALL type returns the input date-time.
             * The result is intended for use with the wall-offset.
             *
             * @param dateTime  the local date-time, not null
             * @param standardOffset  the standard offset, not null
             * @param wallOffset  the wall offset, not null
             * @return the date-time relative to the wall/before offset, not null
             */
            fun createDateTime(
                dateTime: LocalDateTime,
                standardOffset: ZoneOffset,
                wallOffset: ZoneOffset
            ): LocalDateTime {
                return when (this) {
                    UTC -> {
                        val difference = wallOffset.totalSeconds - ZoneOffset.UTC.totalSeconds
                        dateTime.plusSeconds(difference.toLong())
                    }
                    STANDARD -> {
                        val difference = wallOffset.totalSeconds - standardOffset.totalSeconds
                        dateTime.plusSeconds(difference.toLong())
                    }
                    else  // WALL
                    -> dateTime
                }
            }
        }
    }

    /**
     * Creates a transition instance for the specified year.
     *
     *
     * Calculations are performed using the ISO-8601 chronology.
     *
     * @param year  the year to create a transition for, not null
     * @return the transition instance, not null
     */
    fun createTransition(year: Int): ZoneOffsetTransition {
        var date: LocalDate
        if (this.dom < 0) {
            date = LocalDate.of(year, this.month,
                this.month.length(Year.isLeap(year)) + 1 + this.dom)
            if (this.dow != null) {
                date = date.with(previousOrSame(this.dow))
            }
        } else {
            date = LocalDate.of(year, this.month, this.dom.toInt())
            if (this.dow != null) {
                date = date.with(nextOrSame(this.dow))
            }
        }
        if (this.timeEndOfDay) {
            date = date.plusDays(1)
        }
        val localDT = LocalDateTime.of(date, this.time)
        val transition = this.timeDefinition.createDateTime(localDT, this.standardOffset, this.offsetBefore)
        return ZoneOffsetTransition(transition, this.offsetBefore, this.offsetAfter)
    }

    // ----==== Override methods inherited from Any ====----
    /**
     * Checks if this object equals another.
     *
     *
     * The entire state of the object is compared.
     *
     * @param other  the other object to compare to, null returns false
     * @return true if equal
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is ZoneOffsetTransitionRule) {
            return this.month == other.month &&
                    this.dom == other.dom &&
                    this.dow == other.dow &&
                    this.timeDefinition == other.timeDefinition &&
                    this.time == other.time &&
                    this.timeEndOfDay == other.timeEndOfDay &&
                    this.standardOffset == other.standardOffset &&
                    this.offsetBefore == other.offsetBefore &&
                    this.offsetAfter == other.offsetAfter
        }
        return false
    }

    override fun hashCode(): Int {
        val hash = (this.time.toSecondOfDay() + if (this.timeEndOfDay) 1 else 0).shl(15) +
                this.month.ordinal.shl(11) + (this.dom + 32).shl(5) +
                (if (this.dow == null) 7 else this.dow.ordinal).shl(2) + this.timeDefinition.ordinal
        return hash xor this.standardOffset.hashCode() xor
                this.offsetBefore.hashCode() xor this.offsetAfter.hashCode()
    }

    override fun toString(): String {
        val buf = StringBuilder()
        buf.append("TransitionRule[")
            .append(if (this.offsetBefore > this.offsetAfter) "Gap " else "Overlap ")
            .append(offsetBefore).append(" to ").append(offsetAfter).append(", ")
        if (this.dow != null) {
            if (this.dom.toInt() == -1) {
                buf.append(this.dow.name).append(" on or before last day of ").append(this.month.name)
            } else if (this.dom < 0) {
                buf.append(this.dow.name).append(" on or before last day minus ")
                    .append(-this.dom - 1).append(" of ").append(this.month.name)
            } else {
                buf.append(this.dow.name).append(" on or after ")
                    .append(this.month.name).append(' ').append(this.dom)
            }
        } else {
            buf.append(this.month.name).append(' ').append(this.dom)
        }
        buf.append(" at ").append(if (this.timeEndOfDay) "24:00" else time.toString())
            .append(" ").append(this.timeDefinition)
            .append(", standard offset ").append(this.standardOffset)
            .append(']')
        return buf.toString()
    }
}