/*
 *
 *  *  Copyright 2025 The Hyve
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

import org.radarbase.gradle.plugin.radarKotlin
import org.radarbase.gradle.plugin.radarPublishing

plugins {
    application
    kotlin("plugin.serialization")
    kotlin("plugin.noarg")
    kotlin("plugin.jpa")
    kotlin("plugin.allopen")
    id("org.radarbase.radar-kotlin")
    id("org.radarbase.radar-publishing")
}

radarPublishing {
    githubUrl.set("https://github.com/RADAR-base/radar-data-dashboard-backend")
    developers {
        developer {
            id.set("pvanierop")
            name.set("Pim van Nierop")
            email.set("pim@thehyve.nl")
            organization.set("radar-base")
        }
    }
}

radarKotlin {
    log4j2Version.set(Versions.log4j2)
    sentryEnabled.set(true)
    openTelemetryAgentEnabled.set(true)
}

application {
    mainClass.set("org.radarbase.datadashboard.api.DashboardApi")
}

dependencies {
    implementation(kotlin("reflect"))

    implementation("org.radarbase:radar-jersey:${Versions.radarJersey}")
    implementation("org.radarbase:radar-jersey-hibernate:${Versions.radarJersey}") {
        runtimeOnly("org.postgresql:postgresql:${Versions.postgresql}")
    }
    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")

    testImplementation("org.mockito:mockito-core:${Versions.mockitoKotlin}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")
    testImplementation("org.glassfish.jersey.test-framework:jersey-test-framework-core:${Versions.jersey}")
    testImplementation("org.glassfish.jersey.test-framework.providers:jersey-test-framework-provider-grizzly2:${Versions.jersey}")
    testImplementation("com.h2database:h2:2.2.224")
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
