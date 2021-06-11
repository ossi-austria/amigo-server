package org.ossiaustria.amigo.platform.domain.services.sendables

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.StringValidator
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import org.ossiaustria.amigo.platform.domain.repositories.MultimediaRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*
import java.util.UUID.randomUUID

interface MultimediaService : SendableService<Multimedia> {
    fun createMultimedia(senderId: UUID, receiverId: UUID, text: String): Multimedia
}

@Service
class MultimediaServiceImpl : MultimediaService {

    @Autowired
    private lateinit var repository: MultimediaRepository

    @Autowired
    private lateinit var personRepository: PersonRepository

    private val wrapper: SendableServiceMixin<Multimedia> by lazy { SendableServiceMixin(repository, personRepository) }

    override fun createMultimedia(senderId: UUID, receiverId: UUID, text: String): Multimedia {
        StringValidator.validateNotBlank(text)
        wrapper.validateSenderReceiver(senderId, receiverId)
        val multimedia = Multimedia(
            id = randomUUID(),
            senderId = senderId,
            receiverId = receiverId,
            createdAt = ZonedDateTime.now(),
            retrievedAt = null,
            sentAt = null,
            album = null,
            localUrl = "https://orf.at",
            ownerId = senderId,
            remoteUrl = "https://orf.at",
            type = MultimediaType.IMAGE
        )
        return repository.save(multimedia).also {
            Log.info("createMessage: senderId=$senderId receiverId=$receiverId -> $text")
        }
    }

    override fun getOne(id: UUID): Multimedia = wrapper.getOne(id)

    override fun getAll(): List<Multimedia> = wrapper.getAll()

    override fun findWithPersons(senderId: UUID?, receiverId: UUID?) =
        wrapper.findWithPersons(senderId, receiverId)

    override fun findWithSender(senderId: UUID) = wrapper.findWithSender(senderId)

    override fun findWithReceiver(receiverId: UUID) = wrapper.findWithReceiver(receiverId)

    override fun markAsSent(id: UUID, time: ZonedDateTime) = wrapper.markAsSent(id, time)

    override fun markAsRetrieved(id: UUID, time: ZonedDateTime) = wrapper.markAsRetrieved(id, time)

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }
}