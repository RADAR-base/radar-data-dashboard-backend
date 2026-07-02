plugins {
    application
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.kotlinNoarg)
    alias(libs.plugins.kotlinJpa)
    alias(libs.plugins.kotlinAllopen)
}

application {
    mainClass.set("org.radarbase.datadashboard.backend.DashboardApi")
}

dependencies {
    implementation(libs.kotlin.reflect)

    implementation(libs.radar.jersey)
    implementation(libs.radar.jerseyHibernate) {
        runtimeOnly(libs.postgresql)
    }
    implementation(libs.radar.commonsKotlin)

    implementation(libs.hazelcast)
    implementation(libs.hazelcast.hybernate53)
    implementation(libs.hazelcast.kubernetes)

    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.params)
    testImplementation(libs.jersey.testFramework.core)
    testImplementation(libs.jersey.testFramework.provider.grizzly2)
    testImplementation(libs.h2)
    testImplementation(libs.liquibase.core)
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}
