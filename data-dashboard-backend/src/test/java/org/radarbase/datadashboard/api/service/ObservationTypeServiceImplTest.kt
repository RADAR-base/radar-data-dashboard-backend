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

package org.radarbase.datadashboard.api.service

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.stub
import org.radarbase.datadashboard.api.domain.ObservationRepositoryImpl

class ObservationTypeServiceImplTest {

    // Create a Mockito mock of the ObservationRepository. This is instantiated in the init block.
    @Mock
    private lateinit var observationRepository: ObservationRepositoryImpl

    private val topicId = "topic-1"
    private val category = "category-1"

    private val observationTypeService: ObservationTypeServiceImpl

    init {
        // Initialize all Mockito mocks.
        MockitoAnnotations.openMocks(this)
        observationTypeService = ObservationTypeServiceImpl(observationRepository)
        observationRepository.stub {
            onBlocking {
                getVariableType(
                    topicId = topicId,
                    category = category,
                    variable = "string-variable",
                )
            }.doReturn("STRING")
            onBlocking {
                getVariableType(
                    topicId = topicId,
                    category = category,
                    variable = "integer-variable",
                )
            }.doReturn("INTEGER")
            onBlocking {
                getVariableType(
                    topicId = topicId,
                    category = category,
                    variable = "double-variable",
                )
            }.doReturn("DOUBLE")
            onBlocking {
                getVariableType(
                    topicId = topicId,
                    category = category,
                    variable = "json-variable",
                )
            }.doReturn("STRING_JSON")
            onBlocking {
                getVariableType(
                    topicId = topicId,
                    category = category,
                    variable = "non-existing-variable",
                )
            }.doReturn(null)
        }
    }

    @ParameterizedTest
    @ValueSource(strings = ["string-variable", "json-variable"])
    fun test_isNotNumeric(variableName: String) = runBlocking {
        assert(
            observationTypeService.isNumeric(
                topic = topicId,
                category = category,
                variable = variableName,
            ) == false
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["integer-variable", "double-variable"])
    fun test_isNumeric(variableName: String) = runBlocking {
        assert(
            observationTypeService.isNumeric(
                topic = topicId,
                category = category,
                variable = variableName,
            ) == true
        )
    }

    @ParameterizedTest
    @ValueSource(strings = ["non-existing-variable"])
    fun test_isNull(variableName: String) = runBlocking {
        assert(
            observationTypeService.isNumeric(
                topic = topicId,
                category = category,
                variable = variableName,
            ) == null
        )
    }
}
