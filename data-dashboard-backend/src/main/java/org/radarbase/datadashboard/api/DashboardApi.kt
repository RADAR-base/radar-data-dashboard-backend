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

package org.radarbase.datadashboard.api

import io.sentry.Instrumenter
import io.sentry.Sentry
import io.sentry.opentelemetry.OpenTelemetryLinkErrorEventProcessor
import org.radarbase.datadashboard.api.config.DashboardApiConfig
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.config.ConfigLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object DashboardApi {

    init {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
    }

    private val logger: Logger = LoggerFactory.getLogger(DashboardApi.javaClass)

    private fun setupSentryWithOpenTelemetry(sentryDsn: String) {
        // Initialize Sentry with DSN and tracesSampleRate
        Sentry.init { options ->
            options.dsn = sentryDsn
            options.tracesSampleRate = 1.0
            options.instrumenter = Instrumenter.OTEL
            options.addEventProcessor(OpenTelemetryLinkErrorEventProcessor())
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.firstOrNull() in arrayOf("--help", "-h")) {
            logger.info("Usage: <command> [<config file path>]")
            exitProcess(0)
        }

        val configFile = args.firstOrNull() ?: "dashboard.yml"
        val config: DashboardApiConfig = ConfigLoader.loadConfig(configFile, args)
        val resources = ConfigLoader.loadResources(config.service.resourceConfig, config.withEnv())

        // Initialize Sentry with OpenTelemetry.
        // To enable OpenTelemetry, auto-initialization of Sentry must be disabled and needs to be done manually.
        if (!config.service.sentryDsn.isNullOrEmpty() && config.service.enableOpenTelemetry) {
            logger.debug("Initializing Sentry with OpenTelemetry...")
            setupSentryWithOpenTelemetry(config.service.sentryDsn)
        }

        GrizzlyServer(config.service.baseUri, resources).run {
            listen()
        }
    }
}
