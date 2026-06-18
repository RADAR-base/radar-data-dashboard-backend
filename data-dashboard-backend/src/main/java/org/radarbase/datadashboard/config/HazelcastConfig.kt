package org.radarbase.datadashboard.config

import com.hazelcast.config.NetworkConfig

data class HazelcastConfig(
    val enable: Boolean = false,
    val configPath: String? = null,
    val instanceName: String = "data-dashboard-backend",
    val clusterName: String = "data-dashboard-backend",
    val network: NetworkConfig = NetworkConfig().apply {
        // Defaults; overridden by the configuration file.
        port = 5701
        portCount = 1
        join.apply {
            multicastConfig.apply {
                isEnabled = true
                multicastPort = 53215
            }
        }
    },
) {
    fun withEnv() = copy(
        configPath = System.getenv("HAZELCAST_CONFIG_PATH") ?: configPath,
        instanceName = System.getenv("HAZELCAST_INSTANCE_NAME") ?: instanceName,
        clusterName = System.getenv("HAZELCAST_CLUSTER_NAME") ?: clusterName,
    )
}
