package org.ossiaustria.amigo.platform.domain.services.sendables

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.StringValidator
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import org.ossiaustria.amigo.platform.domain.repositories.MultimediaRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.ServiceError
import org.ossiaustria.amigo.platform.domain.services.files.FileStorage
import org.ossiaustria.amigo.platform.domain.services.messaging.NotificationService
import org.ossiaustria.amigo.platform.exceptions.DefaultNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.ZonedDateTime
import java.util.*
import java.util.UUID.randomUUID

interface MultimediaService : SendableService<Multimedia> {
    /**
     * Creates a new Multimedia with an optional uploaded MultipartFile.
     * SenderId and ReceiverId might be the same, to indicate an non-shared creation in an album.
     */
    fun createMultimedia(
        senderId: UUID,
        receiverId: UUID,
        albumId: UUID?,
        name: String? = null,
        file: MultipartFile
    ): Multimedia

    fun uploadFile(multimedia: Multimedia, multipartFile: MultipartFile): Multimedia
    fun loadFile(multimedia: Multimedia): Resource?
}

sealed class MultimediaError(errorName: String, message: String, cause: Throwable? = null) :
    ServiceError(errorName, message, cause) {
    class UnsupportedContentType(contentType: String) :
        MultimediaError("UNSUPPORTED_CONTENT_TYPE", "ContentType is not supported: $contentType! use PNG or JPG!")

    class UnsupportedContent : MultimediaError("UNSUPPORTED_CONTENT", "Content is empty or possbible malicous!")
    class IOError(e: Throwable?) : MultimediaError("IO_ERROR", "Underlying IO error", e)
    class FileSizeExceeded(size: Long, maxSize: Long) :
        MultimediaError("FILE_SIZE_EXCEEDED", "File Size is too big: $size (max. $maxSize)")
}

@Service
class MultimediaServiceImpl : MultimediaService {

    @Autowired
    private lateinit var repository: MultimediaRepository

    @Autowired
    private lateinit var fileStorage: FileStorage

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var notificationService: NotificationService

    private val wrapper: SendableServiceMixin<Multimedia> by lazy { SendableServiceMixin(repository, personRepository) }

    override fun createMultimedia(
        senderId: UUID,
        receiverId: UUID,
        albumId: UUID?,
        name: String?,
        file: MultipartFile
    ): Multimedia {
        validateSenderReceiver(senderId, receiverId)

        val filename = name ?: file.originalFilename
        StringValidator.validateNotBlank(filename)

        val newMultimedia = createNew(senderId, receiverId, filename, albumId)
        val multimedia = this.uploadFile(newMultimedia, file)

        Log.info("createMultimedia: senderId=$senderId receiverId=$receiverId -> $filename")

        val success = notificationService.multimediaSent(receiverId, multimedia)
        return if (success) {
            repository.save(multimedia.copy(sentAt = ZonedDateTime.now())).also {
                Log.info("sent Multimedia: senderId=$senderId receiverId=$receiverId -> $filename")
            }
        } else {
            repository.save(multimedia).also {
                Log.warn("could not send Multimedia: senderId=$senderId receiverId=$receiverId -> $filename")
            }
        }
    }

    private fun createNew(
        senderId: UUID,
        receiverId: UUID,
        filename: String,
        albumId: UUID?
    ) = Multimedia(
        id = randomUUID(),
        senderId = senderId,
        receiverId = receiverId,
        createdAt = ZonedDateTime.now(),
        retrievedAt = null,
        filename = filename,
        sentAt = null,
        albumId = albumId,
        ownerId = senderId,
        type = MultimediaType.IMAGE
    )

    fun validateSenderReceiver(senderId: UUID, receiverId: UUID) {
        val sender = personRepository.findByIdOrNull(senderId) ?: throw DefaultNotFoundException()
        val receiver = personRepository.findByIdOrNull(receiverId) ?: throw DefaultNotFoundException()
        if (sender.groupId != receiver.groupId) throw SendableError.PersonsNotInSameGroup()
    }

    override fun loadFile(multimedia: Multimedia): Resource? {
        return fileStorage.loadFile(multimedia)
    }

    override fun uploadFile(multimedia: Multimedia, multipartFile: MultipartFile): Multimedia {

        val type = checkContentType(multipartFile.contentType)
        checkContent(multipartFile)
        val savedFile = fileStorage.saveFile(multimedia, multipartFile, overwrite = true)

        val size = savedFile.size
        val localUrl = savedFile.absolutePath
        return repository.save(
            multimedia.copy(
                size = size,
                type = type,
                contentType = multipartFile.contentType!!
            )
        ).also {
            Log.info("uploaded file and update multimedia: ${multimedia.id} size=$size localUrl=$localUrl")
        }
    }

    private fun checkContent(multipartFile: MultipartFile) {

        if (multipartFile.isEmpty || multipartFile.size < MIN_SIZE) {
            throw MultimediaError.UnsupportedContent()
        }

        if (multipartFile.size > MAX_SIZE) {
            throw MultimediaError.FileSizeExceeded(multipartFile.size, MAX_SIZE)
        }
    }

    private fun checkContentType(contentType: String?): MultimediaType {
        if (contentType == null) throw MultimediaError.UnsupportedContentType("empty")

        return when (contentType) {
            "image/png", "image/jpeg", "image/jpg" -> MultimediaType.IMAGE
            else -> throw MultimediaError.UnsupportedContentType(contentType)
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
        val MAX_SIZE = 2 * 1024 * 1024L
        val MIN_SIZE = 1024L
    }
}