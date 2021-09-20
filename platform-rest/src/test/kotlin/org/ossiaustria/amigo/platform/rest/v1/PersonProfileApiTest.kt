package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.services.PersonService
import org.ossiaustria.amigo.platform.rest.v1.user.ChangePersonDto
import org.ossiaustria.amigo.platform.rest.v1.user.PersonDto
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields

internal class PersonProfileApiTest : AbstractRestApiTest() {

    val rootUrl = "/v1/profile"

    @SpykBean
    protected lateinit var personService: PersonService

    @BeforeEach
    fun clearRepo() {
        mockUserAuthentication()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `profile should return user's profile`() {

        val person = this.performGet(rootUrl, accessToken = accessToken.token, personId = person1Id)
            .expectOk()
            .document("profile-success", responseFields(personFields()))
            .returns(PersonDto::class.java)

        assertEquals(person.id, person1Id)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `profile needs authentication`() {
        this.performGet(rootUrl).expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `changeMyProfile should change name and avatarUrl of User`() {

        val request = ChangePersonDto("name", "https://gitlab.com/uploads/-/system/user/avatar/3209720/avatar.png")

        val person = this.performPatch(rootUrl, accessToken = accessToken.token, body = request, personId = person1Id)
            .expectOk()
            .document(
                "profile-change-success",
                responseFields(personFields()),
                requestFields(
                    arrayListOf(
                        field("name", JsonFieldType.STRING, "New Fullname").optional(),
                        field("avatarUrl", JsonFieldType.STRING, "New URL of avatar").optional()
                    )
                ),
            ).returns(PersonDto::class.java)

        assertEquals(person.id, person1Id)
        assertEquals(person.name, request.name)
        assertEquals(person.avatarUrl, request.avatarUrl)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `changeMyProfile should change name of User`() {

        val request = ChangePersonDto("name", null)
        val person = this.performPatch(rootUrl, accessToken = accessToken.token, personId = person1Id,
            body = request)
            .returns(PersonDto::class.java)

        assertEquals(person.id, person1Id)
        assertEquals(person.name, request.name)
        assertEquals(person.avatarUrl, person1.avatarUrl)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `changeMyProfile should change avatarUrl of User`() {

        val request = ChangePersonDto(null, "https://gitlab.com/uploads/-/system/user/avatar/3209720/avatar.png")
        val person = this
            .performPatch(
                rootUrl,
                accessToken = accessToken.token,
                body = request,
                personId = person1Id
            )
            .returns(PersonDto::class.java)

        assertEquals(person.id, person1Id)
        assertEquals(person.name, person1.name)
        assertEquals(person.avatarUrl, request.avatarUrl)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `uploadAvatar should change avatarUrl of User`() {

        val avatarUrl = "newUrl"
        val file = MockMultipartFile("file", "content".toByteArray())

        // Cannot mock "RequestPart" name and file
        every { personService.uploadAvatar(eq(person1), any()) } returns
            person1.copy(avatarUrl = avatarUrl)

        val person = this.performPartPost("$rootUrl/avatar", accessToken.token, filePart = file, personId = person1Id)
            .expectOk()
            .document("profile-upload-avatar-success", responseFields(personFields()))
            .returns(PersonDto::class.java)

        assertEquals(person.id, person1Id)
        assertEquals(person.avatarUrl, avatarUrl)
    }
}
