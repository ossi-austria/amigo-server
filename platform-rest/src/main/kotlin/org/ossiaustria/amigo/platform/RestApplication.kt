package org.ossiaustria.amigo.platform

import org.ossiaustria.amigo.platform.domain.DomainModule
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(
    scanBasePackages = ["org.ossiaustria.amigo.platform"],
)
@EnableConfigurationProperties(ApplicationConfiguration::class)
@Import(DomainModule::class)
class RestApplication

fun main(args: Array<String>) {
    runApplication<RestApplication>(*args)
}


@ConfigurationProperties(prefix = "amigo-platform")
@EnableScheduling
class ApplicationConfiguration



