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
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * The range of valid values for a date-time field.
 * <p>
 * All {@link TemporalField} instances have a valid range of values.
 * For example, the ISO day-of-month runs from 1 to somewhere between 28 and 31.
 * This class captures that valid range.
 * <p>
 * It is important to be aware of the limitations of this class.
 * Only the minimum and maximum values are provided.
 * It is possible for there to be invalid values within the outer range.
 * For example, a weird field may have valid values of 1, 2, 4, 6, 7, thus
 * have a range of '1 - 7', despite that fact that values 3 and 5 are invalid.
 * <p>
 * Instances of this class are not tied to a specific field.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
class ValueRange private constructor(
    private val minSmallest: Long,
    private val minLargest: Long,
    private val maxSmallest: Long,
    private val maxLargest: Long
) {
    companion object {
        /**
         * Serialization version.
         */
        @JvmStatic
        private const val serialVersionUID = -7317881728594519368L

        /**
         * Obtains a fixed value range.
         *
         *
         * This factory obtains a range where the minimum and maximum values are fixed.
         * For example, the ISO month-of-year always runs from 1 to 12.
         *
         * @param min  the minimum value
         * @param max  the maximum value
         * @return the ValueRange for min, max, not null
         * @throws IllegalArgumentException if the minimum is greater than the maximum
         */
        @JsName("ofMinAndMax")
        @JvmStatic
        fun of(min: Long, max: Long): ValueRange {
            if (min > max) {
                throw IllegalArgumentException("Minimum value must be less than maximum value")
            }
            return ValueRange(min, min, max, max)
        }

        /**
         * Obtains a variable value range.
         *
         *
         * This factory obtains a range where the minimum value is fixed and the maximum value may vary.
         * For example, the ISO day-of-month always starts at 1, but ends between 28 and 31.
         *
         * @param min  the minimum value
         * @param maxSmallest  the smallest maximum value
         * @param maxLargest  the largest maximum value
         * @return the ValueRange for min, smallest max, largest max, not null
         * @throws IllegalArgumentException if
         * the minimum is greater than the smallest maximum,
         * or the smallest maximum is greater than the largest maximum
         */
        @JsName("ofMinAndMaxRange")
        @JvmStatic
        fun of(min: Long, maxSmallest: Long, maxLargest: Long): ValueRange {
            return of(min, min, maxSmallest, maxLargest)
        }

        /**
         * Obtains a fully variable value range.
         *
         *
         * This factory obtains a range where both the minimum and maximum value may vary.
         *
         * @param minSmallest  the smallest minimum value
         * @param minLargest  the largest minimum value
         * @param maxSmallest  the smallest maximum value
         * @param maxLargest  the largest maximum value
         * @return the ValueRange for smallest min, largest min, smallest max, largest max, not null
         * @throws IllegalArgumentException if
         * the smallest minimum is greater than the smallest maximum,
         * or the smallest maximum is greater than the largest maximum
         * or the largest minimum is greater than the largest maximum
         */
        @JsName("ofMinRangeAndMaxRange")
        @JvmStatic
        fun of(minSmallest: Long, minLargest: Long, maxSmallest: Long, maxLargest: Long): ValueRange {
            if (minSmallest > minLargest) {
                throw IllegalArgumentException("Smallest minimum value must be less than largest minimum value")
            }
            if (maxSmallest > maxLargest) {
                throw IllegalArgumentException("Smallest maximum value must be less than largest maximum value")
            }
            if (minLargest > maxLargest) {
                throw IllegalArgumentException("Minimum value must be less than maximum value")
            }
            return ValueRange(minSmallest, minLargest, maxSmallest, maxLargest)
        }
    } // companion object

    /**
     * Is the value range fixed and fully known.
     *
     *
     * For example, the ISO day-of-month runs from 1 to between 28 and 31.
     * Since there is uncertainty about the maximum value, the range is not fixed.
     * However, for the month of January, the range is always 1 to 31, thus it is fixed.
     *
     * @return true if the set of values is fixed
     */
    @JsName("isFixed")
    fun isFixed(): Boolean {
        return this.minSmallest == this.minLargest && this.maxSmallest == this.maxLargest
    }

    /**
     * Gets the minimum value that the field can take.
     *
     *
     * For example, the ISO day-of-month always starts at 1.
     * The minimum is therefore 1.
     *
     * @return the minimum value for this field
     */
    @JsName("getMinimum")
    fun getMinimum(): Long {
        return this.minSmallest
    }

    /**
     * Gets the largest possible minimum value that the field can take.
     *
     *
     * For example, the ISO day-of-month always starts at 1.
     * The largest minimum is therefore 1.
     *
     * @return the largest possible minimum value for this field
     */
    @JsName("getLargestMinimum")
    fun getLargestMinimum(): Long {
        return this.minLargest
    }

    /**
     * Gets the smallest possible maximum value that the field can take.
     *
     *
     * For example, the ISO day-of-month runs to between 28 and 31 days.
     * The smallest maximum is therefore 28.
     *
     * @return the smallest possible maximum value for this field
     */
    @JsName("getSmallestMaximum")
    fun getSmallestMaximum(): Long {
        return this.maxSmallest
    }

    /**
     * Gets the maximum value that the field can take.
     *
     *
     * For example, the ISO day-of-month runs to between 28 and 31 days.
     * The maximum is therefore 31.
     *
     * @return the maximum value for this field
     */
    @JsName("getMaximum")
    fun getMaximum(): Long {
        return this.maxLargest
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if all values in the range fit in an `int`.
     *
     *
     * This checks that all valid values are within the bounds of an `int`.
     *
     *
     * For example, the ISO month-of-year has values from 1 to 12, which fits in an `int`.
     * By comparison, ISO nano-of-day runs from 1 to 86,400,000,000,000 which does not fit in an `int`.
     *
     *
     * This implementation uses [.getMinimum] and [.getMaximum].
     *
     * @return true if a valid value always fits in an `int`
     */
    @JsName("isIntValue")
    fun isIntValue(): Boolean {
        return getMinimum() >= Int.MIN_VALUE && getMaximum() <= Int.MAX_VALUE
    }

    /**
     * Checks if the value is within the valid range.
     *
     *
     * This checks that the value is within the stored range of values.
     *
     * @param value  the value to check
     * @return true if the value is valid
     */
    @JsName("isValidValue")
    fun isValidValue(value: Long): Boolean {
        return value >= getMinimum() && value <= getMaximum()
    }

    /**
     * Checks if the value is within the valid range and that all values
     * in the range fit in an `int`.
     *
     *
     * This method combines [.isIntValue] and [.isValidValue].
     *
     * @param value  the value to check
     * @return true if the value is valid and fits in an `int`
     */
    @JsName("isValidIntValue")
    fun isValidIntValue(value: Long): Boolean {
        return isIntValue() && isValidValue(value)
    }

    /**
     * Checks that the specified value is valid.
     *
     *
     * This validates that the value is within the valid range of values.
     * The field is only used to improve the error message.
     *
     * @param value  the value to check
     * @param field  the field being checked, may be null
     * @return the value that was passed in
     * @see .isValidValue
     */
    @JsName("checkValidValue")
    fun checkValidValue(value: Long, field: TemporalField): Long {
        if (!isValidValue(value)) {
            throw DateTimeException(genInvalidFieldMessage(field, value))
        }
        return value
    }

    /**
     * Checks that the specified value is valid and fits in an `int`.
     *
     *
     * This validates that the value is within the valid range of values and that
     * all valid values are within the bounds of an `int`.
     * The field is only used to improve the error message.
     *
     * @param value  the value to check
     * @param field  the field being checked, may be null
     * @return the value that was passed in
     * @see .isValidIntValue
     */
    @JsName("checkValidIntValue")
    fun checkValidIntValue(value: Long, field: TemporalField): Int {
        if (!isValidIntValue(value)) {
            throw DateTimeException(genInvalidFieldMessage(field, value))
        }
        return value.toInt()
    }

    private fun genInvalidFieldMessage(field: TemporalField?, value: Long): String {
        return if (field != null) {
            "Invalid value for " + field + " (valid values " + this + "): " + value
        } else {
            "Invalid value (valid values " + this + "): " + value
        }
    }

    // ----==== override methods inherited from Any ----
    /**
     * Checks if this range is equal to another range.
     *
     *
     * The comparison is based on the four values, minimum, largest minimum,
     * smallest maximum and maximum.
     * Only objects of type `ValueRange` are compared, other types return false.
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other range
     */
    override fun equals(other: Any?): Boolean {
        if (other === this) {
            return true
        }
        if (other is ValueRange) {
            return this.minSmallest == other.minSmallest && this.minLargest == other.minLargest &&
                    this.maxSmallest == other.maxSmallest && this.maxLargest == other.maxLargest
        }
        return false
    }

    /**
     * A hash code for this range.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        val hash = this.minSmallest + (this.minLargest shl 16) + (this.minLargest shr 48) +
                (this.maxSmallest shl 32) + (this.maxSmallest shr 32) + (this.maxLargest shl 48) +
                (this.maxLargest shr 16)
        return (hash xor hash.ushr(32)).toInt()
    }

    //-----------------------------------------------------------------------
    /**
     * Outputs this range as a `String`.
     *
     *
     * The format will be '{min}/{largestMin} - {smallestMax}/{max}',
     * where the largestMin or smallestMax sections may be omitted, together
     * with associated slash, if they are the same as the min or max.
     *
     * @return a string representation of this range, not null
     */
    override fun toString(): String {
        val buf = StringBuilder()
        buf.append(this.minSmallest)
        if (this.minSmallest != this.minLargest) {
            buf.append('/').append(this.minLargest)
        }
        buf.append(" - ").append(this.maxSmallest)
        if (this.maxSmallest != this.maxLargest) {
            buf.append('/').append(this.maxLargest)
        }
        return buf.toString()
    }
}