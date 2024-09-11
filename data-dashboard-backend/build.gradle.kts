plugins {
    application
    kotlin("plugin.serialization")
    kotlin("plugin.noarg")
    kotlin("plugin.jpa")
    kotlin("plugin.allopen")
    id("io.sentry.jvm.gradle") version "4.10.0"
}

application {
    mainClass.set("org.radarbase.datadashboard.api.Main")
}

dependencies {
    api(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))

    implementation("org.radarbase:radar-jersey:${Versions.radarJersey}")
    implementation("org.radarbase:radar-jersey-hibernate:${Versions.radarJersey}") {
        runtimeOnly("org.postgresql:postgresql:${Versions.postgresql}")
    }
    implementation("org.radarbase:radar-commons-kotlin:${Versions.radarCommons}")
    annotationProcessor("org.apache.logging.log4j:log4j-core:2.20.0")

    implementation("com.mindscapehq:core:3.0.0")
    implementation("com.mindscapehq:webprovider:3.0.0")

    implementation("com.bugsnag:bugsnag:3.+")

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
    // Generates a JVM (Java, Kotlin, etc.) source bundle and uploads your source code to Sentry.
    // This enables source context, allowing you to see your source
    // code as part of your stack traces in Sentry.
    includeSourceContext = true
    org = "radar-base"
    projectName = "data-dashboard-backend"
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
}
