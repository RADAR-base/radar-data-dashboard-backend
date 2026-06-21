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

package org.radarbase.datadashboard.backend.domain.hibernate

import org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
import org.hibernate.boot.model.naming.Identifier
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment

open class CamelCaseToUppercaseColumnNamingStrategy : CamelCaseToUnderscoresNamingStrategy() {
    override fun toPhysicalSchemaName(logicalName: Identifier?, jdbcEnvironment: JdbcEnvironment?): Identifier? =
        adjustName(super.toPhysicalSchemaName(logicalName, jdbcEnvironment))

    override fun toPhysicalTableName(logicalName: Identifier?, jdbcEnvironment: JdbcEnvironment?): Identifier? =
        adjustName(super.toPhysicalTableName(logicalName, jdbcEnvironment))

    override fun toPhysicalColumnName(logicalName: Identifier?, jdbcEnvironment: JdbcEnvironment?): Identifier? =
        adjustName(super.toPhysicalColumnName(logicalName, jdbcEnvironment))

    private fun adjustName(name: Identifier?): Identifier? = name?.let {
        Identifier(it.text.uppercase(), true)
    }
}
