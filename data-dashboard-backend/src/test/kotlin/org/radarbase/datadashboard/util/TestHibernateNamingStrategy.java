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

package org.radarbase.datadashboard.util;

import  org.radarbase.datadashboard.domain.model.CamelCaseToUppercaseColumnNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;

public class TestHibernateNamingStrategy extends CamelCaseToUppercaseColumnNamingStrategy {

    @Override
    public Identifier toPhysicalTableName(final Identifier name, final JdbcEnvironment context) {
        if (name == null) {
            return null;
        }
        return Identifier.toIdentifier(name.getText(), true);
    }

}
