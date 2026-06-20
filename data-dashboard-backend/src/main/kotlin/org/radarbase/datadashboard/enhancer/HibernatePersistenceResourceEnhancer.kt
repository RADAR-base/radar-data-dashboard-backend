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

package org.radarbase.datadashboard.enhancer

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.datadashboard.config.HazelcastConfig
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class HibernatePersistenceResourceEnhancer(
    private val hazelcastConfig: HazelcastConfig,
) : JerseyResourceEnhancer {
    override fun AbstractBinder.enhance() {
        if (hazelcastConfig.enable) {
            System.setProperty("hazelcast.logging.type", "slf4j")
            val hzConfig = if (hazelcastConfig.configPath != null) {
                com.hazelcast.internal.config.ConfigLoader.load(hazelcastConfig.configPath)
            } else {
                Config().apply {
                    networkConfig = hazelcastConfig.network
                }
            }.apply {
                clusterName = hazelcastConfig.clusterName
                instanceName = hazelcastConfig.instanceName
            }

            val hazelcastInstance = Hazelcast.newHazelcastInstance(hzConfig)

            bind(hazelcastInstance)
                .to(HazelcastInstance::class.java)
                .`in`(Singleton::class.java)

        }
    }
}
