package org.ossiaustria.amigo.platform.domain

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Import

@ConfigurationProperties(prefix = "amigo-platform")
class ApplicationConfiguration()


@SpringBootApplication(
    scanBasePackages = ["org.ossiaustria.amigo.platform.domain"],
)
@EnableConfigurationProperties(ApplicationConfiguration::class)
@Import(DomainModule::class)
class TestDomainApplication