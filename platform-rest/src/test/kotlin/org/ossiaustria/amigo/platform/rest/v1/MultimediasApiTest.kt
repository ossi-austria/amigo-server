package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import org.ossiaustria.amigo.platform.domain.services.sendables.MultimediaService
import org.ossiaustria.amigo.platform.rest.v1.sendables.MultimediaDto
import org.springframework.mock.web.MockMultipartFile
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType.NUMBER
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import java.time.ZonedDateTime
import java.util.*
import java.util.UUID.randomUUID

internal class MultimediasApiTest : AbstractRestApiTest() {

    val baseUrl = "/v1/multimedias"

    @SpykBean
    lateinit var multimediaService: MultimediaService

    @BeforeEach
    fun before() {
        val id = account.person().id

        every { multimediaService.findWithReceiver(eq(id)) } returns listOf(
            mockMultimedia(senderId = randomUUID(), receiverId = id),
            mockMultimedia(senderId = randomUUID(), receiverId = id),
        )

        every { multimediaService.findWithSender(eq(id)) } returns listOf(
            mockMultimedia(senderId = id, receiverId = randomUUID()),
            mockMultimedia(senderId = id, receiverId = randomUUID()),
        )

        mockUserAuthentication()
    }


    @Test
    @Tag(TestTags.RESTDOC)
    fun `createMultimedia should handle MultipartFile and upload it`() {

        val senderId = randomUUID()
        val receiverId = randomUUID()
        val name = "newname"
        val file = MockMultipartFile("file", "content".toByteArray())

        // Cannot mock "RequestPart" name and file
        every { multimediaService.createMultimedia(eq(senderId), eq(receiverId), any(), any(), any()) } returns
                mockMultimedia(senderId = senderId, receiverId = receiverId, filename = name)

        val url = "$baseUrl?receiverId=$receiverId&senderId=$senderId"

        val result = this.performPartPost(url, accessToken.token, file = file)
            .expectOk()
            .document(
                "multimedias-create",
                requestParameters(
                    param("receiverId", "UUID of receiver"),
                    param("senderId", "UUID of sender - must be your id"),
                    param("albumId", "Text to send in message").optional(),
                ),
                responseFields(multimediasResponseFields())
            )
            .returns(MultimediaDto::class.java)

        assertThat(result).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `filter should return multimedias via receiverId`() {

        val receiverId = account.person().id
        val senderId = randomUUID()

        val url = "$baseUrl/filter?receiverId=$receiverId&senderId=$senderId"

        every { multimediaService.findWithPersons(any(), eq(receiverId)) } returns listOf(
            mockMultimedia(senderId = senderId, receiverId = receiverId),
            mockMultimedia(senderId = senderId, receiverId = receiverId),
        )


        val returnsList = this.performGet(url, accessToken.token)
            .expectOk()
            .document(
                "multimedias-filter",
                requestParameters(
                    param("receiverId", "Filter for UUID of receiver").optional(),
                    param("senderId", "Filter for UUID of sender").optional()
                ),
                responseFields(multimediasResponseFields("[]."))
            )
            .returnsList(MultimediaDto::class.java)

        assertThat(returnsList).isNotNull
        assertThat(returnsList).isNotEmpty
        returnsList.forEach {
            assertThat(it.receiverId).isEqualTo(receiverId)
            assertThat(it.senderId).isEqualTo(senderId)
        }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `filter should return error when called with arbitrary persons`() {
        val url = "$baseUrl/filter?receiverId=${randomUUID()}"
        this.performGet(url, accessToken.token).expect4xx()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `received should return multimedias received by current user`() {

        val result = this.performGet("$baseUrl/received", accessToken.token)
            .expectOk()
            .document("multimedias-received", responseFields(multimediasResponseFields("[].")))
            .returnsList(MultimediaDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.receiverId).isEqualTo(account.person().id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `sent should return multimedias sent by current user`() {

        val result = this.performGet("$baseUrl/sent", accessToken.token)
            .expectOk()
            .document("multimedias-sent", responseFields(multimediasResponseFields("[].")))
            .returnsList(MultimediaDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.senderId).isEqualTo(account.person().id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne should return Multimedia visible by current user`() {
        val msgId = randomUUID()

        every { multimediaService.getOne(msgId) } returns mockMultimedia(
            id = msgId, senderId = randomUUID(), receiverId = account.person().id
        )

        val result: Multimedia = this.performGet("$baseUrl/$msgId", accessToken.token)
            .expectOk()
            .document("multimedias-one", responseFields(multimediasResponseFields()))
            .returns(Multimedia::class.java)

        assertThat(result).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `action=retrieved should mark Multimedia as retrievedAt=now`() {
        val msgId = randomUUID()
        val senderId = randomUUID()

        every { multimediaService.getOne(eq(msgId)) } returns mockMultimedia(
            id = msgId, senderId = senderId, receiverId = account.person().id,
        )

        every { multimediaService.markAsRetrieved(eq(msgId), any()) } returns mockMultimedia(
            id = msgId, senderId = senderId, receiverId = account.person().id,
            retrievedAt = ZonedDateTime.now()
        )

        every { multimediaService.markAsSent(eq(msgId), any()) } returns mockMultimedia(
            id = msgId, senderId = senderId, receiverId = account.person().id,
            retrievedAt = ZonedDateTime.now(),
            sentAt = ZonedDateTime.now(),
        )

        val result: MultimediaDto = this.performPatch("$baseUrl/$msgId/set-retrieved", accessToken.token)
            .expectOk()
            .document("multimedias-set-retrieved", responseFields(multimediasResponseFields()))
            .returns(MultimediaDto::class.java)

        assertThat(result).isNotNull
        assertThat(result.retrievedAt).isNotNull
        assertThat(result.sentAt).isNull()
    }

    private fun mockMultimedia(
        id: UUID = randomUUID(),
        senderId: UUID,
        receiverId: UUID,
        sentAt: ZonedDateTime? = null,
        retrievedAt: ZonedDateTime? = null,
        filename: String = "filename",
    ): Multimedia {
        return Multimedia(
            id, senderId = senderId,
            receiverId = receiverId,
            ownerId = senderId,
            filename = filename,
            type = MultimediaType.IMAGE,
            sentAt = sentAt,
            retrievedAt = retrievedAt,

            )
    }

    private fun multimediasResponseFields(prefix: String = ""): List<FieldDescriptor> {
        return arrayListOf(
            field(prefix + "id", STRING, "UUID"),
            field(prefix + "senderId", STRING, "UUID of sending Person"),
            field(prefix + "receiverId", STRING, "UUID of sending Person"),
            field(prefix + "createdAt", STRING, "LocalDateTime of Multimedia creation"),
            field(prefix + "sentAt", STRING, "LocalDateTime of Multimedia sending process").optional(),
            field(
                prefix + "retrievedAt", STRING, "LocalDateTime of Multimedia marked as retrieved"
            ).optional(),
            field(prefix + "ownerId", STRING, "UUID of Owner"),
            field(prefix + "filename", STRING, "File to name that file locally"),
            field(prefix + "type", STRING, "MultimediaType: IMAGE, VIDEO, AUDIO"),
            field(prefix + "contentType", STRING, "ContentType / MIME type of that file").optional(),
            field(prefix + "size", NUMBER, "Size of file in bytes").optional(),
            field(prefix + "albumId", STRING, "UUID of parent Album").optional(),

            )
    }
}
