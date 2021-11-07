package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.services.multimedia.MultimediaService
import org.ossiaustria.amigo.platform.rest.v1.multimedias.MultimediaDto
import org.ossiaustria.amigo.platform.testcommons.Mocks
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.UUID.randomUUID

internal class MultimediasApiTest : AbstractRestApiTest() {

    val baseUrl = "/v1/multimedias"

    @SpykBean
    lateinit var multimediaService: MultimediaService

    @BeforeEach
    fun before() {

        every { multimediaService.findWithOwner(eq(person1Id)) } returns listOf(
            Mocks.multimedia(ownerId = person1Id),
            Mocks.multimedia(ownerId = person1Id),
        )

        mockUserAuthentication()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `createMultimedia should handle MultipartFile and upload it`() {

        val ownerId = person1Id
        val name = "newname"
        val file = MockMultipartFile("file", "content".toByteArray())

        // Cannot mock "RequestPart" name and file
        every { multimediaService.createMultimedia(eq(ownerId), any(), any(), any()) } returns
            Mocks.multimedia(ownerId = ownerId, filename = name)

        val url = "$baseUrl?ownerId=$ownerId"

        val result = this.performPartPost(url, accessToken.token, filePart = file)
            .expectOk()
            .document(
                "multimedias-create",
                requestParameters(
                    param("ownerId", "UUID of owner - must be your person's id"),
                    param("albumId", "Text to send in message").optional(),
                ),
                responseFields(multimediasResponseFields())
            )
            .returns(MultimediaDto::class.java)

        assertThat(result).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `update Multimedia with new file should handle MultipartFile and upload it`() {

        val ownerId = person1Id
        val name = "newname"
        val file = MockMultipartFile("file", "content".toByteArray())

        // Cannot mock "RequestPart" name and file
        every { multimediaService.createMultimedia(eq(ownerId), any(), any(), any()) } returns
            Mocks.multimedia(ownerId = ownerId, filename = name)

        every { multimediaService.getOne(any()) } returns
            Mocks.multimedia(ownerId = ownerId, filename = name)

        every { multimediaService.uploadFile(any(), any()) } returns
            Mocks.multimedia(ownerId = ownerId, filename = name)

        val first = this.performPartPost("$baseUrl?ownerId=$ownerId", accessToken.token, filePart = file)
            .returns(MultimediaDto::class.java)

        val second = this.performPartPost("$baseUrl/${first.id}/file", accessToken.token, filePart = file)
            .expectOk()
            .document(
                "multimedias-update-file",
                responseFields(multimediasResponseFields())
            )
            .returns(MultimediaDto::class.java)
        assertThat(second).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `createMultimedia needs authentication`() {
        val url = "$baseUrl?receiverId=${randomUUID()}&ownerId=${randomUUID()}&callType=VIDEO"
        this.performPost(url).expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `own should return multimedias sent by current user`() {

        val result = this.performGet("$baseUrl/own", accessToken.token)
            .expectOk()
            .document(
                "multimedias-own",
                responseFields(multimediasResponseFields("[]."))
            )
            .returnsList(MultimediaDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.ownerId).isEqualTo(account.primaryPerson().id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `own needs authentication`() {
        this.performGet("$baseUrl/own").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne should return Multimedia visible by current user`() {
        val msgId = randomUUID()

        every { multimediaService.getOne(msgId) } returns Mocks.multimedia(id = msgId, ownerId = person1Id)

        val result: MultimediaDto = this.performGet("$baseUrl/$msgId", accessToken.token, person1Id)
            .expectOk()
            .document(
                "multimedias-one",
                responseFields(multimediasResponseFields())
            )
            .returns(MultimediaDto::class.java)

        assertThat(result).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `download file should return Multimedia's file`() {
        val msgId = randomUUID()
        every { multimediaService.getOne(msgId) } returns Mocks.multimedia(id = msgId, ownerId = person1Id)
        every { multimediaService.loadFile(any()) } returns
            ClassPathResource("classpath:application-test.xml")

        this.performGet("$baseUrl/$msgId/file", accessToken.token, person1Id)
            .expectOk()
            .document("multimedias-get-file")

    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `public download file should return Multimedia's file`() {
        val msgId = randomUUID()
        every { multimediaService.getOne(msgId) } returns Mocks.multimedia(id = msgId, ownerId = person1Id)
        every { multimediaService.loadFile(any()) } returns
            ClassPathResource("classpath:application-test.xml")

        this.performGet("$baseUrl/$msgId/public/filename")
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `public download file should return redirect when no Multimedia is available`() {
        val msgId = randomUUID()
        every { multimediaService.getOne(msgId) } returns Mocks.multimedia(id = msgId, ownerId = person1Id)
        every { multimediaService.loadFile(any()) } returns
            ClassPathResource("classpath:application-test.xml")

        this.performGet("$baseUrl/$msgId/public/filename")
            .document("multimedias-get-file-public")
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne needs authentication`() {
        this.performGet("$baseUrl/${randomUUID()}").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne-File needs no authentication`() {

        val msgId = randomUUID()

        every { multimediaService.getOne(msgId) } returns Mocks.multimedia(id = msgId, ownerId = person1Id)

        every { multimediaService.loadFile(any()) } returns
            ClassPathResource("classpath:application-test.xml")

        this.performGet("$baseUrl").expectUnauthorized()
        this.performGet("$baseUrl/$msgId").expectUnauthorized()
        this.performGet("$baseUrl/$msgId/file").expectUnauthorized()
        this.performGet("$baseUrl/$msgId/public/filename").expectOk()
        this.performGet("$baseUrl/$msgId/public/1234").isNotFound()

    }
}
