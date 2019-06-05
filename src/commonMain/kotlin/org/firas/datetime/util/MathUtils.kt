/*
 * Copyright (c) 1994, 2013, Oracle and/or its affiliates. All rights reserved.
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
package org.firas.datetime.util

import kotlin.js.JsName
import kotlin.jvm.JvmStatic
import kotlin.math.absoluteValue

/**
 *
 * @author Wu Yuping
 * @version 1.0.0
 * @since 1.0.0
 */
class MathUtils private constructor() {

    companion object {
        /**
         * Returns the value of the `long` argument;
         * throwing an exception if the value overflows an `int`.
         *
         * @param value the long value
         * @return the argument as an int
         * @throws ArithmeticException if the `argument` overflows an int
         * @since Java 1.8
         */
        @JsName("toIntExact")
        @JvmStatic
        fun toIntExact(value: Long): Int {
            if (value.toInt().toLong() != value) {
                throw ArithmeticException("integer overflow")
            }
            return value.toInt()
        }

        /**
         * Returns the sum of its arguments,
         * throwing an exception if the result overflows an `int`.
         *
         * @param x the first value
         * @param y the second value
         * @return the result
         * @throws ArithmeticException if the result overflows an int
         * @since Java 1.8
         */
        @JsName("addExactInt")
        @JvmStatic
        fun addExact(x: Int, y: Int): Int {
            val r = x + y
            // HD 2-12 Overflow iff both arguments have the opposite sign of the result
            if (x xor r and (y xor r) < 0) {
                throw ArithmeticException("integer overflow")
            }
            return r
        }

        /**
         * Returns the sum of its arguments,
         * throwing an exception if the result overflows a `long`.
         *
         * @param x the first value
         * @param y the second value
         * @return the result
         * @throws ArithmeticException if the result overflows a long
         * @since Java 1.8
         */
        @JsName("addExactLong")
        @JvmStatic
        fun addExact(x: Long, y: Long): Long {
            val r = x + y
            // HD 2-12 Overflow iff both arguments have the opposite sign of the result
            if (x xor r and (y xor r) < 0) {
                throw ArithmeticException("long overflow")
            }
            return r
        }

        /**
         * Returns the difference of the arguments,
         * throwing an exception if the result overflows an `int`.
         *
         * @param x the first value
         * @param y the second value to subtract from the first
         * @return the result
         * @throws ArithmeticException if the result overflows an int
         * @since Java 1.8
         */
        @JsName("subtractExactInt")
        @JvmStatic
        fun subtractExact(x: Int, y: Int): Int {
            val r = x - y
            // HD 2-12 Overflow iff the arguments have different signs and
            // the sign of the result is different than the sign of x
            if (x xor y and (x xor r) < 0) {
                throw ArithmeticException("integer overflow")
            }
            return r
        }

        /**
         * Returns the difference of the arguments,
         * throwing an exception if the result overflows a `long`.
         *
         * @param x the first value
         * @param y the second value to subtract from the first
         * @return the result
         * @throws ArithmeticException if the result overflows a long
         * @since Java 1.8
         */
        @JsName("subtractExactLong")
        @JvmStatic
        fun subtractExact(x: Long, y: Long): Long {
            val r = x - y
            // HD 2-12 Overflow iff the arguments have different signs and
            // the sign of the result is different than the sign of x
            if (x xor y and (x xor r) < 0) {
                throw ArithmeticException("long overflow")
            }
            return r
        }

        /**
         * Returns the product of the arguments,
         * throwing an exception if the result overflows an `int`.
         *
         * @param x the first value
         * @param y the second value
         * @return the result
         * @throws ArithmeticException if the result overflows an int
         * @since Java 1.8
         */
        @JsName("multiplyExactInt")
        @JvmStatic
        fun multiplyExact(x: Int, y: Int): Int {
            val r = x.toLong() * y.toLong()
            if (r.toInt().toLong() != r) {
                throw ArithmeticException("integer overflow")
            }
            return r.toInt()
        }

        /**
         * Returns the product of the arguments,
         * throwing an exception if the result overflows a `long`.
         *
         * @param x the first value
         * @param y the second value
         * @return the result
         * @throws ArithmeticException if the result overflows a long
         * @since Java 1.8
         */
        @JsName("multiplyExactLong")
        @JvmStatic
        fun multiplyExact(x: Long, y: Long): Long {
            val r = x * y
            val ax = x.absoluteValue
            val ay = y.absoluteValue
            if ((ax or ay).ushr(31) != 0L) {
                // Some bits greater than 2^31 that might cause overflow
                // Check the result using the divide operator
                // and check for the special case of Long.MIN_VALUE * -1
                if (y != 0L && r / y != x || x == Long.MIN_VALUE && y == -1L) {
                    throw ArithmeticException("long overflow")
                }
            }
            return r
        }

        /**
         * Returns the argument incremented by one, throwing an exception if the
         * result overflows an `int`.
         *
         * @param a the value to increment
         * @return the result
         * @throws ArithmeticException if the result overflows an int
         * @since Java 1.8
         */
        @JsName("incrementExactInt")
        @JvmStatic
        fun incrementExact(a: Int): Int {
            if (a == Int.MAX_VALUE) {
                throw ArithmeticException("integer overflow")
            }
            return a + 1
        }

        /**
         * Returns the argument incremented by one, throwing an exception if the
         * result overflows a `long`.
         *
         * @param a the value to increment
         * @return the result
         * @throws ArithmeticException if the result overflows a long
         * @since Java 1.8
         */
        @JsName("incrementExactLong")
        @JvmStatic
        fun incrementExact(a: Long): Long {
            if (a == Long.MAX_VALUE) {
                throw ArithmeticException("long overflow")
            }
            return a + 1L
        }

        /**
         * Returns the argument decremented by one, throwing an exception if the
         * result overflows an `int`.
         *
         * @param a the value to decrement
         * @return the result
         * @throws ArithmeticException if the result overflows an int
         * @since Java 1.8
         */
        @JsName("decrementExactInt")
        @JvmStatic
        fun decrementExact(a: Int): Int {
            if (a == Int.MIN_VALUE) {
                throw ArithmeticException("integer overflow")
            }
            return a - 1
        }

        /**
         * Returns the argument decremented by one, throwing an exception if the
         * result overflows a `long`.
         *
         * @param a the value to decrement
         * @return the result
         * @throws ArithmeticException if the result overflows a long
         * @since Java 1.8
         */
        @JsName("decrementExactLong")
        @JvmStatic
        fun decrementExact(a: Long): Long {
            if (a == Long.MIN_VALUE) {
                throw ArithmeticException("long overflow")
            }

            return a - 1L
        }

        /**
         * Returns the negation of the argument, throwing an exception if the
         * result overflows an `int`.
         *
         * @param a the value to negate
         * @return the result
         * @throws ArithmeticException if the result overflows an int
         * @since Java 1.8
         */
        @JsName("negateExactInt")
        @JvmStatic
        fun negateExact(a: Int): Int {
            if (a == Int.MIN_VALUE) {
                throw ArithmeticException("integer overflow")
            }
            return -a
        }

        /**
         * Returns the negation of the argument, throwing an exception if the
         * result overflows a `long`.
         *
         * @param a the value to negate
         * @return the result
         * @throws ArithmeticException if the result overflows a long
         * @since Java 1.8
         */
        @JsName("negateExactLong")
        @JvmStatic
        fun negateExact(a: Long): Long {
            if (a == Long.MIN_VALUE) {
                throw ArithmeticException("long overflow")
            }
            return -a
        }

        /**
         * Returns the largest (closest to positive infinity)
         * `int` value that is less than or equal to the algebraic quotient.
         * There is one special case, if the dividend is the
         * [Integer.MIN_VALUE] and the divisor is `-1`,
         * then integer overflow occurs and
         * the result is equal to the `Integer.MIN_VALUE`.
         *
         *
         * Normal integer division operates under the round to zero rounding mode
         * (truncation).  This operation instead acts under the round toward
         * negative infinity (floor) rounding mode.
         * The floor rounding mode gives different results than truncation
         * when the exact result is negative.
         *
         *  * If the signs of the arguments are the same, the results of
         * `floorDiv` and the `/` operator are the same.  <br></br>
         * For example, `floorDiv(4, 3) == 1` and `(4 / 3) == 1`.
         *  * If the signs of the arguments are different,  the quotient is negative and
         * `floorDiv` returns the integer less than or equal to the quotient
         * and the `/` operator returns the integer closest to zero.<br></br>
         * For example, `floorDiv(-4, 3) == -2`,
         * whereas `(-4 / 3) == -1`.
         *
         *
         *
         * @param x the dividend
         * @param y the divisor
         * @return the largest (closest to positive infinity)
         * `int` value that is less than or equal to the algebraic quotient.
         * @throws ArithmeticException if the divisor `y` is zero
         * @see .floorMod
         * @see .floor
         * @since Java 1.8
         */
        @JsName("floorDivInt")
        @JvmStatic
        fun floorDiv(x: Int, y: Int): Int {
            var r = x / y
            // if the signs are different and modulo not zero, round down
            if (x xor y < 0 && r * y != x) {
                r -= 1
            }
            return r
        }

        /**
         * Returns the largest (closest to positive infinity)
         * `long` value that is less than or equal to the algebraic quotient.
         * There is one special case, if the dividend is the
         * [Long.MIN_VALUE] and the divisor is `-1`,
         * then integer overflow occurs and
         * the result is equal to the `Long.MIN_VALUE`.
         *
         *
         * Normal integer division operates under the round to zero rounding mode
         * (truncation).  This operation instead acts under the round toward
         * negative infinity (floor) rounding mode.
         * The floor rounding mode gives different results than truncation
         * when the exact result is negative.
         *
         *
         * For examples, see [.floorDiv].
         *
         * @param x the dividend
         * @param y the divisor
         * @return the largest (closest to positive infinity)
         * `long` value that is less than or equal to the algebraic quotient.
         * @throws ArithmeticException if the divisor `y` is zero
         * @see .floorMod
         * @see .floor
         * @since Java 1.8
         */
        @JsName("floorDivLong")
        @JvmStatic
        fun floorDiv(x: Long, y: Long): Long {
            var r = x / y
            // if the signs are different and modulo not zero, round down
            if (x xor y < 0 && r * y != x) {
                r -= 1
            }
            return r
        }

        /**
         * Returns the floor modulus of the `int` arguments.
         *
         *
         * The floor modulus is `x - (floorDiv(x, y) * y)`,
         * has the same sign as the divisor `y`, and
         * is in the range of `-abs(y) < r < +abs(y)`.
         *
         *
         *
         * The relationship between `floorDiv` and `floorMod` is such that:
         *
         *  * `floorDiv(x, y) * y + floorMod(x, y) == x`
         *
         *
         *
         * The difference in values between `floorMod` and
         * the `%` operator is due to the difference between
         * `floorDiv` that returns the integer less than or equal to the quotient
         * and the `/` operator that returns the integer closest to zero.
         *
         *
         * Examples:
         *
         *  * If the signs of the arguments are the same, the results
         * of `floorMod` and the `%` operator are the same.  <br></br>
         *
         *  * `floorMod(4, 3) == 1`; &nbsp; and `(4 % 3) == 1`
         *
         *  * If the signs of the arguments are different, the results differ from the `%` operator.<br></br>
         *
         *  * `floorMod(+4, -3) == -2`; &nbsp; and `(+4 % -3) == +1`
         *  * `floorMod(-4, +3) == +2`; &nbsp; and `(-4 % +3) == -1`
         *  * `floorMod(-4, -3) == -1`; &nbsp; and `(-4 % -3) == -1 `
         *
         *
         *
         * If the signs of arguments are unknown and a positive modulus
         * is needed it can be computed as `(floorMod(x, y) + abs(y)) % abs(y)`.
         *
         * @param x the dividend
         * @param y the divisor
         * @return the floor modulus `x - (floorDiv(x, y) * y)`
         * @throws ArithmeticException if the divisor `y` is zero
         * @see .floorDiv
         * @since Java 1.8
         */
        @JsName("floorModInt")
        @JvmStatic
        fun floorMod(x: Int, y: Int): Int {
            return x - floorDiv(x, y) * y
        }

        /**
         * Returns the floor modulus of the `long` arguments.
         *
         *
         * The floor modulus is `x - (floorDiv(x, y) * y)`,
         * has the same sign as the divisor `y`, and
         * is in the range of `-abs(y) < r < +abs(y)`.
         *
         *
         *
         * The relationship between `floorDiv` and `floorMod` is such that:
         *
         *  * `floorDiv(x, y) * y + floorMod(x, y) == x`
         *
         *
         *
         * For examples, see [.floorMod].
         *
         * @param x the dividend
         * @param y the divisor
         * @return the floor modulus `x - (floorDiv(x, y) * y)`
         * @throws ArithmeticException if the divisor `y` is zero
         * @see .floorDiv
         * @since Java 1.8
         */
        @JsName("floorModLong")
        @JvmStatic
        fun floorMod(x: Long, y: Long): Long {
            return x - floorDiv(x, y) * y
        }
    }
}