package org.radarbase.datadashboard.api

import org.radarbase.jersey.service.ProjectService

class ProjectServiceStub : ProjectService {
    override suspend fun ensureOrganization(organizationId: String) = Unit
    override suspend fun listProjects(organizationId: String): List<String> = emptyList()
    override suspend fun projectOrganization(projectId: String): String = "test-organization"
    override suspend fun ensureProject(projectId: String) = Unit
    override suspend fun ensureSubject(projectId: String, userId: String) = Unit
}
