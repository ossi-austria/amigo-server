package org.ossiaustria.amigo.platform.domain.services.sendables

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.ValidationException
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import org.ossiaustria.amigo.platform.domain.repositories.MultimediaRepository
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID.randomUUID

internal class MultimediaServiceTest : SendableServiceTest<Multimedia, MultimediaService>() {

    @Autowired
    override lateinit var service: MultimediaService

    @Autowired
    private lateinit var repository: MultimediaRepository

    @BeforeEach
    fun beforeEach() {
        cleanTables()
        repository.deleteAll()

        mockPersons()

        repository.save(Multimedia(existingId, personId1, personId2, personId1, "https://orf.at", MultimediaType.IMAGE))
        repository.save(
            Multimedia(
                randomUUID(),
                personId2,
                personId1,
                personId2,
                "https://orf.at",
                MultimediaType.IMAGE
            )
        )
    }

    @Test
    fun `createMessage should save a Message with text`() {
        val result = service.createMultimedia(personId1, personId2, "text")
        assertThat(result).isNotNull
    }

    @Test
    fun `createMessage should throw when sender and receiver are the same`() {
        assertThrows<SendableError.PersonsAreTheSame> {
            service.createMultimedia(personId1, personId1, "text")
        }
    }

    @Test
    fun `createMessage should throw when text is empty`() {
        assertThrows<ValidationException> {
            service.createMultimedia(personId1, personId2, "")
        }
    }

    @Test
    fun `createMessage should throw when send and receiver are not in same group`() {
        assertThrows<SendableError.PersonsNotInSameGroup> {
            service.createMultimedia(personId1, personId3, "text")
        }
    }
}