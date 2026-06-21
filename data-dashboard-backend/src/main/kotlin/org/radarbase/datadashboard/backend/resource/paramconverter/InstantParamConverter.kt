/*
 *
 *  *  Copyright 2026 The Hyve
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package org.radarbase.datadashboard.backend.resource.paramconverter


import jakarta.ws.rs.ext.ParamConverter
import jakarta.ws.rs.ext.ParamConverterProvider
import jakarta.ws.rs.ext.Provider
import java.lang.reflect.Type
import java.time.Instant

@Provider
class InstantParamConverterProvider : ParamConverterProvider {
    override fun <T : Any?> getConverter(
        rawType: Class<T>,
        genericType: Type?,
        annotations: Array<out Annotation>?,
    ): ParamConverter<T>? {
        if (rawType == Instant::class.java) {
            @Suppress("UNCHECKED_CAST")
            return InstantParamConverter as ParamConverter<T>
        }
        return null
    }

    object InstantParamConverter : ParamConverter<Instant> {
        override fun fromString(value: String?): Instant? {
            return value?.let { Instant.parse(it) }
        }

        override fun toString(value: Instant?): String? {
            return value?.toString()
        }
    }
}
