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

package org.radarbase.datadashboard.service

import jakarta.ws.rs.core.Context
import org.radarbase.datadashboard.config.VariableTypeCacheConfig
import org.radarbase.datadashboard.domain.ObservationRepository
import org.radarbase.datadashboard.util.cacheKey
import org.radarbase.kotlin.coroutines.CacheConfig
import org.radarbase.kotlin.coroutines.CachedMap
import kotlin.time.Duration.Companion.seconds


class ObservationTypeServiceImpl(
    @Context private val observationRepository: ObservationRepository,
    @Context private val config: VariableTypeCacheConfig,
) : ObservationTypeService {

    // All values of observations are submitted to Kafka as string. Type is inferred independently for each observation
    // value (check whether the string can be parsed as a number). As a result, a variable may 'develop' numeric values
    // over time as more data is submitted. This cache is refreshed periodically to account for this.
    private val numericVariableCache = CachedMap(
        CacheConfig(refreshDuration = config.refreshDurationSec.seconds)
    ) {
        observationRepository.getNumericVariableTypes()
    }

    /**
     * <p>Determine whether the variable type of observation is numeric.</p>
     * @param topic Name of the kafka topic that contains the observation (e.g., questionnaire_response)
     * @param category Category name of the observation (e.g., baseline_questions)
     * @param variable Variable name of the observation (e.g., Perceived_Pain_Score)
     * @return null when the variable type is unknown (no observations in the database at the moment of the request)
     * @return false when the variable has observations at the moment of the request that can be cast to numeric
     * @return true when the variable has no observations at the moment of the request that can be cast to numeric
     */
    override suspend fun hasNumericValues(topic: String, category: String?, variable: String): Boolean? {
        return numericVariableCache.get(cacheKey(topic, category, variable))
    }

}
