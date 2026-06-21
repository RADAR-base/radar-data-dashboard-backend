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

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.radarbase.datadashboard.backend.api.ObservationListDto
import org.radarbase.datadashboard.backend.domain.ObservationRepositoryImpl
import org.radarbase.datadashboard.backend.domain.mapper.toDto
import org.radarbase.datadashboard.backend.util.ObservationType
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.category
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.createObservation
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.projectId
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.subjectId
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.topicId
import org.radarbase.datadashboard.backend.domain.model.Observation
import org.radarbase.datadashboard.backend.service.ObservationServiceImpl

class ObservationServiceImplTest {

    // Create a Mockito mock of the ObservationRepository. This is instantiated in the init block.
    @Mock
    private lateinit var observationRepository: ObservationRepositoryImpl

    var observations: List<Observation>
    private val observationService: ObservationServiceImpl

    init {
        // Initialize all Mockito mocks.
        MockitoAnnotations.openMocks(this)
        observationService = ObservationServiceImpl(observationRepository)
        observations =
            listOf(
                createObservation(ObservationType.INTEGER),
                createObservation(ObservationType.INTEGER),
                createObservation(ObservationType.INTEGER),
                createObservation(ObservationType.INTEGER),
            )
        val numbers = listOf(2.0, 4.0, 6.0)
        observationRepository.stub {
            onBlocking {
                observationRepository.getObservations(
                    projectId = projectId,
                    subjectId = subjectId,
                    topicId = topicId,
                    since = null,
                    until = null,
                )
            }.doReturn(observations)
            onBlocking {
                observationRepository.getNumericValues(
                    projectId = projectId,
                    subjectId = subjectId,
                    topicId = topicId,
                    category = category,
                    variable = "numeric-variable",
                    since = null,
                    until = null,
                )
            }.doReturn(numbers)
        }
    }

    /** This test does not test much (only whether the service calls the repository).
     *  I made it mainly to document how to write a test with mocking.
     * */
    @Test
    fun test_getObservations1() = runBlocking {
        // Call the ObservationService (class under test) to get the observations.
        val result = observationService.getObservations(
            projectId = projectId, subjectId = subjectId, topicId = topicId, since = null, until = null
        )

        // Check if the result is as expected (observations transformed to ObservationListDto).
        val expectedDto = ObservationListDto(
            observations.map { it.toDto() },
        )
        assertEquals(expectedDto, result)
    }

    @Test
    fun testCalculate() = runBlocking {
        val result = observationService.calculateValueByCategoryAndVariable(
            projectId = projectId,
            subjectId = subjectId,
            topicId = topicId,
            category = category,
            variable = "numeric-variable"
        ) { it.maxOrNull() }
        assert(result == 6.0)
    }

}
