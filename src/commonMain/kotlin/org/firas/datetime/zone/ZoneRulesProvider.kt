/*
 * Copyright (c) 2012, 2016, Oracle and/or its affiliates. All rights reserved.
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
 * Copyright (c) 2009-2012, Stephen Colebourne & Michael Nascimento Santos
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

import org.firas.datetime.LocalDateTime

/**
 * Provider of time-zone rules to the system.
 *
 *
 * This class manages the configuration of time-zone rules.
 * The static methods provide the public API that can be used to manage the providers.
 * The abstract methods provide the SPI that allows rules to be provided.
 *
 *
 * ZoneRulesProvider may be installed in an instance of the Java Platform as
 * extension classes, that is, jar files placed into any of the usual extension
 * directories. Installed providers are loaded using the service-provider loading
 * facility defined by the {@link ServiceLoader} class. A ZoneRulesProvider
 * identifies itself with a provider configuration file named
 * {@code java.time.zone.ZoneRulesProvider} in the resource directory
 * {@code META-INF/services}. The file should contain a line that specifies the
 * fully qualified concrete zonerules-provider class name.
 * Providers may also be made available by adding them to the class path or by
 * registering themselves via {@link #registerProvider} method.
 *
 *
 * The Java virtual machine has a default provider that provides zone rules
 * for the time-zones defined by IANA Time Zone Database (TZDB). If the system
 * property {@code java.time.zone.DefaultZoneRulesProvider} is defined then
 * it is taken to be the fully-qualified name of a concrete ZoneRulesProvider
 * class to be loaded as the default provider, using the system class loader.
 * If this system property is not defined, a system-default provider will be
 * loaded to serve as the default provider.
 *
 *
 * Rules are looked up primarily by zone ID, as used by {@link ZoneId}.
 * Only zone region IDs may be used, zone offset IDs are not used here.
 *
 *
 * Time-zone rules are political, thus the data can change at any time.
 * Each provider will provide the latest rules for each zone ID, but they
 * may also provide the history of how the rules changed.
 *
 * @implSpec
 * This interface is a service provider that can be called by multiple threads.
 * Implementations must be immutable and thread-safe.
 *
 *
 * Providers must ensure that once a rule has been seen by the application, the
 * rule must continue to be available.
 *
 *
 * Providers are encouraged to implement a meaningful {@code toString} method.
 *
 *
 * Many systems would like to update time-zone rules dynamically without stopping the JVM.
 * When examined in detail, this is a complex problem.
 * Providers may choose to handle dynamic updates, however the default provider does not.
 *
 * @since Java 1.8
 * @author Wu Yuping (migrate to Kotlin)
 */
open class ZoneRulesProvider {

    companion object {
        /**
         * Gets the rules for the zone ID.
         *
         *
         * This returns the latest available rules for the zone ID.
         *
         *
         * This method relies on time-zone data provider files that are configured.
         * These are loaded using a `ServiceLoader`.
         *
         *
         * The caching flag is designed to allow provider implementations to
         * prevent the rules being cached in `ZoneId`.
         * Under normal circumstances, the caching of zone rules is highly desirable
         * as it will provide greater performance. However, there is a use case where
         * the caching would not be desirable, see [.provideRules].
         *
         * @param zoneId the zone ID as defined by `ZoneId`, not null
         * @param forCaching whether the rules are being queried for caching,
         * true if the returned rules will be cached by `ZoneId`,
         * false if they will be returned to the user without being cached in `ZoneId`
         * @return the rules, null if `forCaching` is true and this
         * is a dynamic provider that wants to prevent caching in `ZoneId`,
         * otherwise not null
         * @throws ZoneRulesException if rules cannot be obtained for the zone ID
         */
        fun getRules(zoneId: String, forCaching: Boolean): ZoneRules {
            return getProvider(zoneId).provideRules(zoneId, forCaching)
        }

        fun getAvailableZoneIds(): Set<String> {
            return ZONES.keys
        }

        /**
         * Gets the provider for the zone ID.
         *
         * @param zoneId  the zone ID as defined by {@code ZoneId}, not null
         * @return the provider, not null
         * @throws ZoneRulesException if the zone ID is unknown
         */
        private fun getProvider(zoneId: String): ZoneRulesProvider {
            val provider: ZoneRulesProvider? = ZONES.get(zoneId)
            if (provider == null) {
                if (ZONES.isEmpty()) {
                    throw ZoneRulesException("No time-zone data files registered")
                }
                throw ZoneRulesException("Unknown time-zone ID: $zoneId")
            }
            return provider
        }

        init {
            val instance = ZoneRulesProvider()
            ZONES.put("PRC", instance)
            ZONES.put("Singapore", instance)
            ZONES.put("Hongkong", instance)
            ZONES.put("Etc/GMT+8", instance)
            ZONES.put("Asia/Shanghai", instance)
            ZONES.put("Asia/Chongqing", instance)
            ZONES.put("Asia/Chungking", instance)
            ZONES.put("Asia/Asia/Urumqi", instance)
            ZONES.put("Asia/Hong_Kong", instance)
            ZONES.put("Asia/Singapore", instance)
        }
    }

    /**
     * SPI method to get the rules for the zone ID.
     *
     *
     * This loads the rules for the specified zone ID.
     * The provider implementation must validate that the zone ID is valid and
     * available, throwing a `ZoneRulesException` if it is not.
     * The result of the method in the valid case depends on the caching flag.
     *
     *
     * If the provider implementation is not dynamic, then the result of the
     * method must be the non-null set of rules selected by the ID.
     *
     *
     * If the provider implementation is dynamic, then the flag gives the option
     * of preventing the returned rules from being cached in [ZoneId].
     * When the flag is true, the provider is permitted to return null, where
     * null will prevent the rules from being cached in `ZoneId`.
     * When the flag is false, the provider must return non-null rules.
     *
     * @param zoneId the zone ID as defined by `ZoneId`, not null
     * @param forCaching whether the rules are being queried for caching,
     * true if the returned rules will be cached by `ZoneId`,
     * false if they will be returned to the user without being cached in `ZoneId`
     * @return the rules, null if `forCaching` is true and this
     * is a dynamic provider that wants to prevent caching in `ZoneId`,
     * otherwise not null
     * @throws ZoneRulesException if rules cannot be obtained for the zone ID
     */
    protected open fun provideRules(zoneId: String, forCaching: Boolean): ZoneRules {
        if (zoneId == "PRC" || zoneId == "Asia/Shanghai") {
            return ZoneRules(offsetPlus8, offsetPlus8, transitionListPRC, emptyTransitionList, listOf())
        } else if (zoneId == "Asia/Hong_Kong" || zoneId == "Hongkong") {
            // TODO: daylight saving
            return ZoneRules(offsetPlus8, offsetPlus8, emptyTransitionList, emptyTransitionList, listOf())
        } else if (zoneId == "Asia/Chongqing" || zoneId == "Asia/Chungking") {
            // TODO: offsetPlus7?
            return ZoneRules(offsetPlus8, offsetPlus8, transitionListPRC, emptyTransitionList, listOf())
        } else if (zoneId == "Asia/Asia/Urumqi") {
            // TODO: offsetPlus6?
            return ZoneRules(offsetPlus8, offsetPlus8, transitionListPRC, emptyTransitionList, listOf())
        } else if (zoneId == "Asia/Singapore" || zoneId == "Singapore" || zoneId == "Etc/GMT+8") {
            return ZoneRules(offsetPlus8, offsetPlus8, emptyTransitionList, emptyTransitionList, listOf())
        }
        throw ZoneRulesException("Unsupported time-zone ID: $zoneId")
    }
    private val offsetPlus6 = ZoneOffset.ofHours(6)
    private val offsetPlus7 = ZoneOffset.ofHours(7)
    private val offsetPlus8 = ZoneOffset.ofHours(8)
    private val offsetPlus9 = ZoneOffset.ofHours(9)
    private val emptyTransitionList = listOf<ZoneOffsetTransition>()
    private val transitionListPRC = listOf(
        ZoneOffsetTransition(LocalDateTime.of(1986, 5, 4, 2, 0), offsetPlus8, offsetPlus9),
        ZoneOffsetTransition(LocalDateTime.of(1986, 9, 14, 2, 0), offsetPlus9, offsetPlus8),
        ZoneOffsetTransition(LocalDateTime.of(1987, 4, 12, 2, 0), offsetPlus8, offsetPlus9),
        ZoneOffsetTransition(LocalDateTime.of(1987, 9, 13, 2, 0), offsetPlus9, offsetPlus8),
        ZoneOffsetTransition(LocalDateTime.of(1988, 4, 10, 2, 0), offsetPlus8, offsetPlus9),
        ZoneOffsetTransition(LocalDateTime.of(1988, 9, 11, 2, 0), offsetPlus9, offsetPlus8),
        ZoneOffsetTransition(LocalDateTime.of(1989, 4, 16, 2, 0), offsetPlus8, offsetPlus9),
        ZoneOffsetTransition(LocalDateTime.of(1989, 9, 17, 2, 0), offsetPlus9, offsetPlus8),
        ZoneOffsetTransition(LocalDateTime.of(1990, 4, 15, 2, 0), offsetPlus8, offsetPlus9),
        ZoneOffsetTransition(LocalDateTime.of(1990, 9, 16, 2, 0), offsetPlus9, offsetPlus8),
        ZoneOffsetTransition(LocalDateTime.of(1991, 4, 14, 2, 0), offsetPlus8, offsetPlus9),
        ZoneOffsetTransition(LocalDateTime.of(1991, 9, 15, 2, 0), offsetPlus9, offsetPlus8)
    )
}

/**
 * The lookup from zone ID to provider.
 */
expect internal val ZONES: MutableMap<String, ZoneRulesProvider>