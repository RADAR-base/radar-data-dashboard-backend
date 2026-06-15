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

package org.radarbase.datadashboard.api.service

import jakarta.ws.rs.core.Context
import org.radarbase.datadashboard.api.domain.ObservationRepository
import java.util.Locale.getDefault


class ObservationTypeServiceImpl(
    @field:Context private val observationRepository: ObservationRepository,
) : ObservationTypeService {

    val typeCache = mutableMapOf<String, String?>()
    val isNumericCache = mutableMapOf<String, Boolean?>()

    val supplier: suspend (String, String, String) -> String? = { topic: String, category: String, variable: String ->
        observationRepository.getVariableType(topic, category, variable)
    }

    /**
     * <p>Determine whether the variable of an observation is numeric.</p>
     * @param topic Name of the kafka topic that contains the observation (e.g., questionnaire_response)
     * @param category Category name of the observation (e.g., baseline_questions)
     * @param variable Variable name of the observation (e.g., Perceived_Pain_Score)
     * @return null when the variable type is not found
     * @return false when the variable type is not numeric
     * @return true when the variable type is numeric
     */
    override suspend fun isNumeric(topic: String, category: String, variable: String): Boolean? {
        val cacheKey = "${topic}:${category}:${variable}"
        return isNumericCache.getOrPut(cacheKey) {
            typeCache.getOrPut(cacheKey) {
                supplier(topic, category, variable)
            }?.let {
                numericTypes.contains(it.lowercase(getDefault()))
            }
        }
    }

    companion object {
        val numericTypes = listOf("integer", "double")
    }

}
