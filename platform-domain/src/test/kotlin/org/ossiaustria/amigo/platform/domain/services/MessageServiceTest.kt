package org.ossiaustria.amigo.platform.domain.services

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.models.ValidationException
import org.ossiaustria.amigo.platform.domain.repositories.MessageRepository
import org.ossiaustria.amigo.platform.domain.testcommons.Models
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import java.util.UUID.randomUUID

internal class MessageServiceTest : AbstractServiceTest() {

    @Autowired
    private lateinit var service: MessageService

    @Autowired
    private lateinit var messageRepository: MessageRepository


    val personId1: UUID = randomUUID()
    val personId2: UUID = randomUUID()
    val personId3: UUID = randomUUID()
    val groupId1: UUID = randomUUID()
    val groupId2: UUID = randomUUID()

    @BeforeEach
    fun beforeEach() {
        cleanTables()
        messageRepository.deleteAll()
        groups.save(Models.group(groupId1))
        groups.save(Models.group(groupId2))
        accounts.save(Models.account()).also {
            persons.save(Models.persons(personId1, it.id, groupId1))
        }
        accounts.save(Models.account()).also {
            persons.save(Models.persons(personId2, it.id, groupId1))
        }
        accounts.save(Models.account()).also {
            persons.save(Models.persons(personId3, it.id, groupId2))
        }

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

    @Test
    fun `getOne should throw when id does not exist`() {
        assertThrows<NotFoundException> {
            service.getOne(randomUUID())
        }
    }

    @Test
    fun `getOne should succeed for existing id`() {
        val id = randomUUID()
        messageRepository.save(Message(id, personId1, personId2, "text"))
        val result = service.getOne(id)
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(id)
    }

    @Test
    fun `getAll should succeed generally`() {
        val id = randomUUID()
        messageRepository.save(Message(id, personId1, personId2, "text"))
        val result = service.getAll()
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)
        assertThat(result.first().id).isEqualTo(id)

    }

    @Test
    fun `findWithPersons should succeed for persons in same group`() {
        messageRepository.save(Message(randomUUID(), personId1, personId2, "text"))
        messageRepository.save(Message(randomUUID(), personId2, personId1, "text"))

        val result = service.findWithPersons(personId1, personId2)
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)

    }

    @Test
    fun `findWithPersons should throw when personsId are both null`() {
        assertThrows<SendableError.PersonsNotProvided> {
            service.findWithPersons(null, null)
        }
    }

    @Test
    fun `findWithPersons should succeed when senderId is set`() {
        messageRepository.save(Message(randomUUID(), personId1, personId2, "text"))
        messageRepository.save(Message(randomUUID(), personId2, personId1, "text"))

        val result = service.findWithPersons(personId1, null)
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)
    }

    @Test
    fun `findWithPersons should succeed when receiverId is set`() {
        messageRepository.save(Message(randomUUID(), personId1, personId2, "text"))
        messageRepository.save(Message(randomUUID(), personId2, personId1, "text"))

        val result = service.findWithPersons(null, personId1)
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)
    }

    @Test
    fun `findWithSender should succeed generally`() {
        messageRepository.save(Message(randomUUID(), personId1, personId2, "text"))
        messageRepository.save(Message(randomUUID(), personId2, personId1, "text"))

        val result = service.findWithSender(personId1)
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)
    }

    @Test
    fun `findWithReceiver should succeed generally`() {
        messageRepository.save(Message(randomUUID(), personId1, personId2, "text"))
        messageRepository.save(Message(randomUUID(), personId2, personId1, "text"))

        val result = service.findWithReceiver(personId1)
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)
    }

    @Test
    fun `markAsSent should throw when id does not exist`() {
        assertThrows<NotFoundException> {
            service.markAsSent(randomUUID())
        }
    }

    @Test
    fun `markAsSent should succeed for existing id`() {
        val id = randomUUID()
        messageRepository.save(Message(id, personId1, personId2, "text"))
        val result = service.markAsSent(id)
        assertThat(result).isNotNull
        assertThat(result.sendAt).isNotNull
    }

    @Test
    fun `markAsRetrieved should throw when id does not exist`() {
        assertThrows<NotFoundException> {
            service.markAsRetrieved(randomUUID())
        }
    }

    @Test
    fun `markAsRetrieved should succeed for existing id`() {
        val id = randomUUID()
        messageRepository.save(Message(id, personId1, personId2, "text"))
        val result = service.markAsRetrieved(id)
        assertThat(result).isNotNull
        assertThat(result.retrievedAt).isNotNull
    }

}