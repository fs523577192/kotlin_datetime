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
package org.firas.datetime.format

/**
 * Localized decimal style used in date and time formatting.
 *
 * A significant part of dealing with dates and times is the localization.
 * This class acts as a central point for accessing the information.
 *
 * @implSpec
 * This class is immutable and thread-safe.
 *
 * @since Java 1.8
 * @author Wu Yuping
 */
class DecimalStyle private constructor(
    /**
     * The zero digit.
     */
    private val zeroDigit: Char,

    /**
     * The positive sign.
     */
    private val positiveSign: Char,

    /**
     * The negative sign.
     */
    private val negativeSign: Char,

    /**
     * The decimal separator.
     */
    private val decimalSeparator: Char
) {
    companion object {
        /**
         * The standard set of non-localized decimal style symbols.
         *
         *
         * This uses standard ASCII characters for zero, positive, negative and a dot for the decimal point.
         */
        val STANDARD = DecimalStyle('0', '+', '-', '.')
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the character that represents zero.
     *
     *
     * The character used to represent digits may vary by culture.
     * This method specifies the zero character to use, which implies the characters for one to nine.
     *
     * @return the character for zero
     */
    fun getZeroDigit(): Char {
        return zeroDigit
    }

    /**
     * Returns a copy of the info with a new character that represents zero.
     *
     *
     * The character used to represent digits may vary by culture.
     * This method specifies the zero character to use, which implies the characters for one to nine.
     *
     * @param zeroDigit  the character for zero
     * @return  a copy with a new character that represents zero, not null
     */
    fun withZeroDigit(zeroDigit: Char): DecimalStyle {
        return if (zeroDigit == this.zeroDigit) {
            this
        } else DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator)
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the character that represents the positive sign.
     *
     *
     * The character used to represent a positive number may vary by culture.
     * This method specifies the character to use.
     *
     * @return the character for the positive sign
     */
    fun getPositiveSign(): Char {
        return positiveSign
    }

    /**
     * Returns a copy of the info with a new character that represents the positive sign.
     *
     *
     * The character used to represent a positive number may vary by culture.
     * This method specifies the character to use.
     *
     * @param positiveSign  the character for the positive sign
     * @return  a copy with a new character that represents the positive sign, not null
     */
    fun withPositiveSign(positiveSign: Char): DecimalStyle {
        return if (positiveSign == this.positiveSign) {
            this
        } else DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator)
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the character that represents the negative sign.
     *
     *
     * The character used to represent a negative number may vary by culture.
     * This method specifies the character to use.
     *
     * @return the character for the negative sign
     */
    fun getNegativeSign(): Char {
        return negativeSign
    }

    /**
     * Returns a copy of the info with a new character that represents the negative sign.
     *
     *
     * The character used to represent a negative number may vary by culture.
     * This method specifies the character to use.
     *
     * @param negativeSign  the character for the negative sign
     * @return  a copy with a new character that represents the negative sign, not null
     */
    fun withNegativeSign(negativeSign: Char): DecimalStyle {
        return if (negativeSign == this.negativeSign) {
            this
        } else DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator)
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the character that represents the decimal point.
     *
     *
     * The character used to represent a decimal point may vary by culture.
     * This method specifies the character to use.
     *
     * @return the character for the decimal point
     */
    fun getDecimalSeparator(): Char {
        return decimalSeparator
    }

    /**
     * Returns a copy of the info with a new character that represents the decimal point.
     *
     *
     * The character used to represent a decimal point may vary by culture.
     * This method specifies the character to use.
     *
     * @param decimalSeparator  the character for the decimal point
     * @return  a copy with a new character that represents the decimal point, not null
     */
    fun withDecimalSeparator(decimalSeparator: Char): DecimalStyle {
        return if (decimalSeparator == this.decimalSeparator) {
            this
        } else DecimalStyle(zeroDigit, positiveSign, negativeSign, decimalSeparator)
    }

    //-----------------------------------------------------------------------
    /**
     * Checks whether the character is a digit, based on the currently set zero character.
     *
     * @param ch  the character to check
     * @return the value, 0 to 9, of the character, or -1 if not a digit
     */
    fun convertToDigit(ch: Char): Int {
        val value = ch - this.zeroDigit
        return if (value in 0..9) value else -1
    }

    /**
     * Converts the input numeric text to the internationalized form using the zero character.
     *
     * @param numericText  the text, consisting of digits 0 to 9, to convert, not null
     * @return the internationalized text, not null
     */
    fun convertNumberToI18N(numericText: String): String {
        if (this.zeroDigit == '0') {
            return numericText
        }
        val diff = this.zeroDigit - '0'
        val array = numericText.toList().toCharArray()
        for (i in array.indices) {
            array[i] = (array[i].toInt() + diff).toChar()
        }
        return String(array)
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this DecimalStyle is equal to another DecimalStyle.
     *
     * @param other  the object to check, null returns false
     * @return true if this is equal to the other date
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is DecimalStyle) {
            return this.zeroDigit == other.zeroDigit && this.positiveSign == other.positiveSign &&
                    this.negativeSign == other.negativeSign && this.decimalSeparator == other.decimalSeparator
        }
        return false
    }

    /**
     * A hash code for this DecimalStyle.
     *
     * @return a suitable hash code
     */
    override fun hashCode(): Int {
        return this.zeroDigit.toInt() + this.positiveSign.toInt() +
                this.negativeSign.toInt() + this.decimalSeparator.toInt()
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string describing this DecimalStyle.
     *
     * @return a string description, not null
     */
    override fun toString(): String {
        return "DecimalStyle[$zeroDigit$positiveSign$negativeSign$decimalSeparator]"
    }
}