package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.rest.v1.auth.PasswordResetRequest
import org.ossiaustria.amigo.platform.services.email.TemplateService
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters

internal class PasswordApiTest : AbstractRestApiTest() {

    val baseUrl = "/v1/auth/password"

    @SpykBean
    lateinit var templateService: TemplateService

    @BeforeEach
    @AfterEach
    fun clearRepo() {
        every { templateService.createPasswordResetTemplateHtml(any()) } returns "Generated email template"
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `Can request password reset by email`() {
//        val existingUser = createMockUser()

        val url = "$baseUrl/reset?email=${account.email}"

        this.performPost(url)
            .expectNoContent()
            .document(
                "password-reset-successful",
                requestParameters(
                    parameterWithName("user_id").optional().description("Internal User id - UUID"),
                    parameterWithName("email").optional().description("User email"),
                    parameterWithName("user_name").optional().description("Username")
                )
            )
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `Cannot request password reset for non existing user`() {
        val url = "$baseUrl/reset?email=notexistingemail@example.com"

        this.performPost(url).isNotFound()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `should confirm password reset by email`() {
//        val existingUser = createMockUser("0001")

        val changedUser = accountService.requestPasswordChange(account)

        val resetConfirmRequest = PasswordResetRequest(changedUser.changeAccountToken!!, "NEW-PASSWORD")

        val url = "$baseUrl/reset/confirm"

        this.performPost(url, body = resetConfirmRequest)
            .expectOk()
            .document(
                "password-reset-confirmation",
                requestFields(confirmPasswordResetFields())
            )
            .returns(Boolean::class.java)
    }

    fun confirmPasswordResetFields(): List<FieldDescriptor> {
        return listOf(
            fieldWithPath("token").type(JsonFieldType.STRING).optional().description("Token gotten from url"),
            fieldWithPath("password").type(JsonFieldType.STRING).optional().description("New user password")
        )
    }
}
