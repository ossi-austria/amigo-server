package org.ossiaustria.amigo.platform.domain.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.models.ValidationException
import org.ossiaustria.amigo.platform.domain.repositories.MessageRepository
import org.ossiaustria.amigo.platform.domain.services.sendables.SendableServiceTest
import org.springframework.beans.factory.annotation.Autowired
import java.util.*

internal class MessageServiceTest : SendableServiceTest<Message, MessageService>() {

    @Autowired
    override lateinit var service: MessageService

    @Autowired
    private lateinit var messageRepository: MessageRepository


    @BeforeEach
    fun beforeEach() {
        cleanTables()
        messageRepository.deleteAll()

        mockPersons()

        messageRepository.save(Message(existingId, personId1, personId2, "text"))
        messageRepository.save(Message(UUID.randomUUID(), personId2, personId1, "text"))

    }

    @Test
    fun `createMessage should save a Message with text`() {
        val result = service.createMessage(personId1, personId2, "text")
        assertThat(result).isNotNull
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