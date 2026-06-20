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

package org.radarbase.datadashboard.domain

import org.radarbase.datadashboard.domain.model.Observation
import java.time.Instant

interface ObservationRepository {
    suspend fun getObservations(
        projectId: String,
        subjectId: String,
        topicId: String,
        since: Instant? = null,
        until: Instant? = null,
    ): List<Observation>

    suspend fun getObservations(
        projectId: String,
        subjectId: String,
        topicId: String,
        category: String? = null,
        variable: String,
        since: Instant? = null,
        until: Instant? = null,
    ): List<Observation>

    suspend fun getVariableType(topicId: String, category: String? = null, variable: String): String?
    suspend fun getNumericVariableTypes(): Map<String,Boolean>

    suspend fun getNumericValues(
        projectId: String,
        subjectId: String,
        topicId: String,
        category: String? = null,
        variable: String,
        since: Instant? = null,
        until: Instant? = null,
    ): List<Double>

    suspend fun getTextValues(
        projectId: String,
        subjectId: String,
        topicId: String,
        category: String? = null,
        variable: String,
        since: Instant? = null,
        until: Instant? = null,
    ): List<String>
}
