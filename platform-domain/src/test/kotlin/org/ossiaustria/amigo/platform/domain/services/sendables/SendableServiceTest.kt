package org.ossiaustria.amigo.platform.domain.services.sendables

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.models.Sendable
import org.ossiaustria.amigo.platform.domain.services.AbstractServiceTest
import org.ossiaustria.amigo.platform.domain.testcommons.Models
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import java.time.ZonedDateTime
import java.util.*
import java.util.UUID.randomUUID

internal abstract class SendableServiceTest<S : Sendable<S>, T : SendableService<S>> : AbstractServiceTest() {

    protected abstract var service: T

    val existingId: UUID = randomUUID()

    val personId1: UUID = randomUUID()
    val personId2: UUID = randomUUID()
    val personId3: UUID = randomUUID()

    val groupId1: UUID = randomUUID()
    val groupId2: UUID = randomUUID()

    protected fun mockPersons() {
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
    fun `getOne should throw when id does not exist`() {
        assertThrows<NotFoundException> {
            service.getOne(randomUUID())
        }
    }

    @Test
    fun `getOne should succeed for existing id`() {
        val result = service.getOne(existingId)
        assertThat(result).isNotNull
        assertThat(result.id).isEqualTo(existingId)
    }

    @Test
    fun `getAll should succeed generally`() {
        val result = service.getAll()
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(2)
    }

    @Test
    fun `findWithPersons should succeed for persons in same group`() {
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
        val result = service.findWithPersons(personId1, null)
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)
    }

    @Test
    fun `findWithPersons should succeed when receiverId is set`() {
        val result = service.findWithPersons(null, personId1)
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)
    }

    @Test
    fun `findWithSender should succeed generally`() {
        val result = service.findWithSender(personId1)
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)
    }

    @Test
    fun `findWithReceiver should succeed generally`() {
        val result = service.findWithReceiver(personId1)
        assertThat(result).isNotNull
        assertThat(result.size).isEqualTo(1)
    }

    @Test
    fun `markAsSent should throw when id does not exist`() {
        assertThrows<NotFoundException> {
            service.markAsSent(randomUUID(), ZonedDateTime.now())
        }
    }

    @Test
    fun `markAsSent should succeed for existing id`() {
        val result = service.markAsSent(existingId, ZonedDateTime.now())
        assertThat(result).isNotNull
        assertThat(result.sentAt).isNotNull
    }

    @Test
    fun `markAsRetrieved should throw when id does not exist`() {
        assertThrows<NotFoundException> {
            service.markAsRetrieved(randomUUID(), ZonedDateTime.now())
        }
    }

    @Test
    fun `markAsRetrieved should succeed for existing id`() {
        val result = service.markAsRetrieved(existingId, ZonedDateTime.now())
        assertThat(result).isNotNull
        assertThat(result.retrievedAt).isNotNull
    }

}