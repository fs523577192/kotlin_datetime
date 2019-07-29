package org.firas.datetime.util

import org.firas.util.Locale
import kotlin.js.JsName
import kotlin.jvm.JvmStatic

/**
 *
 * @author Wu Yuping
 */
class TimeZoneNameUtility {

    companion object {

        /**
         * Retrieve display names for a time zone ID.
         */
        @JvmStatic
        @JsName("retrieveDisplayNames")
        fun retrieveDisplayNames(id: String, locale: Locale): Array<String>? {
            return arrayOf()
        }

        /**
         * Retrieves a generic time zone display name for a time zone ID.
         *
         * @param id     time zone ID
         * @param style  TimeZone.LONG or TimeZone.SHORT
         * @param locale desired Locale
         * @return the requested generic time zone display name, or null if not found.
         */
        @JvmStatic
        @JsName("retrieveGenericDisplayName")
        fun retrieveGenericDisplayName(id: String, style: Int, locale: Locale): String? {
            val names = retrieveDisplayNames(id, locale)
            return if (null == names) null else names[6 - style]
        }

        /**
         * get time zone localized strings. Enumerate all keys.
         */
        @JvmStatic
        @JsName("getZoneStrings")
        fun getZoneStrings(locale: Locale): Array<Array<String>>? {
            return arrayOf()
        }
    } // companion object
}