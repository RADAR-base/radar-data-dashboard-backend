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

package org.radarbase.datadashboard.config

data class VariableTypeCacheConfig(
    // This needs activation of the UseJavaDurationConversion module in Jackson Object mapper
    // val refreshDuration: Duration = Duration.parse("30m"),
    val refreshDurationSec: Int = 1800,
) {
    fun withEnv(): VariableTypeCacheConfig = copy(
        refreshDurationSec = System.getenv("DATA_DASHBOARD_VARIABLE_TYPE_CACHE_REFRESH_DURATION_SECONDS")?.toInt()
            ?: refreshDurationSec
    )

}
