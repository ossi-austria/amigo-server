package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.enums.CallState
import org.ossiaustria.amigo.platform.domain.models.enums.CallType
import org.ossiaustria.amigo.platform.domain.services.sendables.CallService
import org.ossiaustria.amigo.platform.rest.v1.sendables.CallDto
import org.ossiaustria.amigo.platform.rest.v1.sendables.CallTokenDto
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import java.util.*
import java.util.UUID.randomUUID

internal class CallsApiTest : AbstractRestApiTest() {

    val baseUrl = "/v1/calls"

    @SpykBean
    lateinit var callService: CallService

    val msgId = randomUUID()

    @BeforeEach
    fun before() {
        every { callService.findWithReceiver(eq(person1Id)) } returns listOf(
            mockCall(senderId = person2Id, receiverId = person1Id),
            mockCall(senderId = person2Id, receiverId = person1Id),
        )

        every { callService.findWithSender(eq(person1Id)) } returns listOf(
            mockCall(senderId = person1Id, receiverId = person2Id),
            mockCall(senderId = person1Id, receiverId = person2Id),
        )

        every { callService.getOne(eq(msgId)) } returns Call(
            id = msgId,
            senderId = person2Id,
            receiverId = person1Id,
            callType = CallType.VIDEO,
            senderToken = "sender",
            receiverToken = "receiver"
        )
        mockUserAuthentication()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `createCall should return calls via receiverId`() {

        val senderId = person1Id
        val receiverId = person2Id

        every { callService.createCall(eq(senderId), eq(receiverId), any()) } returns
                mockCall(senderId = senderId, receiverId = receiverId)

        val url = "$baseUrl?receiverId=$receiverId&personId=$senderId&callType=VIDEO"

        val result = this.performPost(url, accessToken.token)
            .expectOk()
            .document(
                "calls-create",
                requestParameters(
                    param("receiverId", "UUID of receiver"),
                    param("personId", "UUID of sender - must be your id"),
                    param("callType", "VIDEO or AUDIO"),
                ),
                responseFields(callTokenResponseFields())
            )
            .returns(CallTokenDto::class.java)

        assertThat(result).isNotNull
        assertThat(result.callState).isEqualTo(CallState.CREATED)
        assertThat(result.senderId).isEqualTo(senderId)
        assertThat(result.receiverId).isEqualTo(receiverId)
        assertThat(result.token).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `createCall needs authentication`() {
        val url = "$baseUrl?receiverId=${randomUUID()}&senderId=${randomUUID()}&callType=VIDEO"
        this.performPost(url).expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `filter should return calls via receiverId`() {

        val receiverId = account.primaryPerson().id
        val senderId = randomUUID()

        val url = "$baseUrl/filter?receiverId=$receiverId&senderId=$senderId"

        every { callService.findWithPersons(any(), eq(receiverId)) } returns listOf(
            mockCall(senderId = senderId, receiverId = receiverId),
            mockCall(senderId = senderId, receiverId = receiverId),
        )

        val returnsList = this.performGet(url, accessToken.token)
            .expectOk()
            .document(
                "calls-filter",
                requestParameters(
                    param("receiverId", "Filter for UUID of receiver").optional(),
                    param("senderId", "Filter for UUID of sender").optional()
                ),
                responseFields(callResponseFields("[]."))
            )
            .returnsList(CallDto::class.java)

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
    fun `filter needs authentication`() {
        this.performGet("$baseUrl/filter?receiverId=${randomUUID()}").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `received should return calls received by current user`() {

        val result = this.performGet("$baseUrl/received", accessToken.token, person1Id)
            .expectOk()
            .document(
                "calls-received",
                responseFields(callResponseFields("[].")),
                requestParameters(optionalPersonId())
            )
            .returnsList(CallDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.receiverId).isEqualTo(account.primaryPerson().id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `received needs authentication`() {
        this.performPost("$baseUrl/received").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `sent should return calls sent by current user`() {

        val result = this.performGet("$baseUrl/sent", accessToken.token, person1Id)
            .expectOk()
            .document(
                "calls-sent",
                responseFields(callResponseFields("[].")),
                requestParameters(optionalPersonId())
            )
            .returnsList(CallDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.senderId).isEqualTo(account.primaryPerson().id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `sent needs authentication`() {
        this.performGet("$baseUrl/sent").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne should return call visible by current user`() {
        val id = randomUUID()

        every { callService.getOne(id) } returns mockCall(
            id = id, senderId = randomUUID(), receiverId = account.primaryPerson().id
        )

        val result: CallTokenDto = this.performGet("$baseUrl/$id", accessToken.token, person1Id)
            .expectOk()
            .document(
                "calls-one",
                responseFields(callTokenResponseFields()),
                requestParameters(optionalPersonId())
            )
            .returns(CallTokenDto::class.java)

        assertThat(result).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne needs authentication`() {
        this.performGet("$baseUrl/${randomUUID()}").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `accept should start call`() {
        val result: CallTokenDto = this.performPatch("$baseUrl/$msgId/accept", accessToken.token, person1Id)
            .expectOk()
            .document(
                "calls-accept",
                responseFields(callTokenResponseFields()),
                requestParameters(optionalPersonId())
            )
            .returns(CallTokenDto::class.java)

        assertThat(result).isNotNull
        assertThat(result.retrievedAt).isNotNull
        assertThat(result.startedAt).isNotNull
        assertThat(result.callState).isEqualTo(CallState.ACCEPTED)
        assertThat(result.senderId).isEqualTo(person2Id)
//        assertThat(result.token).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `accept needs authentication`() {
        this.performPatch("$baseUrl/${randomUUID()}/accept",personId = person1Id).expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `cancel should cancel call`() {
        // cancel must be executed by sender
        every { callService.getOne(eq(msgId)) } returns Call(
            id = msgId,
            senderId = account.primaryPerson().id,
            receiverId = person2Id,
            callType = CallType.VIDEO,
        )

        val result: CallDto = this.performPatch("$baseUrl/$msgId/cancel", accessToken.token, person1Id)
            .expectOk()
            .document(
                "calls-cancel",
                responseFields(callResponseFields()),
                requestParameters(optionalPersonId())
            )
            .returns(CallDto::class.java)

        assertThat(result).isNotNull
        assertThat(result.startedAt).isNull()
        assertThat(result.callState).isEqualTo(CallState.CANCELLED)
//        assertThat(result.senderId).isEqualTo(senderId)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `cancel needs authentication`() {
        this.performPatch("$baseUrl/${randomUUID()}/cancel").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `deny should deny call`() {
        val result: CallDto = this.performPatch("$baseUrl/$msgId/deny", accessToken.token, person1Id)
            .expectOk()
            .document(
                "calls-deny",
                responseFields(callResponseFields()),
                requestParameters(optionalPersonId())
            )
            .returns(CallDto::class.java)

        assertThat(result).isNotNull
        assertThat(result.retrievedAt).isNotNull
        assertThat(result.startedAt).isNull()
        assertThat(result.callState).isEqualTo(CallState.DENIED)
        assertThat(result.senderId).isEqualTo(person2Id)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `deny needs authentication`() {
        this.performPatch("$baseUrl/${randomUUID()}/deny").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `finish should stop call`() {

        val result: CallDto = this.performPatch("$baseUrl/$msgId/finish", accessToken.token, person1Id)
            .expectOk()
            .document(
                "calls-finish",
                responseFields(callResponseFields()),
                requestParameters(optionalPersonId())
            )
            .returns(CallDto::class.java)

        assertThat(result).isNotNull
        assertThat(result.retrievedAt).isNotNull
        assertThat(result.finishedAt).isNotNull
        assertThat(result.callState).isEqualTo(CallState.FINISHED)
        assertThat(result.senderId).isEqualTo(person2Id)
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `finish needs authentication`() {
        this.performPatch("$baseUrl/${randomUUID()}/finish").expectUnauthorized()
    }

    private fun mockCall(id: UUID = randomUUID(), senderId: UUID, receiverId: UUID): Call {
        return Call(
            id,
            senderId = senderId,
            receiverId = receiverId,
            callType = CallType.VIDEO,
            receiverToken = "receiver",
            senderToken = "sender"
        )
    }

    private fun callResponseFields(prefix: String = ""): List<FieldDescriptor> {
        return arrayListOf(
            field(prefix + "id", JsonFieldType.STRING, "UUID"),
            field(prefix + "senderId", JsonFieldType.STRING, "UUID of sending Person"),
            field(prefix + "receiverId", JsonFieldType.STRING, "UUID of receiving Person"),
            field(prefix + "callType", JsonFieldType.STRING, "VIDEO or AUDIO"),
            field(
                prefix + "callState", JsonFieldType.STRING,
                "CallState: CREATED, CALLING, CANCELLED, DENIED, ACCEPTED, STARTED, FINISHED, TIMEOUT"
            ),
            field(prefix + "startedAt", JsonFieldType.STRING, "LocalDateTime of start").optional(),
            field(prefix + "finishedAt", JsonFieldType.STRING, "LocalDateTime of finish").optional(),
            field(prefix + "createdAt", JsonFieldType.STRING, "LocalDateTime of call creation"),
            field(prefix + "sentAt", JsonFieldType.STRING, "LocalDateTime of call sending process").optional(),
            field(prefix + "retrievedAt", JsonFieldType.STRING, "LocalDateTime of call marked as retrieved").optional(),
        )
    }

    private fun callTokenResponseFields(prefix: String = ""): List<FieldDescriptor> {
        return callResponseFields(prefix).toMutableList().apply {
            add(
                field(prefix + "token", JsonFieldType.STRING, "Jitsi JWT Token to authenticate person")
            )
        }
    }
}
