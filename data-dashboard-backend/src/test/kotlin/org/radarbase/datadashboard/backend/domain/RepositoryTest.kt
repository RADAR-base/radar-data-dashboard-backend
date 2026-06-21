/*
 *
 *  *  Copyright 2026 The Hyve
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

package org.radarbase.datadashboard.backend.domain

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.hibernate.boot.model.naming.PhysicalNamingStrategy
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.radarbase.datadashboard.backend.domain.model.Observation
import org.radarbase.datadashboard.backend.util.H2HibernateNamingStrategy
import java.sql.DriverManager
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class RepositoryTest {
    protected lateinit var emf: EntityManagerFactory
    protected lateinit var em: EntityManager

    @BeforeAll
    fun setUp() {
        val url =
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;DEFAULT_NULL_ORDERING=HIGH;INIT=create schema if not exists ${SCHEMA_NAME}"
        val user = "sa"
        val password = ""

        // Run Liquibase first
        DriverManager.getConnection(url, user, password).let {
            val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(it))
            database.liquibaseSchemaName = null
            database.defaultSchemaName = SCHEMA_NAME
            val liquibase = Liquibase(
                "db/changelog/changes/db.changelog-master.xml",
                ClassLoaderResourceAccessor(),
                database
            )
            liquibase.update(LIQUIBASE_CONTEXT)
        }

        val props = Properties()
        props["jakarta.persistence.jdbc.url"] = url
        props["jakarta.persistence.jdbc.user"] = user
        props["jakarta.persistence.jdbc.password"] = password
        props["jakarta.persistence.jdbc.driver"] = "org.h2.Driver"
        props["hibernate.dialect"] = "org.hibernate.dialect.H2Dialect"
        props["hibernate.show_sql"] = "true"
        props["hibernate.format_sql"] = "true"
        // Since we use Liquibase to create the schema, we don't want Hibernate to change it
        props["jakarta.persistence.schema-generation.database.action"] = "none"
        props["hibernate.default_schema"] = SCHEMA_NAME
        props["hibernate.globally_quoted_identifiers"] = "true"

        val configuration = org.hibernate.cfg.Configuration()
        configuration.addAnnotatedClass(Observation::class.java)
        configuration.addProperties(props)
        configuration.setPhysicalNamingStrategy(
            H2HibernateNamingStrategy()
        )

        emf = configuration.buildSessionFactory()
        em = emf.createEntityManager()
    }

    @AfterAll
    fun tearDown() {
        if (this::em.isInitialized) em.close()
        if (this::emf.isInitialized) emf.close()
    }

    companion object {
        const val SCHEMA_NAME = "OBSERVATIONS"
        const val LIQUIBASE_CONTEXT = "dev"
    }
}
