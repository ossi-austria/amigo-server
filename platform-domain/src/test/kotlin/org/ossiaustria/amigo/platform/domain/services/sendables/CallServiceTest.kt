package org.ossiaustria.amigo.platform.domain.services.sendables

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.enums.CallState
import org.ossiaustria.amigo.platform.domain.models.enums.CallType
import org.ossiaustria.amigo.platform.domain.repositories.CallRepository
import org.ossiaustria.amigo.platform.domain.services.SecurityError
import org.ossiaustria.amigo.platform.domain.services.jitsi.JitsiJwtService
import org.ossiaustria.amigo.platform.domain.services.messaging.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID.randomUUID

internal class CallServiceTest : SendableServiceTest<Call, CallService>() {

    @Autowired
    override lateinit var service: CallService

    @Autowired
    private lateinit var callRepository: CallRepository

    @MockkBean
    private lateinit var notificationService: NotificationService

    @MockkBean
    private lateinit var jitsiJwtService: JitsiJwtService

    private var token1 = "token1"
    private var token2 = "token2"

    @BeforeEach
    fun beforeEach() {
        cleanTables()
        callRepository.deleteAll()

        mockPersons()

        every { notificationService.callChanged(eq(personId1), any()) } returns true
        every { notificationService.callChanged(eq(personId2), any()) } returns true

        every { jitsiJwtService.generateToken(any(), eq(person1), eq(person2)) } returns token1
        every { jitsiJwtService.generateToken(any(), eq(person2), eq(person1)) } returns token2

        callRepository.save(Call(existingId, personId1, personId2, CallType.VIDEO))
        callRepository.save(Call(randomUUID(), personId2, personId1, CallType.VIDEO))
    }

    @Test
    fun `createCall should create a Call with proper sender and receiver`() {

        val result = service.createCall(personId1, personId2, CallType.VIDEO)
        verify { notificationService.callChanged(eq(personId2), any()) }

        assertThat(result).isNotNull
        assertThat(result.senderId).isEqualTo(personId1)
        assertThat(result.receiverId).isEqualTo(personId2)
    }

    @Test
    fun `createCall should create a Call with createdAt != null`() {

        val result = service.createCall(personId1, personId2, CallType.VIDEO)
        verify { notificationService.callChanged(eq(personId2), any()) }

        assertThat(result.createdAt).isNotNull
        assertThat(result.retrievedAt).isNull()
        assertThat(result.startedAt).isNull()
        assertThat(result.finishedAt).isNull()
    }

    @Test
    fun `createCall should generate jitsi JWT tokens`() {

        val result = service.createCall(personId1, personId2, CallType.VIDEO)

        verify { jitsiJwtService.generateToken(any(), eq(person1), eq(person2)) }
        verify { jitsiJwtService.generateToken(any(), eq(person2), eq(person1)) }

        assertThat(result.senderToken).isEqualTo(token1)
        assertThat(result.receiverToken).isEqualTo(token2)

    }

    @Test
    fun `denyCall should copy Call and set state = DENIED`() {

        val call = service.createCall(personId1, personId2, CallType.VIDEO)
        verify { notificationService.callChanged(eq(personId2), any()) }

        val result = service.denyCall(call)
        verify { notificationService.callChanged(eq(personId1), any()) }

        assertCallsEquals(result, call)
        assertThat(result.retrievedAt).isNotNull
        assertThat(result.callState).isEqualTo(CallState.DENIED)
        assertThat(result.startedAt).isNull()
        assertThat(result.finishedAt).isNull()
    }

    @Test
    fun `cancelCall should copy Call and set state = CANCELLED`() {

        val call = service.createCall(personId1, personId2, CallType.VIDEO)
        verify { notificationService.callChanged(eq(personId2), any()) }

        val result = service.cancelCall(call)
        verify { notificationService.callChanged(eq(personId2), any()) }

        assertCallsEquals(result, call)
        assertThat(result.retrievedAt).isNull()
        assertThat(result.callState).isEqualTo(CallState.CANCELLED)
        assertThat(result.startedAt).isNull()
        assertThat(result.finishedAt).isNull()
    }

    @Test
    fun `acceptCall should copy Call and set state = ACCEPTED`() {

        val call = service.createCall(personId1, personId2, CallType.VIDEO)
        verify { notificationService.callChanged(eq(personId2), any()) }

        val result = service.acceptCall(call)
        verify { notificationService.callChanged(eq(personId1), any()) }

        assertCallsEquals(result, call)
        assertThat(result.retrievedAt).isNotNull
        assertThat(result.callState).isEqualTo(CallState.ACCEPTED)
        assertThat(result.startedAt).isNotNull
        assertThat(result.finishedAt).isNull()
    }

    @Test
    fun `finishCall should copy Call and set state = FINISHED`() {

        val call = service.createCall(personId1, personId2, CallType.VIDEO)
        verify { notificationService.callChanged(eq(personId2), any()) }

        val result = service.finishCall(call)
        verify { notificationService.callChanged(eq(personId1), any()) }
        verify { notificationService.callChanged(eq(personId2), any()) }

        assertCallsEquals(result, call)
        assertThat(result.retrievedAt).isNotNull
        assertThat(result.callState).isEqualTo(CallState.FINISHED)
        assertThat(result.finishedAt).isNotNull
    }

    private fun assertCallsEquals(result: Call, call: Call) {
        assertThat(result.callType).isEqualTo(call.callType)
        assertThat(result.senderId).isEqualTo(call.senderId)
        assertThat(result.receiverId).isEqualTo(call.receiverId)
        assertThat(result.id).isEqualTo(call.id)
        assertThat(result.createdAt).isEqualTo(call.createdAt)
    }

    @Test
    fun `createCall should create a Call with CallType VIDEO`() {

        val result = service.createCall(personId1, personId2, CallType.VIDEO)
        assertThat(result.callType).isEqualTo(CallType.VIDEO)
    }

    @Test
    fun `createCall should create a Call with CallType AUDIO`() {

        val result = service.createCall(personId1, personId2, CallType.AUDIO)
        assertThat(result.callType).isEqualTo(CallType.AUDIO)
    }

    @Test
    fun `createCall should sent notification and update Message with sentAt `() {
        val result = service.createCall(personId1, personId2, CallType.VIDEO)
        assertThat(result).isNotNull
        assertThat(result.sentAt).isNotNull
        assertThat(result.retrievedAt).isNull()
        assertThat(result.callState).isEqualTo(CallState.CALLING)
    }

    @Test
    fun `createCall should sent notification and update Message without sentAt`() {
        every { notificationService.callChanged(eq(personId2), any()) } returns false

        val result = service.createCall(personId1, personId2, CallType.VIDEO)
        assertThat(result).isNotNull
        assertThat(result.sentAt).isNull()
        assertThat(result.retrievedAt).isNull()
        assertThat(result.callState).isEqualTo(CallState.CREATED)

    }

    @Test
    fun `createCall should throw when sender and receiver are the same`() {
        assertThrows<SecurityError.PersonsAreTheSame> {
            service.createCall(personId1, personId1, CallType.VIDEO)
        }
    }


    @Test
    fun `createCall should throw when send and receiver are not in same group`() {
        assertThrows<SecurityError.PersonsNotInSameGroup> {
            service.createCall(personId1, personId3, CallType.VIDEO)
        }
    }
}
