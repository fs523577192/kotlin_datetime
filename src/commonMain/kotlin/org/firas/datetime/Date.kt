package org.firas.datetime

expect class Date {
    constructor()
    constructor(milliseconds: Long)
    fun getTime(): Long
    fun setTime(milliseconds: Long)
}