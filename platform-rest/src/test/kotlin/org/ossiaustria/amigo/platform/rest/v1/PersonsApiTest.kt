package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.services.PersonAvatar
import org.ossiaustria.amigo.platform.domain.services.PersonService
import org.springframework.core.io.FileUrlResource
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

internal class PersonsApiTest : AbstractRestApiTest() {

    val rootUrl = "/v1/persons/"

    @SpykBean
    protected lateinit var personService: PersonService

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
        this.performGet(url, accessToken = accessToken.token).andExpect(MockMvcResultMatchers.status().isNotFound)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `avatar should redirect when no resource is available`() {

        every { personService.loadAvatar(eq(person1), eq(person2Id)) } returns
                PersonAvatar(person2.copy(avatarUrl = "avatar.jpg"), resource = null)

        val url = "$rootUrl$person2Id/avatar.jpg"
        this.performGet(url, accessToken = accessToken.token)
            .document("persons-avatar-success")
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection)
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
        this.performGet(url, accessToken = accessToken.token).expectOk()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `avatar needs authentication`() {
        this.performGet(rootUrl).expectUnauthorized()
    }

}
