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
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.radarbase.datadashboard.api.util.MockAsyncCoroutineService
import java.time.Instant

class ObservationRepositoryImplTest : RepositoryTest() {
    private lateinit var repository: ObservationRepositoryImpl

    @BeforeEach
    fun setUpRepository() {
        val emProvider = Provider { em }
        val asyncService = MockAsyncCoroutineService()
        repository = ObservationRepositoryImpl(emProvider, asyncService)
    }

    @Test
    fun testGetObservationsNoSinceAndUntil() = runBlocking {
        val observations = repository.getObservations(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
        )
        assertEquals(4, observations.size)
    }

    @Test
    fun testGetObservationsWithoutCategoryAndVariable() = runBlocking {
        val observations = repository.getObservations(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            since = null,
            until = null,
        )

        assertEquals(4, observations.size)
        val variables = observations.map { it.variable }.toSet()
        assertTrue(variables.contains("Perceived_Pain_Score"))
        assertTrue(variables.contains("Name_Of_Physician"))
    }

    @Test
    fun testGetObservationsWithoutCategoryAndVariableWithSince() = runBlocking {
        val observations = repository.getObservations(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            since = Instant.parse("2021-01-20T12:00:00Z"),
        )

        assertEquals(3, observations.size)
        val variables = observations.map { it.variable }.toSet()
        assertTrue(variables.contains("Name_Of_Physician"))
    }

    @Test
    fun testGetObservationsWithoutCategoryAndVariableWithUntil() = runBlocking {
        val observations = repository.getObservations(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            until = Instant.parse("2021-01-20T12:00:00Z"),
        )

        assertEquals(1, observations.size)
        val variables = observations.map { it.variable }.toSet()
        assertTrue(variables.contains("Perceived_Pain_Score"))
    }

    @Test
    fun testGetObservationsWithCategoryAndVariable() = runBlocking {
        val observations = repository.getObservations(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            category = "baseline_questions",
            variable = "Perceived_Pain_Score",
        )

        assertEquals(3, observations.size)
        assertEquals("Perceived_Pain_Score", observations[0].variable)
        assertEquals(5.0, observations[0].valueNumeric)
    }

    @ParameterizedTest
    @CsvSource(
        "2021-01-01T00:00:00Z, 3",
        "2022-01-01T00:00:00Z, 2",
        "2023-01-01T00:00:00Z, 1",
        "2024-01-01T00:00:00Z, 0",
    )
    fun testGetObservationsWithCategoryAndVariableWithSince(date: Instant, count: Int) = runBlocking {
        val observations = repository.getObservations(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            category = "baseline_questions",
            variable = "Perceived_Pain_Score",
            since = date,
        )
        assertEquals(count, observations.size)
    }

    @ParameterizedTest
    @CsvSource(
        "2021-01-01T00:00:00Z, 0",
        "2022-01-01T00:00:00Z, 1",
        "2023-01-01T00:00:00Z, 2",
        "2024-01-01T00:00:00Z, 3",
    )
    fun testGetObservationsWithCategoryAndVariableWithUntil(date: Instant, count: Int) = runBlocking {
        val observations = repository.getObservations(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            category = "baseline_questions",
            variable = "Perceived_Pain_Score",
            until = date,
        )
        assertEquals(count, observations.size)
    }


    @Test
    fun testGetNumericValues() = runBlocking {
        val values = repository.getNumericValues(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "questionnaire_answer",
            category = "baseline_questions",
            variable = "Perceived_Pain_Score",
        )

        assertEquals(3, values.size)
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
        )

        assertEquals(1, values.size)
        assertEquals("Dr.J.Adams", values[0])
    }

    @Test
    fun testObservationWithNullCategory() = runBlocking {
        val observations = repository.getObservations(
            projectId = "project-1",
            subjectId = "sub-1",
            topicId = "phone_battery_level",
            variable = "batteryLevel",
        )

        assertEquals(1, observations.size)
        assertEquals(5.0, observations[0].valueNumeric)
    }

    @Test
    fun testExceptionNullCategoryInVariableWithCategoryForObservations(): Unit = runBlocking {
        assertThrows<IllegalStateException> {
            repository.getObservations(
                projectId = "project-1",
                subjectId = "sub-1",
                topicId = "questionnaire_answer",
                variable = "Name_Of_Physician",
            )
        }
    }

    @Test
    fun testExceptionNullCategoryInVariableWithCategoryForTypes(): Unit = runBlocking {
        assertThrows<IllegalStateException> {
            repository.getVariableType(
                topicId = "questionnaire_answer",
                variable = "Name_Of_Physician",
            )
        }
    }

}
