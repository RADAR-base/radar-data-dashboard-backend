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

package org.radarbase.datadashboard.backend.service

import org.radarbase.datadashboard.backend.api.ObservationListDto
import java.time.Instant

interface ObservationService {
    suspend fun getObservations(
        projectId: String,
        subjectId: String,
        topicId: String,
        since: Instant? = null,
        until: Instant? = null,
    ): ObservationListDto

    suspend fun getObservations(
        projectId: String,
        subjectId: String,
        topicId: String,
        category: String? = null,
        variable: String,
        since: Instant? = null,
        until: Instant? = null,
    ): ObservationListDto

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

    suspend fun calculateValueByCategoryAndVariable(
        projectId: String,
        subjectId: String,
        topicId: String,
        category: String? = null,
        variable: String,
        since: Instant? = null,
        until: Instant? = null,
        func: (Iterable<Double>) -> Number?,
    ): Number?

    suspend fun getValues(
        projectId: String,
        subjectId: String,
        topicId: String,
        category: String? = null,
        variable: String,
        since: Instant? = null,
        until: Instant? = null,
    ): List<Any?>
}
