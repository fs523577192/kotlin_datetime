package org.firas.datetime.temporal

/**
 *
 * @author Wu Yuping
 */
actual fun TemporalAccessor.getClassName(): String {
    return this::class.qualifiedName!!
}