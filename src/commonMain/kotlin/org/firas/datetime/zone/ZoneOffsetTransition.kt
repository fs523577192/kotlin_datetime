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

import org.firas.datetime.Duration
import org.firas.datetime.Instant
import org.firas.datetime.LocalDateTime
import org.firas.util.Integers

/**
 * A transition between two offsets caused by a discontinuity in the local time-line.
 *
 *
 * A transition between two offsets is normally the result of a daylight savings cutover.
 * The discontinuity is normally a gap in spring and an overlap in autumn.
 * {@code ZoneOffsetTransition} models the transition between the two offsets.
 *
 *
 * Gaps occur where there are local date-times that simply do not exist.
 * An example would be when the offset changes from {@code +03:00} to {@code +04:00}.
 * This might be described as 'the clocks will move forward one hour tonight at 1am'.
 *
 *
 * Overlaps occur where there are local date-times that exist twice.
 * An example would be when the offset changes from {@code +04:00} to {@code +03:00}.
 * This might be described as 'the clocks will move back one hour tonight at 2am'.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class ZoneOffsetTransition private constructor(
    /**
     * The transition epoch-second.
     */
    val epochSecond: Long,

    /**
     * The local transition date-time at the transition.
     */
    private val transition: LocalDateTime,

    /**
     * The offset before transition.
     */
    val offsetBefore: ZoneOffset,

    /**
     * The offset after transition.
     */
    val offsetAfter: ZoneOffset
) : Comparable<ZoneOffsetTransition> {

    /**
     * Creates an instance defining a transition between two offsets.
     *
     * @param transition  the transition date-time with the offset before the transition, not null
     * @param offsetBefore  the offset before the transition, not null
     * @param offsetAfter  the offset at and after the transition, not null
     */
    internal constructor(transition: LocalDateTime, offsetBefore: ZoneOffset, offsetAfter: ZoneOffset): this(
        transition.toEpochSecond(offsetBefore),
        transition, offsetBefore, offsetAfter
    ) {
        if (transition.getNano() != 0) {
            throw AssertionError()
        }
    }

    /**
     * Creates an instance from epoch-second and offsets.
     *
     * @param epochSecond  the transition epoch-second
     * @param offsetBefore  the offset before the transition, not null
     * @param offsetAfter  the offset at and after the transition, not null
     */
    internal constructor(epochSecond: Long, offsetBefore: ZoneOffset, offsetAfter: ZoneOffset): this(
        epochSecond,
        LocalDateTime.ofEpochSecond(epochSecond, 0, offsetBefore),
        offsetBefore, offsetAfter
    )

    //-------------------------------------------------------------------------
    /**
     * Gets the local transition date-time, as would be expressed with the 'before' offset.
     *
     *
     * This is the date-time where the discontinuity begins expressed with the 'before' offset.
     * At this instant, the 'after' offset is actually used, therefore the combination of this
     * date-time and the 'before' offset will never occur.
     *
     *
     * The combination of the 'before' date-time and offset represents the same instant
     * as the 'after' date-time and offset.
     *
     * @return the transition date-time expressed with the before offset, not null
     */
    fun getDateTimeBefore(): LocalDateTime {
        return transition
    }

    /**
     * Gets the local transition date-time, as would be expressed with the 'after' offset.
     *
     *
     * This is the first date-time after the discontinuity, when the new offset applies.
     *
     *
     * The combination of the 'before' date-time and offset represents the same instant
     * as the 'after' date-time and offset.
     *
     * @return the transition date-time expressed with the after offset, not null
     */
    fun getDateTimeAfter(): LocalDateTime {
        return transition.plusSeconds(getDurationSeconds().toLong())
    }

    /**
     * Gets the transition instant.
     *
     *
     * This is the instant of the discontinuity, which is defined as the first
     * instant that the 'after' offset applies.
     *
     *
     * The methods [.getInstant], [.getDateTimeBefore] and [.getDateTimeAfter]
     * all represent the same instant.
     *
     * @return the transition instant, not null
     */
    fun getInstant(): Instant {
        return Instant.ofEpochSecond(epochSecond)
    }

    /**
     * Gets the duration of the transition.
     *
     *
     * In most cases, the transition duration is one hour, however this is not always the case.
     * The duration will be positive for a gap and negative for an overlap.
     * Time-zones are second-based, so the nanosecond part of the duration will be zero.
     *
     * @return the duration of the transition, positive for gaps, negative for overlaps
     */
    fun getDuration(): Duration {
        return Duration.ofSeconds(getDurationSeconds().toLong())
    }

    /**
     * Does this transition represent a gap in the local time-line.
     *
     *
     * Gaps occur where there are local date-times that simply do not exist.
     * An example would be when the offset changes from `+01:00` to `+02:00`.
     * This might be described as 'the clocks will move forward one hour tonight at 1am'.
     *
     * @return true if this transition is a gap, false if it is an overlap
     */
    fun isGap(): Boolean {
        return this.offsetAfter.totalSeconds > this.offsetBefore.totalSeconds
    }

    /**
     * Does this transition represent an overlap in the local time-line.
     *
     *
     * Overlaps occur where there are local date-times that exist twice.
     * An example would be when the offset changes from `+02:00` to `+01:00`.
     * This might be described as 'the clocks will move back one hour tonight at 2am'.
     *
     * @return true if this transition is an overlap, false if it is a gap
     */
    fun isOverlap(): Boolean {
        return this.offsetAfter.totalSeconds < this.offsetBefore.totalSeconds
    }

    /**
     * Checks if the specified offset is valid during this transition.
     *
     *
     * This checks to see if the given offset will be valid at some point in the transition.
     * A gap will always return false.
     * An overlap will return true if the offset is either the before or after offset.
     *
     * @param offset  the offset to check, null returns false
     * @return true if the offset is valid during the transition
     */
    fun isValidOffset(offset: ZoneOffset): Boolean {
        return if (isGap()) false else (this.offsetBefore == offset || this.offsetAfter == offset)
    }

    /**
     * Compares this transition to another based on the transition instant.
     *
     *
     * This compares the instants of each transition.
     * The offsets are ignored, making this order inconsistent with equals.
     *
     * @param transition  the transition to compare to, not null
     * @return the comparator value, negative if less, positive if greater
     */
    override fun compareTo(other: ZoneOffsetTransition): Int {
        return Integers.compare(this.epochSecond, other.epochSecond)
    }

    /**
     * Gets the valid offsets during this transition.
     *
     *
     * A gap will return an empty list, while an overlap will return both offsets.
     *
     * @return the list of valid offsets
     */
    internal fun getValidOffsets(): List<ZoneOffset> {
        return if (isGap()) listOf()
                else listOf(this.offsetBefore, this.offsetAfter)
    }

    /**
     * Gets the duration of the transition in seconds.
     *
     * @return the duration in seconds
     */
    private fun getDurationSeconds(): Int {
        return this.offsetAfter.totalSeconds - this.offsetBefore.totalSeconds
    }
}