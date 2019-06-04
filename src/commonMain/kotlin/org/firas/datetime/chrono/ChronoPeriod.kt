package org.firas.datetime.chrono

import org.firas.datetime.temporal.Temporal
import org.firas.datetime.temporal.TemporalAmount
import org.firas.datetime.temporal.TemporalUnit
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 * A date-based amount of time, such as '3 years, 4 months and 5 days' in an
 * arbitrary chronology, intended for advanced globalization use cases.
 *
 *
 * This interface models a date-based amount of time in a calendar system.
 * While most calendar systems use years, months and days, some do not.
 * Therefore, this interface operates solely in terms of a set of supported
 * units that are defined by the `Chronology`.
 * The set of supported units is fixed for a given chronology.
 * The amount of a supported unit may be set to zero.
 *
 *
 * The period is modeled as a directed amount of time, meaning that individual
 * parts of the period may be negative.
 *
 * @implSpec
 * This interface must be implemented with care to ensure other classes operate correctly.
 * All implementations that can be instantiated must be final, immutable and thread-safe.
 * Subclasses should be Serializable wherever possible.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
interface ChronoPeriod: TemporalAmount {

    companion object {
        /**
         * Obtains a `ChronoPeriod` consisting of amount of time between two dates.
         *
         *
         * The start date is included, but the end date is not.
         * The period is calculated using [ChronoLocalDate.until].
         * As such, the calculation is chronology specific.
         *
         *
         * The chronology of the first date is used.
         * The chronology of the second date is ignored, with the date being converted
         * to the target chronology system before the calculation starts.
         *
         *
         * The result of this method can be a negative period if the end is before the start.
         * In most cases, the positive/negative sign will be the same in each of the supported fields.
         *
         * @param startDateInclusive  the start date, inclusive, specifying the chronology of the calculation, not null
         * @param endDateExclusive  the end date, exclusive, in any chronology, not null
         * @return the period between this date and the end date, not null
         * @see ChronoLocalDate.until
         */
        @JsName("between")
        @JvmStatic
        fun between(startDateInclusive: ChronoLocalDate, endDateExclusive: ChronoLocalDate): ChronoPeriod {
            TODO()
            // return startDateInclusive.until(endDateExclusive)
        }
    }

    /**
     * Gets the value of the requested unit.
     *
     *
     * The supported units are chronology specific.
     * They will typically be [YEARS][ChronoUnit.YEARS],
     * [MONTHS][ChronoUnit.MONTHS] and [DAYS][ChronoUnit.DAYS].
     * Requesting an unsupported unit will throw an exception.
     *
     * @param unit the `TemporalUnit` for which to return the value
     * @return the long value of the unit
     * @throws DateTimeException if the unit is not supported
     * @throws UnsupportedTemporalTypeException if the unit is not supported
     */
    override operator fun get(unit: TemporalUnit): Long

    /**
     * Gets the set of units supported by this period.
     *
     *
     * The supported units are chronology specific.
     * They will typically be [YEARS][ChronoUnit.YEARS],
     * [MONTHS][ChronoUnit.MONTHS] and [DAYS][ChronoUnit.DAYS].
     * They are returned in order from largest to smallest.
     *
     *
     * This set can be used in conjunction with [.get]
     * to access the entire state of the period.
     *
     * @return a list containing the supported units, not null
     */
    override fun getUnits(): List<TemporalUnit>

    /**
     * Gets the chronology that defines the meaning of the supported units.
     *
     *
     * The period is defined by the chronology.
     * It controls the supported units and restricts addition/subtraction
     * to `ChronoLocalDate` instances of the same chronology.
     *
     * @return the chronology defining the period, not null
     */
    @JsName("getChronology")
    fun getChronology(): Chronology

    //-----------------------------------------------------------------------
    /**
     * Checks if all the supported units of this period are zero.
     *
     * @return true if this period is zero-length
     */
    @JsName("isZero")
    fun isZero(): Boolean {
        for (unit in getUnits()) {
            if (get(unit) != 0L) {
                return false
            }
        }
        return true
    }

    /**
     * Checks if any of the supported units of this period are negative.
     *
     * @return true if any unit of this period is negative
     */
    @JsName("isNegative")
    fun isNegative(): Boolean {
        for (unit in getUnits()) {
            if (get(unit) < 0) {
                return true
            }
        }
        return false
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this period with the specified period added.
     *
     *
     * If the specified amount is a `ChronoPeriod` then it must have
     * the same chronology as this period. Implementations may choose to
     * accept or reject other `TemporalAmount` implementations.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToAdd  the period to add, not null
     * @return a `ChronoPeriod` based on this period with the requested period added, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    operator fun plus(amountToAdd: TemporalAmount): ChronoPeriod

    /**
     * Returns a copy of this period with the specified period subtracted.
     *
     *
     * If the specified amount is a `ChronoPeriod` then it must have
     * the same chronology as this period. Implementations may choose to
     * accept or reject other `TemporalAmount` implementations.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param amountToSubtract  the period to subtract, not null
     * @return a `ChronoPeriod` based on this period with the requested period subtracted, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    operator fun minus(amountToSubtract: TemporalAmount): ChronoPeriod

    //-----------------------------------------------------------------------
    /**
     * Returns a new instance with each amount in this period in this period
     * multiplied by the specified scalar.
     *
     *
     * This returns a period with each supported unit individually multiplied.
     * For example, a period of "2 years, -3 months and 4 days" multiplied by
     * 3 will return "6 years, -9 months and 12 days".
     * No normalization is performed.
     *
     * @param scalar  the scalar to multiply by, not null
     * @return a `ChronoPeriod` based on this period with the amounts multiplied
     * by the scalar, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    @JsName("multipliedBy")
    fun multipliedBy(scalar: Int): ChronoPeriod

    /**
     * Returns a new instance with each amount in this period negated.
     *
     *
     * This returns a period with each supported unit individually negated.
     * For example, a period of "2 years, -3 months and 4 days" will be
     * negated to "-2 years, 3 months and -4 days".
     * No normalization is performed.
     *
     * @return a `ChronoPeriod` based on this period with the amounts negated, not null
     * @throws ArithmeticException if numeric overflow occurs, which only happens if
     * one of the units has the value `Long.MIN_VALUE`
     */
    @JsName("negated")
    fun negated(): ChronoPeriod {
        return multipliedBy(-1)
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a copy of this period with the amounts of each unit normalized.
     *
     *
     * The process of normalization is specific to each calendar system.
     * For example, in the ISO calendar system, the years and months are
     * normalized but the days are not, such that "15 months" would be
     * normalized to "1 year and 3 months".
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @return a `ChronoPeriod` based on this period with the amounts of each
     * unit normalized, not null
     * @throws ArithmeticException if numeric overflow occurs
     */
    @JsName("normalized")
    fun normalized(): ChronoPeriod

    //-------------------------------------------------------------------------
    /**
     * Adds this period to the specified temporal object.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with this period added.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * [Temporal.plus].
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * dateTime = thisPeriod.addTo(dateTime);
     * dateTime = dateTime.plus(thisPeriod);
     * </pre>
     *
     *
     * The specified temporal must have the same chronology as this period.
     * This returns a temporal with the non-zero supported units added.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param temporal  the temporal object to adjust, not null
     * @return an object of the same type with the adjustment made, not null
     * @throws DateTimeException if unable to add
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun addTo(temporal: Temporal): Temporal

    /**
     * Subtracts this period from the specified temporal object.
     *
     *
     * This returns a temporal object of the same observable type as the input
     * with this period subtracted.
     *
     *
     * In most cases, it is clearer to reverse the calling pattern by using
     * [Temporal.minus].
     * <pre>
     * // these two lines are equivalent, but the second approach is recommended
     * dateTime = thisPeriod.subtractFrom(dateTime);
     * dateTime = dateTime.minus(thisPeriod);
     * </pre>
     *
     *
     * The specified temporal must have the same chronology as this period.
     * This returns a temporal with the non-zero supported units subtracted.
     *
     *
     * This instance is immutable and unaffected by this method call.
     *
     * @param temporal  the temporal object to adjust, not null
     * @return an object of the same type with the adjustment made, not null
     * @throws DateTimeException if unable to subtract
     * @throws ArithmeticException if numeric overflow occurs
     */
    override fun subtractFrom(temporal: Temporal): Temporal
}