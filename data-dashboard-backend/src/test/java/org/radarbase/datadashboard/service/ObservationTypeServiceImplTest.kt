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

package org.radarbase.datadashboard.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.radarbase.datadashboard.domain.ObservationRepositoryImpl
import org.radarbase.datadashboard.util.TestUtil.Companion.category
import org.radarbase.datadashboard.util.TestUtil.Companion.topicId
import org.radarbase.datadashboard.util.cacheKey

class ObservationTypeServiceImplTest {

    // Create a Mockito mock of the ObservationRepository. This is instantiated in the init block.
    @Mock
    private lateinit var observationRepository: ObservationRepositoryImpl

    private val observationTypeService: ObservationTypeServiceImpl

    init {
        // Initialize all Mockito mocks.
        MockitoAnnotations.openMocks(this)
        observationTypeService = ObservationTypeServiceImpl(
            observationRepository,
            org.radarbase.datadashboard.config.VariableTypeCacheConfig()
        )
        observationRepository.stub {
            onBlocking {
                getNumericVariableTypes()
            }.doReturn(
                mapOf(
                    cacheKey(topicId, category, "integer-variable") to true,
                    cacheKey(topicId, category, "string-variable") to false,
                    cacheKey(topicId, category, "double-variable") to true,
                    cacheKey(topicId, category, "json-variable") to false,
                )
            )
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "integer-variable, true",
            "string-variable, false",
            "double-variable, true",
            "json-variable, false",
            "non-existing-variable, null",
        ],
        nullValues = ["null"]
    )
    fun test_hasNumericValues(variableName: String, hasNumericValues: Boolean?) = runBlocking {
        assert(
            observationTypeService.hasNumericValues(
                topic = topicId,
                category = category,
                variable = variableName,
            ) == hasNumericValues
        )
    }

}
