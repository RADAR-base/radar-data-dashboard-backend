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
class ObservationResource(
    @Context private val observationService: ObservationService,
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
    @Path("observations/count")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getlObservationsCount(
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
        ).observations.size
    }

    @GET
    @Path("variable/{variable}/observations")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getObservationsByCategoryAndVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        observationService.getObservations(
            projectId = projectId,
            subjectId = subjectId,
            topicId = topicId,
            variable = variable,
            since = since,
            until = until,
        )
    }

    @GET
    @Path("variable/{variable}/observations/count")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getObservationsCountByCategoryAndVariable(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @PathParam("variable") variable: String,
        @QueryParam("since") since: Instant?,
        @QueryParam("until") until: Instant?,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        observationService.getObservations(
            projectId = projectId,
            subjectId = subjectId,
            topicId = topicId,
            variable = variable,
            since = since,
            until = until,
        ).observations.size
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
    @Path("category/{category}/variable/{variable}/observations/count")
    @NeedsPermission(Permission.MEASUREMENT_READ, "projectId", "subjectId")
    fun getObservationsCountByCategoryAndVariable(
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
        ).observations.size
    }

}
