package org.firas.datetime

/**
 *
 * @author Wu Yuping
 */
actual class Date: Comparable<Date> {
    private var date: kotlin.js.Date

    @JsName("Date_init")
    actual constructor() {
        this.date = kotlin.js.Date()
    }

    @JsName("Date_initWithMilliseconds")
    actual constructor(milliseconds: Long) {
        this.date = kotlin.js.Date(milliseconds)
    }

    @JsName("getTime")
    actual fun getTime(): Long {
        return this.date.getTime().toLong()
    }

    @JsName("setTime")
    actual fun setTime(milliseconds: Long) {
        this.date = kotlin.js.Date(milliseconds)
    }

    @JsName("getTimezoneOffset")
    actual fun getTimezoneOffset(): Int {
        return this.date.getTimezoneOffset()
    }

    override fun compareTo(other: Date): Int {
        val a = this.getTime()
        val b = other.getTime()
        return if (a > b) 1 else if (a < b) -1 else 0
    }
}