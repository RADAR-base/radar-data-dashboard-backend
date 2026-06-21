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

package org.radarbase.datadashboard.backend.resource

import jakarta.annotation.Resource
import jakarta.inject.Singleton
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import org.radarbase.auth.authorization.Permission
import org.radarbase.datadashboard.backend.service.ObservationService
import org.radarbase.datadashboard.backend.service.ObservationTypeService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.service.AsyncCoroutineService
import java.time.Instant

@Path("project/{projectId}/subject/{subjectId}/topic/{topicId}")
@Resource
@Produces("application/json")
@Consumes("application/json")
@Authenticated
@Singleton
class ValueResource(
    @Context private val observationService: ObservationService,
    @Context private val typeService: ObservationTypeService,
    @Context private val asyncService: AsyncCoroutineService,
) {

    @GET
    @Path("category/{category}/variable/{variable}/values")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getValuesByCategoryAndVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("category") category: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        // when the data type cannot be determined, this means that there are no
        // observations for this subject. return emtpy list in this case.
        val isNumericVariable =
            typeService.hasNumericValues(topicId, category, variable) ?: return@runAsCoroutine listOf()
        if (isNumericVariable) {
            observationService.getNumericValues(projectId, subjectId, topicId, category, variable, since, until)
        } else {
            observationService.getTextValues(projectId, subjectId, topicId, category, variable, since, until)
        }
    }

    @GET
    @Path("category/{category}/variable/{variable}/values/min")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getMinByCategoryAndVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("category") category: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = calculateValue(
        projectId = projectId,
        subjectId = subjectId,
        topicId = topicId,
        category = category,
        variable = variable,
        since = since,
        until = until,
        asyncResponse = asyncResponse
    ) { it.minOrNull() }

    @GET
    @Path("category/{category}/variable/{variable}/values/max")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getMaxByCategoryAndVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("category") category: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = calculateValue(
        projectId = projectId,
        subjectId = subjectId,
        topicId = topicId,
        category = category,
        variable = variable,
        since = since,
        until = until,
        asyncResponse = asyncResponse
    ) { it.maxOrNull() }

    @GET
    @Path("category/{category}/variable/{variable}/values/avg")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getAverageByCategoryAndVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("category") category: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = calculateValue(
        projectId = projectId,
        subjectId = subjectId,
        topicId = topicId,
        category = category,
        variable = variable,
        since = since,
        until = until,
        asyncResponse = asyncResponse
    ) { it.average() }


    @GET
    @Path("variable/{variable}/values")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getValuesByVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        // When the data type cannot be determined, this means that there are no
        // observations for this subject. return emtpy list in this case.
        val isNumericVariable =
            typeService.hasNumericValues(topic = topicId, variable = variable) ?: return@runAsCoroutine listOf()
        if (isNumericVariable) {
            observationService.getNumericValues(
                projectId = projectId,
                subjectId = subjectId,
                topicId = topicId,
                variable = variable,
                since = since,
                until = until,
            )
        } else {
            observationService.getTextValues(
                projectId = projectId,
                subjectId = subjectId,
                topicId = topicId,
                variable = variable,
                since = since,
                until = until,
            )
        }
    }

    @GET
    @Path("variable/{variable}/values/min")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getMinByVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = calculateValue(
        projectId = projectId,
        subjectId = subjectId,
        topicId = topicId,
        variable = variable,
        since = since,
        until = until,
        asyncResponse = asyncResponse
    ) { it.minOrNull() }

    @GET
    @Path("variable/{variable}/values/max")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getMaxByVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = calculateValue(
        projectId = projectId,
        subjectId = subjectId,
        topicId = topicId,
        variable = variable,
        since = since,
        until = until,
        asyncResponse = asyncResponse
    ) { it.maxOrNull() }

    @GET
    @Path("variable/{variable}/values/avg")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getAverageByVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = calculateValue(
        projectId = projectId,
        subjectId = subjectId,
        topicId = topicId,
        variable = variable,
        since = since,
        until = until,
        asyncResponse = asyncResponse
    ) { it.average() }

    private fun calculateValue(
        projectId: String,
        subjectId: String,
        topicId: String,
        category: String? = null,
        variable: String,
        since: Instant? = null,
        until: Instant? = null,
        asyncResponse: AsyncResponse,
        func: (Iterable<Double>) -> Number?,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        when (typeService.hasNumericValues(topicId, category, variable)) {
            true -> {
                observationService.calculateValueByCategoryAndVariable(
                    projectId = projectId,
                    subjectId = subjectId,
                    topicId = topicId,
                    category = category,
                    variable = variable,
                    since = since,
                    until = until,
                    func = func,
                )
            }
            // When no observations are found in the database return null.
            null -> null
            // Since observations may acquire numeric values over time during data
            // collection, silently return null (do not terminate with an error).
            false -> null
        }
    }
}
