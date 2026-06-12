package org.radarbase.datadashboard.api.enhancer

import com.hazelcast.config.Config
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.datadashboard.api.config.HazelcastConfig
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
