package org.ossiaustria.amigo.platform.domain

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@EntityScan("org.ossiaustria.amigo.platform.domain")
@EnableJpaRepositories("org.ossiaustria.amigo.platform.domain.repositories")
@Configuration
class DomainModule {
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
