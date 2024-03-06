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
import org.radarbase.datadashboard.api.domain.model.Observation
import org.radarbase.jersey.hibernate.HibernateRepository
import org.slf4j.LoggerFactory

class ObservationRepository(
    @Context em: Provider<EntityManager>,
) : HibernateRepository(em) {

    fun getObservations(subjectId: String): List<Observation> {
        logger.debug("Get observations of {}", subjectId)

        return transact {
            createQuery(
                "SELECT o FROM Observation o WHERE o.subjectId = :subjectId ORDER BY o.date DESC",
                Observation::class.java,
            ).apply {
                setParameter("subjectId", subjectId)
            }.resultList
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ObservationRepository::class.java)
    }
}
