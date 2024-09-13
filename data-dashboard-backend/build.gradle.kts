plugins {
    application
    kotlin("plugin.serialization")
    kotlin("plugin.noarg")
    kotlin("plugin.jpa")
    kotlin("plugin.allopen")
}

application {
    mainClass.set("org.radarbase.datadashboard.api.Main")
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

sentry {
    org = rootProject.group as String
    projectName = project.name
    if (System.getenv("SENTRY_AUTH_TOKEN") != null) {
        logger.info("Sentry auth token found, enabling source context")
        // Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
        // This allows you to see your source code as part of your stack traces in Sentry.
        includeSourceContext = true
        authToken = System.getenv("SENTRY_AUTH_TOKEN")
    } else {
        logger.info("Not Sentry auth token found. Source context will not be uploaded to Sentry.")
        includeSourceContext = false
    }
}
