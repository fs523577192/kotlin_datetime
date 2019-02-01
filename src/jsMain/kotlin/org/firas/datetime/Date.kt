package org.firas.datetime

/**
 *
 * @author Wu Yuping
 */
actual class Date {
    private var date: kotlin.js.Date

    actual constructor() {
        this.date = kotlin.js.Date()
    }

    actual constructor(milliseconds: Long) {
        this.date = kotlin.js.Date(milliseconds)
    }

    actual fun getTime(): Long {
        return this.date.getTime().toLong()
    }

    actual fun setTime(milliseconds: Long) {
        this.date = kotlin.js.Date(milliseconds)
    }
}