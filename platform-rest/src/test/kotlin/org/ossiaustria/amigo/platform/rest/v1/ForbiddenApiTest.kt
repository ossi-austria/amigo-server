package org.ossiaustria.amigo.platform.rest.v1

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.test.annotation.Rollback
import javax.transaction.Transactional

internal class ForbiddenApiTest : AbstractRestApiTest() {

    @BeforeEach
    fun clearRepo() {
        truncateAllTables()
        accountSubjectPreparationTrait.apply()
        account = accountSubjectPreparationTrait.account

    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `must not access GET my groups without accessToken`() {
        mockUserAuthentication()
        this.performGet("/v1/groups/my").expectUnauthorized()

        mockUserAuthentication(accessTokenTime = 1)
        Thread.sleep(1000)
        this.performGet("/v1/groups/my", accessToken = accessToken.token).expectUnauthorized()
    }


}
