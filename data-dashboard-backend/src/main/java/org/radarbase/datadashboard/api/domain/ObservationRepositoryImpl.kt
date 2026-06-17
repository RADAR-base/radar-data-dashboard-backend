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

package org.radarbase.datadashboard.api.domain

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.hibernate.exception.SQLGrammarException
import org.radarbase.datadashboard.api.domain.model.Observation
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.ZoneOffset

class ObservationRepositoryImpl(
    @Context em: Provider<EntityManager>,
    @Context asyncService: AsyncCoroutineService,
) : HibernateRepository(em, asyncService), ObservationRepository {

    private val tableExistsRegex = Regex("relation \".*\" does not exist")

    override suspend fun getObservations(
        projectId: String,
        subjectId: String,
        topicId: String,
        since: Instant?,
        until: Instant?,
    ): List<Observation> {
        val query = buildString {
            append("SELECT o FROM Observation o WHERE o.project = :projectId AND o.subject = :subjectId AND o.topic = :topicId")
            if (since != null) append(" AND o.observationTime > :since")
            if (until != null) append(" AND o.observationTime <= :until")
            append(" ORDER BY o.observationTime DESC")
        }
        val params = buildList<Pair<String, Any>> {
            add(Pair("projectId", projectId))
            add(Pair("subjectId", subjectId))
            add(Pair("topicId", topicId))
            if (since != null) add(Pair("since", since.atZone(ZoneOffset.UTC)))
            if (until != null) add(Pair("until", until.atZone(ZoneOffset.UTC)))
        }
        logger.debug("Get observations in topic {} of subject {} in project {}", topicId, subjectId, projectId)
        return performQuery<Observation>(query, params)
    }

    override suspend fun getObservations(
        projectId: String,
        subjectId: String,
        topicId: String,
        category: String?,
        variable: String,
        since: Instant?,
        until: Instant?,
    ): List<Observation> {
        val query = buildString {
            append("SELECT o FROM Observation o WHERE o.project = :projectId AND o.subject = :subjectId AND o.topic = :topicId AND  o.variable = :variable")
            if (category != null) append(" AND o.category = :category")
            if (since != null) append(" AND o.observationTime > :since")
            if (until != null) append(" AND o.observationTime <= :until")
            append(" ORDER BY o.observationTime DESC")
        }
        val params = buildList<Pair<String, Any>> {
            add(Pair("projectId", projectId))
            add(Pair("subjectId", subjectId))
            add(Pair("topicId", topicId))
            if (category != null) add(Pair("category", category))
            add(Pair("variable", variable))
            if (since != null) add(Pair("since", since.atZone(ZoneOffset.UTC)))
            if (until != null) add(Pair("until", until.atZone(ZoneOffset.UTC)))
        }
        logger.debug(
            "Get observations in topic {} with category {} and variable {} of subject {} in project {}",
            topicId,
            category,
            variable,
            subjectId,
            projectId
        )
        val observations = performQuery<Observation>(query, params,)
        if (category == null && observations.any { it.category != null })
            throw IllegalStateException("Category was null in request, but observation had category. A category must be supplied for this variable.")
        return observations
    }

    override suspend fun getVariableType(
        topicId: String,
        category: String?,
        variable: String,
    ): String? {
        val query = buildString {
            append("SELECT o FROM Observation o WHERE o.topic = :topicId AND o.variable = :variable")
            if (category != null) append(" AND o.category = :category")
        }
        val params = buildList<Pair<String, Any>> {
            add(Pair("topicId", topicId))
            add(Pair("variable", variable))
            if (category != null) add(Pair("category", category))
        }
        logger.debug("Get type for variable {} in category {} of topic {}", variable, category, topicId)
        val observation = performQuery<Observation>(query, params, limit = 1).firstOrNull()
        observation?.category.let {
            if (category == null) {
                throw IllegalStateException("Category was null in request, but observation had category. A category must be supplied for this variable.")
            }
        }
        return observation?.type
    }

    override suspend fun getNumericValues(
        projectId: String,
        subjectId: String,
        topicId: String,
        category: String?,
        variable: String,
        since: Instant?,
        until: Instant?,
    ): List<Double> {
        val query = buildString {
            append("SELECT o.valueNumeric FROM Observation o WHERE o.project = :projectId AND o.subject = :subjectId AND o.topic = :topicId AND o.variable = :variable")
            if (category != null) append(" AND o.category = :category")
            if (since != null) append(" AND o.observationTime > :since")
            if (until != null) append(" AND o.observationTime <= :until")
        }
        val params = buildList<Pair<String, Any>> {
            add(Pair("projectId", projectId))
            add(Pair("subjectId", subjectId))
            add(Pair("topicId", topicId))
            if (category != null) add(Pair("category", category))
            add(Pair("variable", variable))
            if (since != null) add(Pair("since", since.atZone(ZoneOffset.UTC)))
            if (until != null) add(Pair("until", until.atZone(ZoneOffset.UTC)))
        }
        logger.debug(
            "Get numeric values in topic {} with category {} and variable {} of subject {} in project {}",
            topicId,
            category,
            variable,
            subjectId,
            projectId
        )
        return performQuery<Double>(query, params)
    }

    override suspend fun getTextValues(
        projectId: String,
        subjectId: String,
        topicId: String,
        category: String?,
        variable: String,
        since: Instant?,
        until: Instant?,
    ): List<String> {
        val query = buildString {
            append("SELECT o.valueTextual FROM Observation o WHERE o.project = :projectId AND o.subject = :subjectId AND o.topic = :topicId AND o.variable = :variable")
            if (category != null) append(" AND o.category = :category")
            if (since != null) append(" AND o.observationTime > :since")
            if (until != null) append(" AND o.observationTime <= :until")
        }
        val params = buildList<Pair<String, Any>> {
            add(Pair("projectId", projectId))
            add(Pair("subjectId", subjectId))
            add(Pair("topicId", topicId))
            if (category != null) add(Pair("category", category))
            add(Pair("variable", variable))
            if (since != null) add(Pair("since", since.atZone(ZoneOffset.UTC)))
            if (until != null) add(Pair("until", until.atZone(ZoneOffset.UTC)))
        }
        logger.debug(
            "Get numeric values in topic {} with category {} and variable {} of subject {} in project {}",
            topicId,
            category,
            variable,
            subjectId,
            projectId
        )
        return performQuery<String>(query, params)
    }

    private suspend inline fun <reified T> performQuery(
        query: String,
        params: List<Pair<String, Any>>,
        limit: Int? = null,
    ): List<T> {
        return transact {
            try {
                createQuery(
                    query,
                    T::class.java,
                ).apply {
                    params.forEach { this.setParameter(it.first, it.second) }
                    limit?.let {
                        setMaxResults(it)
                    }
                }.resultList
            } catch (ex: SQLGrammarException) {
                if (tableDoesNotExist(ex)) {
                    logger.info(
                        "Observations table has not been created by JDBC connector yet " + "(will be created upon first data ingestion). Returning empty result...",
                    )
                    emptyList()
                } else {
                    throw ex
                }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ObservationRepositoryImpl::class.java)
    }

    private fun tableDoesNotExist(ex: SQLGrammarException): Boolean {
        return tableExistsRegex.containsMatchIn(ex.message ?: "")
    }
}
