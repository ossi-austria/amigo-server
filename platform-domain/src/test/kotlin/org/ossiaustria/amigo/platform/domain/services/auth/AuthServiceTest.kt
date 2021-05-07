package org.ossiaustria.amigo.platform.domain.services.auth

import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.services.AbstractServiceTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Rollback
import javax.transaction.Transactional

class AuthServiceTest : AbstractServiceTest() {

    @Autowired
    private lateinit var authService: AuthService

    @Transactional
    @Rollback
    @Test
    fun `should generate valid accessToken`() {

    }
}