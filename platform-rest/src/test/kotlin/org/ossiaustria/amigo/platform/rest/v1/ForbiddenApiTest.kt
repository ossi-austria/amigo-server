package org.ossiaustria.amigo.platform.rest.v1

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

internal class ForbiddenApiTest : AbstractRestApiTest() {

    @BeforeEach
    fun clearRepo() {
    }

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
