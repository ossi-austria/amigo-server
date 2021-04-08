package org.ossiaustria.amigo.platform

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

object ApplicationProfiles {
    const val TEST = "test"
    const val INTEGRATION_TEST = "integration-test"
    const val DEV = "dev"
    const val DOCKER = "docker"
    const val PROD = "prod"
}

@SpringBootApplication(scanBasePackages = ["org.ossiaustria.amigo.platform"])
@EntityScan("org.ossiaustria.amigo.platform.domain")
@EnableJpaRepositories("org.ossiaustria.amigo.platform.repositories")
@EnableConfigurationProperties(ApplicationConfiguration::class)
class RestApplication

fun main(args: Array<String>) {
    runApplication<RestApplication>(*args)
}


@ConfigurationProperties(prefix = "amigo-platform")
class ApplicationConfiguration()



