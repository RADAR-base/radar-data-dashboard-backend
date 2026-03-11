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

import org.radarbase.gradle.plugin.radarKotlin
import org.radarbase.gradle.plugin.radarPublishing

plugins {
    alias(libs.plugins.radarRootProject)
    alias(libs.plugins.radarDependencyManagement)
    alias(libs.plugins.radarKotlin) apply false
    alias(libs.plugins.radarPublishing) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.kotlinNoarg) apply false
    alias(libs.plugins.kotlinJpa) apply false
    alias(libs.plugins.kotlinAllopen) apply false
    alias(libs.plugins.sentry) apply false
}

radarRootProject {
    projectVersion.set(libs.versions.project)
    gradleVersion.set(libs.versions.gradle)
}

subprojects {
    apply(plugin = "org.radarbase.radar-kotlin")
    apply(plugin = "org.radarbase.radar-publishing")

    radarKotlin {
        javaVersion.set(rootProject.libs.versions.java.get().toInt())
        kotlinVersion.set(rootProject.libs.versions.kotlin)
        slf4jVersion.set(rootProject.libs.versions.slf4j)
        log4j2Version.set(rootProject.libs.versions.log4j2)
        sentryEnabled.set(true)
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
}
