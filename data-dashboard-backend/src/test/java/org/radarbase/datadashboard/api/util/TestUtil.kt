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

package org.radarbase.datadashboard.api.util

import org.radarbase.datadashboard.api.domain.model.Observation
import java.time.ZonedDateTime

class TestUtil {

    companion object {
        enum class ObservationType {
            STRING, INTEGER, DOUBLE,
        }

        const val projectId = "project-1"
        const val subjectId = "sub-1"
        const val sourceId = "source-1"
        const val topicId = "topic-1"
        const val category = "category-1"

        fun createObservation(type: ObservationType): Observation {
            return when (type) {
                ObservationType.STRING -> buildObservation("string-variable", "STRING", "my-value", null)
                ObservationType.INTEGER -> buildObservation("integer-variable", "INTEGER", null, 1.0)
                ObservationType.DOUBLE -> buildObservation("double-variable", "DOUBLE", null, 1.0)
            }
        }

        private fun buildObservation(variableName: String, type: String, valueTextual: String?, valueNumeric: Double?) =
            Observation(
                project = projectId,
                subject = subjectId,
                source = sourceId,
                topic = topicId,
                category = category,
                variable = variableName,
                observationTime = ZonedDateTime.now(),
                observationTimeEnd = null,
                type = type,
                valueTextual = valueTextual,
                valueNumeric = valueNumeric,
            )

    }

}
