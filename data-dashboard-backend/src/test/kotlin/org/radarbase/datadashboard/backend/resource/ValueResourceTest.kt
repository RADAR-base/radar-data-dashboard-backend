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
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub
import org.radarbase.datadashboard.backend.resource.paramconverter.InstantParamConverterProvider
import org.radarbase.datadashboard.backend.service.ObservationService
import org.radarbase.datadashboard.backend.util.MockAsyncCoroutineService
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.category
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.projectId
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.subjectId
import org.radarbase.datadashboard.backend.util.TestUtil.Companion.topicId
import org.radarbase.datadashboard.backend.util.buildTarget
import org.radarbase.jersey.config.ConfigLoader
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService

class ValueResourceTest : JerseyTest() {

    init {
        set(TestProperties.CONTAINER_PORT, "0")
    }

    @Mock
    lateinit var observationService: ObservationService

    private var capturedFunc: ((Iterable<Double>) -> Number?)? = null

    private val numericValues = listOf(1.0, 2.0, 3.0)
    private val textValues = listOf("a", "b", "c")
    private val stubCalculationResponse = 1.0

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
            bind(MockAsyncCoroutineService()).to(AsyncCoroutineService::class.java).`in`(Singleton::class.java)
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
        capturedFunc = null
        observationService.stub {
            onBlocking {
                getValues(
                    projectId = anyString(),
                    subjectId = anyString(),
                    topicId = anyString(),
                    category = anyOrNull(),
                    variable = eq("numeric-variable"),
                    since = anyOrNull(),
                    until = anyOrNull(),
                )
            }.doReturn(numericValues)
            onBlocking {
                getValues(
                    projectId = anyString(),
                    subjectId = anyString(),
                    topicId = anyString(),
                    category = anyOrNull(),
                    variable = eq("text-variable"),
                    since = anyOrNull(),
                    until = anyOrNull(),
                )
            }.doReturn(textValues)
            onBlocking {
                calculateValueByCategoryAndVariable(
                    projectId = anyString(),
                    subjectId = anyString(),
                    topicId = anyString(),
                    category = anyOrNull(),
                    variable = anyString(),
                    func = anyOrNull(),
                    since = anyOrNull(),
                    until = anyOrNull(),
                )
            }.thenAnswer { invocation ->
                capturedFunc = invocation.getArgument(7)
                stubCalculationResponse
            }
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z"
        ],
        nullValues = ["null"]
    )
    fun testGetNumbers(url: String, since: String?, until: String?) = runBlocking {
        buildTarget(url, since, until).request().get().use { response ->
            // Expect the http response to be OK and the same as the expected DTO.
            assertEquals(200, response.status)
            assertEquals(numericValues, response.readEntity(List::class.java))
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/text-variable/values, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/text-variable/values, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/text-variable/values, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/text-variable/values, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/text-variable/values, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/text-variable/values, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/text-variable/values, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/text-variable/values, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z"
        ],
        nullValues = ["null"]
    )
    fun testGetText(url: String, since: String?, until: String?) = runBlocking {
        buildTarget(url, since, until).request().get().use { response ->
            // Expect the http response to be OK and the same as the expected DTO.
            assertEquals(200, response.status)
            assertEquals(textValues, response.readEntity(List::class.java))
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/max, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/max, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/max, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/max, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/max, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/max, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/max, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/max, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z"
        ],
        nullValues = ["null"]
    )
    fun testGetMax(url: String, since: String?, until: String?) = runBlocking {
        buildTarget(url, since, until).request().get().use { response ->
            // Expect the http response to be OK and the same as the expected DTO.
            assertEquals(200, response.status)
            assertEquals(stubCalculationResponse, response.readEntity(Double::class.java))
        }
        // Test whether the passed function is the max function.
        val testData = listOf(10.0, 20.0, 5.0)
        assertEquals(20.0, capturedFunc?.invoke(testData))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/min, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/min, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/min, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/min, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/min, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/min, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/min, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/min, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z"
        ],
        nullValues = ["null"]
    )
    fun testGetMin(url: String, since: String?, until: String?) = runBlocking {
        buildTarget(url, since, until).request().get().use { response ->
            assertEquals(200, response.status)
            assertEquals(stubCalculationResponse, response.readEntity(Double::class.java))
        }
        // Test whether the passed function is the min function.
        val testData = listOf(10.0, 20.0, 5.0)
        assertEquals(5.0, capturedFunc?.invoke(testData))
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/avg, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/avg, null, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/avg, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/avg, 2020-06-01T00:00:00Z, null",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/avg, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/avg, null, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/category/$category/variable/numeric-variable/values/avg, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z",
            "project/$projectId/subject/$subjectId/topic/$topicId/variable/numeric-variable/values/avg, 2020-06-01T00:00:00Z, 2021-06-01T00:00:00Z"
        ],
        nullValues = ["null"]
    )
    fun testGetAverage(url: String, since: String?, until: String?) = runBlocking {
        buildTarget(url, since, until).request().get().use { response ->
            assertEquals(200, response.status)
            assertEquals(stubCalculationResponse, response.readEntity(Double::class.java))
        }
        // Test whether the passed function is the average function.
        val testData = listOf(10.0, 20.0, 30.0)
        assertEquals(20.0, capturedFunc?.invoke(testData))
    }

}
