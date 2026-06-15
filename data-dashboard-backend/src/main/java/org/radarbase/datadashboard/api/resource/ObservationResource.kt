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
import org.radarbase.datadashboard.api.service.ObservationService
import org.radarbase.datadashboard.api.service.ObservationTypeService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.LoggerFactory
import java.time.Instant

@Path("project/{projectId}/subject/{subjectId}/topic/{topicId}")
@Resource
@Produces("application/json")
@Consumes("application/json")
@Authenticated
@Singleton
class ObservationResource(
    @Context private val observationService: ObservationService,
    @Context private val typeService: ObservationTypeService,
    @Context private val asyncService: AsyncCoroutineService,
) {
    @GET
    @Path("observations")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getAllObservations(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        observationService.getObservations(
            projectId = projectId,
            subjectId = subjectId,
            topicId = topicId,
            since = since,
            until = until,
        )
    }

    @GET
    @Path("category/{category}/variable/{variable}/observations")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getObservationsByCategoryAndVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("category") category: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        observationService.getObservations(
            projectId = projectId,
            subjectId = subjectId,
            topicId = topicId,
            category = category,
            variable = variable,
            since = since,
            until = until,
        )
    }

    @GET
    @Path("category/{category}/variable/{variable}/max")
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
    ) = asyncService.runAsCoroutine(asyncResponse) {
        when (typeService.isNumeric(topicId, category, variable)) {
            true -> observationService.getMaxByCategoryAndVariable(
                projectId = projectId,
                subjectId = subjectId,
                topicId = topicId,
                category = category,
                variable = variable,
                since = since,
                until = until,
            )
            null -> null // Means that there are no observations for this variable yet, and no calculation could be done at the moment of the request.
            else -> throw IllegalArgumentException("Variable is not numeric")
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ObservationResource::class.java)
    }
}
