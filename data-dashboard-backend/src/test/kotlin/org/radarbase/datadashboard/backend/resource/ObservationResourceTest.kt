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

package org.radarbase.datadashboard.backend.resource

import jakarta.inject.Singleton
import jakarta.ws.rs.core.Application
import kotlinx.coroutines.runBlocking
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.test.JerseyTest
import org.glassfish.jersey.test.TestProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.radarbase.datadashboard.backend.api.ObservationListDto
import org.radarbase.datadashboard.backend.domain.mapper.toDto
import org.radarbase.datadashboard.backend.resource.paramconverter.InstantParamConverterProvider
import org.radarbase.datadashboard.backend.service.ObservationService
import org.radarbase.datadashboard.backend.util.MockAsyncCoroutineService
import org.radarbase.datadashboard.backend.util.ObservationType
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.category
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.createObservation
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.projectId
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.subjectId
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.topicId
import org.radarbase.datadashboard.backend.util.buildTarget
import org.radarbase.datadashboard.backend.domain.model.Observation
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService
import java.time.Instant

class ObservationResourceTest : JerseyTest() {

    init {
        set(TestProperties.CONTAINER_PORT, "0")
    }

    @Mock
    lateinit var observationService: ObservationService

    private lateinit var observationListDto: ObservationListDto

    var sinceCaptor = argumentCaptor<Instant>()
    var untilCaptor = argumentCaptor<Instant>()

    class TestResourceEnhancer : JerseyResourceEnhancer {
        override val classes: Array<Class<*>>
            get() = listOfNotNull(
                // Needed to map Instant type in @QueryParam
                InstantParamConverterProvider::class.java,
            ).toTypedArray()

        override val packages: Array<String> = arrayOf(
            "org.radarbase.datadashboard.backend.resource",
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
        reset(observationService)
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
        sinceCaptor = argumentCaptor<Instant>()
        untilCaptor = argumentCaptor<Instant>()
        observationService.stub {
            onBlocking {
                // Instruct the mock to return the fake observations when called.
                getObservations(
                    projectId = anyString(),
                    subjectId = anyString(),
                    topicId = anyString(),
                    category = anyOrNull(),
                    variable = anyOrNull(),
                    since = anyOrNull(),
                    until = anyOrNull(),
                )
            }.doReturn(observationListDto)
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/observations, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/observations, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/observations, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/observations, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/observations, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/observations, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/observations, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/observations, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z"
        ],
        nullValues = ["null"]
    )
    fun testGetObservations(url: String, since: String?, until: String?) = runBlocking {
        // Since a parameterized test is used, the reset and init functions are called for each test case.
        reset(observationService)
        init()
        // Make the call to the REST endpoint.
        buildTarget(url, since, until).request().get()
            .use { response ->
                // Expect the http response to be OK and the same as the expected DTO.
                assertEquals(200, response.status)
                assertEquals(observationListDto, response.readEntity(ObservationListDto::class.java))
            }
        verify(observationService).getObservations(
            projectId = anyString(),
            subjectId = anyString(),
            topicId = anyString(),
            category = anyOrNull(),
            variable = anyString(),
            since = sinceCaptor.capture(),
            until = untilCaptor.capture(),
        )
        assertEquals(since?.let { Instant.parse(it) }, sinceCaptor.lastValue)
        assertEquals(until?.let { Instant.parse(it) }, untilCaptor.lastValue)
    }

    @ParameterizedTest
    @CsvSource(
        "project/$projectId/subject//topic/$topicId/observations",
        "project/$projectId/subject//topic/$topicId/category/$category/variable/numeric-variable/observations",
        "project/$projectId/subject//topic/$topicId/variable/numeric-variable/observations",
    )
    fun testGetObservations_failNoSubjectId(url: String) = runBlocking {
        target(url)
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
        target(url)
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
