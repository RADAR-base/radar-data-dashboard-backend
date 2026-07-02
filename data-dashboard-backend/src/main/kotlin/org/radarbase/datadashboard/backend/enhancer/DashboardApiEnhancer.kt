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

package org.radarbase.datadashboard.backend.enhancer

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.datadashboard.backend.config.DashboardApiConfig
import org.radarbase.datadashboard.backend.domain.ObservationRepository
import org.radarbase.datadashboard.backend.domain.ObservationRepositoryImpl
import org.radarbase.datadashboard.backend.resource.paramconverter.InstantParamConverterProvider
import org.radarbase.datadashboard.backend.service.ObservationService
import org.radarbase.datadashboard.backend.service.ObservationServiceImpl
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.filter.Filters

class DashboardApiEnhancer(
    private val config: DashboardApiConfig,
) : JerseyResourceEnhancer {
    override val classes = buildList {
        add(Filters.logResponse)
        add(Filters.cache)
        add(InstantParamConverterProvider::class.java)
        if (config.service.enableCors == true) {
            add(Filters.cors)
        }
    }.toTypedArray()

    override val packages: Array<String> = arrayOf(
        "org.radarbase.datadashboard.backend.resource",
    )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(DashboardApiConfig::class.java)

        bind(ObservationServiceImpl::class.java)
            .to(ObservationService::class.java)
            .`in`(Singleton::class.java)

        bind(ObservationRepositoryImpl::class.java)
            .to(ObservationRepository::class.java)
            .`in`(Singleton::class.java)
    }
}
