package org.ossiaustria.amigo.platform.config.http

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.ossiaustria.amigo.platform.security.APSessionsRepository
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer

@Configuration
@EnableRedisHttpSession
class SessionConfig(
    private val sessionRepository: FindByIndexNameSessionRepository<out Session>
) : AbstractHttpSessionApplicationInitializer() {

    @Bean("apSessionRepository")
    @Primary
    fun sessionRepository(): FindByIndexNameSessionRepository<out Session> {
        return APSessionsRepository(sessionRepository)
    }
}
