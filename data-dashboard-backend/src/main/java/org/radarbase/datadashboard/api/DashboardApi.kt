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

import com.bugsnag.Bugsnag
import com.mindscapehq.raygun4java.core.IRaygunClientFactory
import com.mindscapehq.raygun4java.core.RaygunClient
import com.mindscapehq.raygun4java.core.RaygunClientFactory
import io.sentry.Sentry
import io.sentry.SentryOptions
import org.radarbase.datadashboard.api.config.DashboardApiConfig
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.config.ConfigLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

object Main {

    init {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
    }
    private val logger: Logger = LoggerFactory.getLogger(Main.javaClass)

    @JvmStatic
    fun main(args: Array<String>) {
        if (args.firstOrNull() in arrayOf("--help", "-h")) {
            logger.info("Usage: <command> [<config file path>]")
            exitProcess(0)
        }

        val configFile = args.firstOrNull() ?: "dashboard.yml"
        val config: DashboardApiConfig = ConfigLoader.loadConfig(configFile, args)
        val resources = ConfigLoader.loadResources(config.service.resourceConfig, config.withEnv())

        Sentry.init { options: SentryOptions ->
            options.dsn =
                "https://6a0679aa8e76466de1f22a03af1cf335@o4507611449065472.ingest.de.sentry.io/4507611454242896"
            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
            // We recommend adjusting this value in production.
            options.tracesSampleRate = 1.0
            // When first trying Sentry it's good to see what the SDK is doing:
            options.isDebug = true

        }
        val raygunFactory: IRaygunClientFactory = RaygunClientFactory("T6N90OBA1JBDQeQ8vIwg")
            .withVersion("1.2.3")
            .withTag("beta")
            .withData("prod", false)
        val raygunClient: RaygunClient = raygunFactory.newClient()

        val bugsnag: Bugsnag = Bugsnag("28921adc65be4fd1643684b73ab0b636")

        try {
            throw Exception("This is a test exception")
        } catch (e: Exception) {
            Sentry.captureException(e)
            raygunClient.send(e)
            bugsnag.notify(e)
        }

        GrizzlyServer(config.service.baseUri, resources).run {
            listen()
        }
    }
}
