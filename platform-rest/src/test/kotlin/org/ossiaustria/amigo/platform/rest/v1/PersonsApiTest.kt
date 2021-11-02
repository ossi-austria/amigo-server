package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.services.PersonAvatar
import org.ossiaustria.amigo.platform.domain.services.PersonProfileService
import org.springframework.core.io.FileUrlResource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

internal class PersonsApiTest : AbstractRestApiTest() {

    val rootUrl = "/v1/persons/"

    @SpykBean
    protected lateinit var personService: PersonProfileService

    @BeforeEach
    fun clearRepo() {
        mockUserAuthentication()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `avatar should return 404 when no avatar is available`() {

        every { personService.loadAvatar(eq(person1), eq(person2Id)) } returns
            PersonAvatar(person2.copy(avatarUrl = null), resource = null)

        val url = "$rootUrl$person2Id/avatar.jpg"
        this.performGet(url, accessToken = accessToken.token, personId = person1Id)
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `avatar should redirect when no resource is available`() {

        every { personService.loadAvatar(eq(person1), eq(person2Id)) } returns
            PersonAvatar(person2.copy(avatarUrl = "avatar.jpg"), resource = null)

        val url = "$rootUrl$person2Id/avatar.jpg"
        this.performGet(url, accessToken = accessToken.token, personId = person1Id)
            .document("persons-avatar-success")
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `publicAvatar should return 404 when provided $key was wrong`() {

        every { personService.loadAvatar(any(), eq(person2Id)) } returns
            PersonAvatar(person2.copy(avatarUrl = "1234.jpg"), resource = null)

        this.performGet("$rootUrl$person2Id/public/1235.jpg")
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `publicAvatar should return redirect when no avatar resource is available`() {

        every { personService.loadAvatar(any(), eq(person2Id)) } returns
            PersonAvatar(person2.copy(avatarUrl = "1234.jpg"), resource = null)

        this.performGet("$rootUrl$person2Id/public/1234.jpg")
            .document("persons-avatar-public-success")
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)

    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `publicAvatar should return 404 when no avatar is available`() {

        every { personService.loadAvatar(any(), eq(person2Id)) } returns
            PersonAvatar(person2.copy(avatarUrl = null), resource = null)

        this.performGet("$rootUrl$person2Id/public/1234.jpg")
            .andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    @Disabled("Cannot be mocked right now")
    fun `avatar should return image when resource is available`() {

        every { personService.loadAvatar(eq(person1), eq(person2Id)) } returns
            PersonAvatar(
                person2.copy(avatarUrl = "avatar.jpg"),
                resource = FileUrlResource("application-test.xml")
            )

        val url = "$rootUrl$person2Id/avatar.jpg"
        this.performGet(url, accessToken = accessToken.token, personId = person1Id).expectOk()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `avatar needs no authentication`() {
        this.performGet(rootUrl).expectUnauthorized()
        this.performGet("$rootUrl$person2Id").expectUnauthorized()
        this.performGet("$rootUrl$person2Id/404.jpg").expectUnauthorized()
        this.performGet("$rootUrl$person2Id/public/404.jpg").isNotFound()
    }

}
