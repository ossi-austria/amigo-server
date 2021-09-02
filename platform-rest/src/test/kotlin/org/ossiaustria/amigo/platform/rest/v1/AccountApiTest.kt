package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.ossiaustria.amigo.platform.rest.v1.user.SetFcmTokenRequest
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields

internal class AccountApiTest : AbstractRestApiTest() {

    val baseUrl = "/v1/account"

    @SpykBean
    lateinit var authService: AuthService

    @BeforeEach
    fun before() {
        every { authService.setFcmToken(eq(account.id), eq("fcm")) } returns account.copy(fcmToken = "fcm")
        mockUserAuthentication()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `setFcmToken should save fcm token in Account`() {

        this.performPost("$baseUrl/set-fcm-token", accessToken.token, SetFcmTokenRequest("fcm"))
            .expectOk()
            .document(
                "account-set-fcm-token",
                requestFields(fcmTokenRequestFields()),
            )
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `setFcmToken needs authentication`() {
        this.performPost("$baseUrl/set-fcm-token", body = SetFcmTokenRequest("fcm")).expectUnauthorized()
    }

    private fun fcmTokenRequestFields(prefix: String = ""): List<FieldDescriptor> {
        return arrayListOf(
            field(prefix + "fcmToken", JsonFieldType.STRING, "Secret token for FCM client messages"),
        )
    }
}
