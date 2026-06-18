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

import jakarta.inject.Singleton
import jakarta.ws.rs.core.Application
import kotlinx.coroutines.runBlocking
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.test.JerseyTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.stub
import org.radarbase.datadashboard.api.api.ObservationListDto
import org.radarbase.datadashboard.api.domain.mapper.toDto
import org.radarbase.datadashboard.api.domain.model.Observation
import org.radarbase.datadashboard.api.resource.paramconverter.InstantParamConverterProvider
import org.radarbase.datadashboard.api.service.ObservationService
import org.radarbase.datadashboard.api.service.ObservationTypeService
import org.radarbase.datadashboard.api.util.MockAsyncCoroutineService
import org.radarbase.datadashboard.api.util.TestUtil.Companion.ObservationType.DOUBLE
import org.radarbase.datadashboard.api.util.TestUtil.Companion.category
import org.radarbase.datadashboard.api.util.TestUtil.Companion.createObservation
import org.radarbase.datadashboard.api.util.TestUtil.Companion.projectId
import org.radarbase.datadashboard.api.util.TestUtil.Companion.subjectId
import org.radarbase.datadashboard.api.util.TestUtil.Companion.topicId
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService

class ObservationResourceTest : JerseyTest() {

    @Mock
    lateinit var observationService: ObservationService

    @Mock
    lateinit var observationTypeService: ObservationTypeService

    private lateinit var observationListDto: ObservationListDto

    class TestResourceEnhancer : JerseyResourceEnhancer {
        override val classes: Array<Class<*>>
            get() = listOfNotNull(
                // Needed to map Instant type in @QueryParam
                InstantParamConverterProvider::class.java,
            ).toTypedArray()

        override val packages: Array<String> = arrayOf(
            "org.radarbase.datadashboard.api.resource",
        )

        override fun org.glassfish.jersey.internal.inject.AbstractBinder.enhance() {
            bind(MockAsyncCoroutineService())
                .to(AsyncCoroutineService::class.java)
                .`in`(Singleton::class.java)
        }
    }

    class TestEnhancerFactory : EnhancerFactory {
        override fun createEnhancers(): List<JerseyResourceEnhancer> = listOf(
            TestResourceEnhancer(),
            Enhancers.mapper,
            Enhancers.exception,
        )
    }

    override fun configure(): Application {
        // Initialize all defined Mockito mocks (with @Mock annotation).
        MockitoAnnotations.openMocks(this)
        // Configure the Jersey Application.
        val resourceConfig = ConfigLoader.loadResources(TestEnhancerFactory::class.java)
        // Register the ObservationService mock for dependency injection (needed by ObservationResource).
        resourceConfig.register(object : AbstractBinder() {
            override fun configure() {
                bind(observationService).to(ObservationService::class.java)
                bind(observationTypeService).to(ObservationTypeService::class.java)
            }
        })
        return resourceConfig
    }

    @BeforeEach
    fun init() {
        // Create some fake observations that are returned by the service.
        val observations: List<Observation> = listOf(
            createObservation(DOUBLE),
            createObservation(DOUBLE),
            createObservation(DOUBLE),
            createObservation(DOUBLE),
        )
        // Create Dto that should be returned by the ObservationService.
        observationListDto = ObservationListDto(
            observations.map { it.toDto() },
        )
        observationService.stub {
            onBlocking {
                // Instruct the mock to return the fake observations when called.
                getObservations(projectId = projectId, subjectId = subjectId, topicId = topicId)
            }.doReturn(observationListDto)
            onBlocking {
                // Instruct the mock to return the fake observations when called.
                getObservations(
                    projectId = projectId,
                    subjectId = subjectId,
                    topicId = topicId,
                    category = category,
                    variable = "numeric-variable"
                )
            }.doReturn(observationListDto)
            onBlocking {
                // Instruct the mock to return the fake observations when called.
                getObservations(
                    projectId = projectId,
                    subjectId = subjectId,
                    topicId = topicId,
                    variable = "numeric-variable"
                )
            }.doReturn(observationListDto)
        }
        observationTypeService.stub {
            onBlocking {
                isNumeric(anyString(), anyString(), eq("text-variable"))
            }.doReturn(false)
            onBlocking {
                isNumeric(anyString(), anyString(), eq("numeric-variable"))
            }.doReturn(true)
        }
    }

    @Test
    fun testGetObservations() = runBlocking {
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
        target("project/$projectId/subject//topic/$topicId/observations")
            .request()
            .get()
            .use { response ->
                assertEquals(404, response.status)
            }
    }

    @Test
    fun testGetObservations_failNoTopicId() = runBlocking {
        target("project/$projectId/subject/$subjectId/topic//observations")
            .request()
            .get()
            .use { response ->
                assertEquals(404, response.status)
            }
    }

    @Test
    fun testGetObservations_failNoProjectId() = runBlocking {
        target("project//subject/$subjectId/topic/$topicId/observations")
            .request()
            .get()
            .use { response ->
                assertEquals(404, response.status)
            }
    }

    @Test
    fun testGetObservationsWithCategoryAndVariable() = runBlocking {
        // Make the call to the REST endpoint.
        target("project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/observations")
            .request()
            .get()
            .use { response ->
                // Expect the http response to be OK and the same as the expected DTO.
                assertEquals(200, response.status)
                assertEquals(observationListDto, response.readEntity(ObservationListDto::class.java))
            }
    }

    @Test
    fun testGetObservationsWithVariable() = runBlocking {
        // Make the call to the REST endpoint.
        target("project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/observations")
            .request()
            .get()
            .use { response ->
                // Expect the http response to be OK and the same as the expected DTO.
                assertEquals(200, response.status)
                assertEquals(observationListDto, response.readEntity(ObservationListDto::class.java))
            }

    }
}
