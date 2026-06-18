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

package org.radarbase.datadashboard.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.core.Application
import kotlinx.coroutines.runBlocking
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.test.JerseyTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.radarbase.datadashboard.api.ObservationListDto
import org.radarbase.datadashboard.domain.mapper.toDto
import org.radarbase.datadashboard.domain.model.Observation
import org.radarbase.datadashboard.resource.paramconverter.InstantParamConverterProvider
import org.radarbase.datadashboard.service.ObservationService
import org.radarbase.datadashboard.util.MockAsyncCoroutineService
import org.radarbase.datadashboard.util.ObservationType
import org.radarbase.datadashboard.util.TestUtil.Companion.category
import org.radarbase.datadashboard.util.TestUtil.Companion.createObservation
import org.radarbase.datadashboard.util.TestUtil.Companion.projectId
import org.radarbase.datadashboard.util.TestUtil.Companion.subjectId
import org.radarbase.datadashboard.util.TestUtil.Companion.topicId
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService

class ObservationResourceTest : JerseyTest() {

    @Mock
    lateinit var observationService: ObservationService

    private lateinit var observationListDto: ObservationListDto

    class TestResourceEnhancer : JerseyResourceEnhancer {
        override val classes: Array<Class<*>>
            get() = listOfNotNull(
                // Needed to map Instant type in @QueryParam
                InstantParamConverterProvider::class.java,
            ).toTypedArray()

        override val packages: Array<String> = arrayOf(
            "org.radarbase.datadashboard.resource",
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
            }
        })
        return resourceConfig
    }

    @BeforeEach
    fun init() {
        // Create some fake observations that are returned by the service.
        val observations: List<Observation> = listOf(
            createObservation(ObservationType.DOUBLE),
            createObservation(ObservationType.DOUBLE),
            createObservation(ObservationType.DOUBLE),
            createObservation(ObservationType.DOUBLE),
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
    }

    @ParameterizedTest
    @CsvSource(
        "project/$projectId/subject/$subjectId/topic/$topicId/observations",
        "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/observations",
        "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/observations",
    )
    fun testGetObservations(url: String) = runBlocking {
        // Make the call to the REST endpoint.
        target(url)
            .request()
            .get()
            .use { response ->
                // Expect the http response to be OK and the same as the expected DTO.
                assertEquals(200, response.status)
                assertEquals(observationListDto, response.readEntity(ObservationListDto::class.java))
            }
    }

    @ParameterizedTest
    @CsvSource(
        "project/$projectId/subject//topic/$topicId/observations",
        "project/$projectId/subject//topic/$topicId/category/$category/variable/numeric-variable/observations",
        "project/$projectId/subject//topic/$topicId/variable/numeric-variable/observations",
    )
    fun testGetObservations_failNoSubjectId(url: String) = runBlocking {
        target("project/$projectId/subject//topic/$topicId/observations")
            .request()
            .get()
            .use { response ->
                assertEquals(404, response.status)
            }
    }

    @ParameterizedTest
    @CsvSource(
        "project/$projectId/subject/$subjectId/topic//observations",
        "project/$projectId/subject/$subjectId/topic//category/$category/variable/numeric-variable/observations",
        "project/$projectId/subject/$subjectId/topic//variable/numeric-variable/observations",
    )
    fun testGetObservations_failNoTopicId(url: String) = runBlocking {
        target("project/$projectId/subject/$subjectId/topic//observations")
            .request()
            .get()
            .use { response ->
                assertEquals(404, response.status)
            }
    }

    @ParameterizedTest
    @CsvSource(
        "project//subject/$subjectId/topic/$topicId/observations",
        "project//subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/observations",
        "project//subject/$subjectId/topic/$topicId/variable/numeric-variable/observations",
    )
    fun testGetObservations_failNoProjectId(url: String) = runBlocking {
        target(url)
            .request()
            .get()
            .use { response ->
                assertEquals(404, response.status)
            }
    }
}
