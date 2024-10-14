/*
 *
 *  *  Copyright 2024 The Hyve
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

package org.radarbase.datadashboard.api.resource

import jakarta.ws.rs.core.Application
import kotlinx.coroutines.runBlocking
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.test.JerseyTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.radarbase.datadashboard.api.api.ObservationListDto
import org.radarbase.datadashboard.api.domain.mapper.toDto
import org.radarbase.datadashboard.api.domain.model.Observation
import org.radarbase.datadashboard.api.service.ObservationService
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.upload.mock.MockAsyncCoroutineService
import java.time.ZonedDateTime

class ObservationResourceTest : JerseyTest() {

    @Mock
    lateinit var observationService: ObservationService

    private lateinit var observationListDto: ObservationListDto
    private val projectId = "project-1"
    private val subjectId = "sub-1"
    private val topicId = "topic-1"

    override fun configure(): Application {
        // Initialize all defined Mockito mocks (with @Mock annotation).
        MockitoAnnotations.openMocks(this)
        // Configure the Jersey Application.
        val resourceConfig = ResourceConfig(ObservationResource::class.java)
        // Register the ObservationService mock for dependency injection (needed by ObservationResource).
        resourceConfig.register(object : AbstractBinder() {
            override fun configure() {
                bind(observationService).to(ObservationService::class.java)
                bind(MockAsyncCoroutineService()).to(AsyncCoroutineService::class.java)
            }
        })
        return resourceConfig
    }

    @BeforeEach
    fun init() {
        // Create some fake observations that are returned by the service.
        val observations: List<Observation> = listOf(createObservation(), createObservation(), createObservation(), createObservation())
        // Create Dto that should be returned by the ObservationService.
        observationListDto = ObservationListDto(
            observations.map { it.toDto() },
        )
    }

    @Test
    fun testGetObservations() = runBlocking {
        // Instruct the mock to return the fake observations when called.
        `when`(observationService.getObservations(projectId = projectId, subjectId = subjectId, topicId = topicId)).thenReturn(observationListDto)
        // Make the call to the REST endpoint.
        target("project/project-1/subject/sub-1/topic/topic-1/observations")
            .request()
            .get()
            .use { response ->
                // Expect the http response to be OK and the same as the expected DTO.
                assertEquals(200, response.status)
                assertEquals(observationListDto, response.readEntity(ObservationListDto::class.java))
            }
    }

    @Test
    fun testGetObservations_failNoSubjectId() = runBlocking {
        target("project/project-1/subject//topic/topic-1/observations")
            .request()
            .get()
            .use { response ->
                assertEquals(404, response.status)
            }
    }

    @Test
    fun testGetObservations_failNoTopicId() = runBlocking {
        target("project/project-1/subject/sub-1/topic//observations")
            .request()
            .get()
            .use { response ->
                assertEquals(404, response.status)
            }
    }

    @Test
    fun testGetObservations_failNoProjectId() = runBlocking {
        target("project//subject/sub-1/topic/topic-1/observations")
            .request()
            .get()
            .use { response ->
                assertEquals(404, response.status)
            }
    }

    private fun createObservation(): Observation {
        return Observation(
            project = "project-1",
            subject = subjectId,
            source = "source-1",
            topic = "topic-1",
            category = "category-1",
            variable = "variable-1",
            observationTime = ZonedDateTime.now(),
            observationTimeEnd = null,
            type = "STRING",
            valueTextual = "value1",
            valueNumeric = null,
        )
    }
}
