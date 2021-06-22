package org.ossiaustria.amigo.platform.domain.services.sendables

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.models.ValidationException
import org.ossiaustria.amigo.platform.domain.repositories.MessageRepository
import org.ossiaustria.amigo.platform.domain.services.messaging.NotificationService
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID.randomUUID

internal class MessageServiceTest : SendableServiceTest<Message, MessageService>() {

    @Autowired
    override lateinit var service: MessageService

    @Autowired
    private lateinit var messageRepository: MessageRepository

    @MockkBean
    private lateinit var notificationService: NotificationService

    @BeforeEach
    fun beforeEach() {
        cleanTables()
        messageRepository.deleteAll()

        mockPersons()

        messageRepository.save(Message(existingId, personId1, personId2, "text"))
        messageRepository.save(Message(randomUUID(), personId2, personId1, "text"))
    }

    @Test
    fun `createMessage should save a Message with text`() {

        every { notificationService.messageSent(eq(personId2), any()) } returns true

        val result = service.createMessage(personId1, personId2, "text")
        assertThat(result).isNotNull
        assertThat(result.text).isEqualTo("text")
        assertThat(result.senderId).isEqualTo(personId1)
        assertThat(result.receiverId).isEqualTo(personId2)
        assertThat(result.createdAt).isNotNull
        assertThat(result.retrievedAt).isNull()
    }

    @Test
    fun `createMessage should sent notification and update Message with sentAt `() {
        every { notificationService.messageSent(eq(personId2), any()) } returns true

        val result = service.createMessage(personId1, personId2, "text")
        assertThat(result).isNotNull
        assertThat(result.sentAt).isNotNull
    }

    @Test
    fun `createMessage should sent notification and update Message without sentAt`() {
        every { notificationService.messageSent(eq(personId2), any()) } returns false

        val result = service.createMessage(personId1, personId2, "text")
        assertThat(result).isNotNull
        assertThat(result.sentAt).isNull()
    }

    @Test
    fun `createMessage should throw when sender and receiver are the same`() {
        assertThrows<SendableError.PersonsAreTheSame> {
            service.createMessage(personId1, personId1, "text")
        }
    }

    @Test
    fun `createMessage should throw when text is empty`() {
        assertThrows<ValidationException> {
            service.createMessage(personId1, personId2, "")
        }
    }

    @Test
    fun `createMessage should throw when send and receiver are not in same group`() {
        assertThrows<SendableError.PersonsNotInSameGroup> {
            service.createMessage(personId1, personId3, "text")
        }
    }
}