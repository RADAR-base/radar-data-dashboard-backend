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

package org.radarbase.datadashboard.api.domain

import jakarta.inject.Provider
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.radarbase.datadashboard.api.util.MockAsyncCoroutineService

class ObservationRepositoryImplTest : RepositoryTest() {
    private lateinit var repository: ObservationRepositoryImpl

    @BeforeEach
    fun setUpRepository() {
        val emProvider = Provider { em }
        val asyncService = MockAsyncCoroutineService()
        repository = ObservationRepositoryImpl(emProvider, asyncService)
    }

    @Test
    fun testGetObservations() = runBlocking {
        val observations = repository.getObservations(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            since = null,
            until = null
        )

        assertEquals(2, observations.size)
        val variables = observations.map { it.variable }.toSet()
        assertTrue(variables.contains("Perceived_Pain_Score"))
        assertTrue(variables.contains("Name_Of_Physician"))
    }

    @Test
    fun testGetObservationsWithFilter() = runBlocking {
        val observations = repository.getObservations(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            category = "baseline_questions",
            variable = "Perceived_Pain_Score",
            since = null,
            until = null
        )

        assertEquals(1, observations.size)
        assertEquals("Perceived_Pain_Score", observations[0].variable)
        assertEquals(5.0, observations[0].valueNumeric)
    }

    @Test
    fun testGetNumericValues() = runBlocking {
        val values = repository.getNumericValues(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            category = "baseline_questions",
            variable = "Perceived_Pain_Score",
            since = null,
            until = null
        )

        assertEquals(1, values.size)
        assertEquals(5.0, values[0])
    }

    @Test
    fun testGetTextValues() = runBlocking {
        val values = repository.getTextValues(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            category = "followup_questions",
            variable = "Name_Of_Physician",
            since = null,
            until = null
        )

        assertEquals(1, values.size)
        assertEquals("Dr.J.Adams", values[0])
    }
}
