# RADAR-base Data Dashboard backend

Application that provides a REST API to provide Variables (measured modalities) and related Observations (measurements)
for Subjects (participants). The data layer connects to the TimescaleDB database that is provisioned with (a subset of)
the data from the RADAR-base kafka service.[]

<!-- TOC -->
* [RADAR-base Data Dashboard backend](#radar-base-data-dashboard-backend)
  * [Features supported](#features-supported)
  * [APIs to be used by REST Source-Connectors](#apis-to-be-used-by-rest-source-connectors)
    * [Path Parameters](#path-parameters)
    * [Query Parameters](#query-parameters)
    * [Observation Endpoints](#observation-endpoints)
    * [Value Endpoints](#value-endpoints)
    * [Examples](#examples)
  * [Installation](#installation)
  * [Authorization](#authorization)
    * [Registering OAuth Clients with ManagementPortal](#registering-oauth-clients-with-managementportal)
  * [Sentry monitoring](#sentry-monitoring)
<!-- TOC -->

## Features supported

1. API for retrieving variables and observations for a single subject.
2. Has liquibase support to enable seamless database schema migration.

## APIs to be used by REST Source-Connectors

Data dashboard applications can use the APIs as follows.

All endpoints are relative to the base URL and require a `MEASUREMENT.READ` scope.

### Path Parameters

- `projectId`: The ID of the project.
- `subjectId`: The ID of the subject.
- `topicId`: The ID of the topic.
- `category`: (Optional) The category of the observation.
- `variable`: The variable name.

### Query Parameters

- `since`: (Optional) Filter observations since this timestamp (inclusive).
- `until`: (Optional) Filter observations until this timestamp (exclusive).

Timestamps should be in ISO-8601 format, e.g., `2026-06-21T13:00:00Z`.

### Observation Endpoints

- `GET /project/{projectId}/subject/{subjectId}/topic/{topicId}/observations`
  Retrieve all observations for a subject and topic.
- `GET /project/{projectId}/subject/{subjectId}/topic/{topicId}/observations/count`
  Retrieve the count of observations.
- `GET /project/{projectId}/subject/{subjectId}/topic/{topicId}/variable/{variable}/observations`
  Retrieve observations for a specific variable.
- `GET /project/{projectId}/subject/{subjectId}/topic/{topicId}/category/{category}/variable/{variable}/observations`
  Retrieve observations for a specific category and variable.

### Value Endpoints

- `GET /project/{projectId}/subject/{subjectId}/topic/{topicId}/variable/{variable}/values`
  Retrieve values for a specific variable.
- `GET /project/{projectId}/subject/{subjectId}/topic/{topicId}/variable/{variable}/values/min`
  Retrieve the minimum value for a variable.
- `GET /project/{projectId}/subject/{subjectId}/topic/{topicId}/variable/{variable}/values/max`
  Retrieve the maximum value for a variable.
- `GET /project/{projectId}/subject/{subjectId}/topic/{topicId}/variable/{variable}/values/avg`
  Retrieve the average value for a variable.

Similar endpoints are available with a category: `/project/{projectId}/subject/{subjectId}/topic/{topicId}/category/{category}/variable/{variable}/values` (also for `min`, `max`, `avg`).

### Examples

**Retrieve observations since a specific time:**
`GET /project/my-project/subject/my-subject/topic/my-topic/observations?since=2026-01-01T00:00:00Z`

**Retrieve observations within a time range:**
`GET /project/my-project/subject/my-subject/topic/my-topic/observations?since=2026-01-01T00:00:00Z&until=2026-01-02T00:00:00Z`

## Installation

To install functional RADAR-base Rest-Sources Authorizer application with minimal dependencies from source, please use
the `docker-compose.yml` under the root directory.

Copy the `docker/etc/rest-source-authorizer/authorizer.yml.template`
into `docker/etc/rest-source-authorizer/authorizer.yml` and modify the `restSourceClients.FitBit.clientId`
and `restSourceClients.FitBit.clientSecret` with your Fitbit client application credentials. Then start the
docker-compose stack:

```bash
docker-compose up -d --build
```

## Authorization

All users registered with the application will be authorized against ManagementPortal for integrity and security.

### Registering OAuth Clients with ManagementPortal

To add new OAuth clients, you can add at runtime through the UI on Management Portal, or you can add them to the OAuth
clients file referenced by the `MANAGEMENTPORTAL_OAUTH_CLIENTS_FILE` configuration option. For more info,
see [officail docs](https://github.com/RADAR-base/ManagementPortal#oauth-clients)
The OAuth client for authorizer-app-backend should have the following properties.

```properties
client-id:data_dashboard_api
client-secret:Confidential
grant-type:authorization_code,refresh_token
resources:res_DataDashboardAPI
scope:MEASUREMENT.READ
```

## Sentry monitoring

To enable Sentry monitoring:

1. Set a `SENTRY_DSN` environment variable that points to the desired Sentry DSN.
2. (Optional) Set the `SENTRY_LOG_LEVEL` environment variable to control the minimum log level of events sent to Sentry.
   The default log level for Sentry is `WARN`. Possible values are `TRACE`, `DEBUG`, `INFO`, `WARN`, and `ERROR`.
