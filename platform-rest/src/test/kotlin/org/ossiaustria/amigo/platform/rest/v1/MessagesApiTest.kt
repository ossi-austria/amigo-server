package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.services.sendables.MessageService
import org.ossiaustria.amigo.platform.rest.v1.sendables.MessageDto
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import java.time.ZonedDateTime
import java.util.*
import java.util.UUID.randomUUID

internal class MessagesApiTest : AbstractRestApiTest() {

    val baseUrl = "/v1/messages"

    @SpykBean
    lateinit var messageService: MessageService

    @BeforeEach
    fun before() {
        val id = account.person().id

        every { messageService.findWithReceiver(eq(id)) } returns listOf(
            mockMessage(senderId = randomUUID(), receiverId = id),
            mockMessage(senderId = randomUUID(), receiverId = id),
        )

        every { messageService.findWithSender(eq(id)) } returns listOf(
            mockMessage(senderId = id, receiverId = randomUUID()),
            mockMessage(senderId = id, receiverId = randomUUID()),
        )

        mockUserAuthentication()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `createMessage should return messages via receiverId`() {

        val senderId = randomUUID()
        val receiverId = randomUUID()
        val text = "expected"

        // cannot mock RequestBody "text"
        every { messageService.createMessage(eq(senderId), eq(receiverId), any()) } returns
                mockMessage(senderId = senderId, receiverId = receiverId, text = text)

        val url = "$baseUrl?receiverId=$receiverId&senderId=$senderId"

        val result = this.performPost(url, accessToken.token, body = text)
            .expectOk()
            .document(
                "messages-create",
                requestParameters(
                    param("receiverId", "UUID of receiver"),
                    param("senderId", "UUID of sender - must be your id"),
//                    param("text", "Text to send in message"),
                ),
                responseFields(messageResponseFields())
            )
            .returns(MessageDto::class.java)

        assertThat(result).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `filter should return messages via receiverId`() {

        val receiverId = account.person().id
        val senderId = randomUUID()

        val url = "$baseUrl/filter?receiverId=$receiverId&senderId=$senderId"

        every { messageService.findWithPersons(any(), eq(receiverId)) } returns listOf(
            mockMessage(senderId = senderId, receiverId = receiverId),
            mockMessage(senderId = senderId, receiverId = receiverId),
        )

        val returnsList = this.performGet(url, accessToken.token)
            .expectOk()
            .document(
                "messages-filter",
                requestParameters(
                    param("receiverId", "Filter for UUID of receiver").optional(),
                    param("senderId", "Filter for UUID of sender").optional()
                ),
                responseFields(messageResponseFields("[]."))
            )
            .returnsList(MessageDto::class.java)

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
        this.performGet("$baseUrl/filter?receiverId=${randomUUID()}", accessToken.token).expect4xx()
        this.performGet("$baseUrl/filter?senderId=${randomUUID()}", accessToken.token).expect4xx()
        this.performGet("$baseUrl/filter", accessToken.token).expect4xx()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `received should return messages received by current user`() {

        val result = this.performGet("$baseUrl/received", accessToken.token)
            .expectOk()
            .document("messages-received", responseFields(messageResponseFields("[].")))
            .returnsList(MessageDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.receiverId).isEqualTo(account.person().id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `sent should return messages sent by current user`() {

        val result = this.performGet("$baseUrl/sent", accessToken.token)
            .expectOk()
            .document("messages-sent", responseFields(messageResponseFields("[].")))
            .returnsList(MessageDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.senderId).isEqualTo(account.person().id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne should return message visible by current user`() {
        val msgId = randomUUID()

        every { messageService.getOne(msgId) } returns mockMessage(
            id = msgId, senderId = randomUUID(), receiverId = account.person().id
        )

        val result: Message = this.performGet("$baseUrl/$msgId", accessToken.token)
            .expectOk()
            .document("messages-one", responseFields(messageResponseFields()))
            .returns(Message::class.java)

        assertThat(result).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `set-retrieved should mark message as retrievedAt=now`() {
        val msgId = randomUUID()
        val senderId = randomUUID()

        every { messageService.getOne(eq(msgId)) } returns Message(
            id = msgId, senderId = senderId, receiverId = account.person().id, text = "text",
            retrievedAt = null
        )

        every { messageService.markAsRetrieved(eq(msgId), any()) } returns Message(
            id = msgId, senderId = senderId, receiverId = account.person().id, text = "text",
            retrievedAt = ZonedDateTime.now()
        )


        val result: MessageDto = this.performPatch("$baseUrl/$msgId/set-retrieved", accessToken.token)
            .expectOk()
            .document("messages-set-retrieved", responseFields(messageResponseFields()))
            .returns(MessageDto::class.java)

        assertThat(result).isNotNull
        assertThat(result.retrievedAt).isNotNull
        assertThat(result.sentAt).isNull()
    }

    private fun mockMessage(id: UUID = randomUUID(), senderId: UUID, receiverId: UUID, text: String = "text"): Message {
        return Message(id, senderId = senderId, receiverId = receiverId, text = text)
    }

    private fun messageResponseFields(prefix: String = ""): List<FieldDescriptor> {
        return arrayListOf(
            field(prefix + "id", JsonFieldType.STRING, "UUID"),
            field(prefix + "senderId", JsonFieldType.STRING, "UUID of sending Person"),
            field(prefix + "receiverId", JsonFieldType.STRING, "UUID of sending Person"),
            field(prefix + "text", JsonFieldType.STRING, "Text of this message"),
            field(prefix + "createdAt", JsonFieldType.STRING, "LocalDateTime of message creation"),
            field(prefix + "sentAt", JsonFieldType.STRING, "LocalDateTime of message sending process").optional(),
            field(
                prefix + "retrievedAt", JsonFieldType.STRING, "LocalDateTime of message marked as retrieved"
            ).optional(),
        )
    }
}
