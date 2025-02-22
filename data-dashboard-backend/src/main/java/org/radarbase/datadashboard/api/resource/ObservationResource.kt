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
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Context
import org.radarbase.auth.authorization.Permission
import org.radarbase.datadashboard.api.service.ObservationService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.LoggerFactory

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
    fun getObservations(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("topicId") topicId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        observationService.getObservations(projectId = projectId, subjectId = subjectId, topicId = topicId)
    }

    companion object {
        private val log = LoggerFactory.getLogger(ObservationResource::class.java)
    }
}
