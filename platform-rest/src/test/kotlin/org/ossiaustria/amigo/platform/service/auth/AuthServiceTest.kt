package org.ossiaustria.amigo.platform.service.auth

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.rest.v1.TestTags
import org.ossiaustria.amigo.platform.service.AbstractServiceTest
import org.ossiaustria.amigo.platform.services.auth.AuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.annotation.Rollback
import javax.transaction.Transactional

class AuthServiceTest : AbstractServiceTest() {

    @Autowired
    private lateinit var authService: AuthService

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `should generate valid accessToken`() {

    }
}