package org.ossiaustria.amigo.platform.domain.services.sendables

import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.StringValidator
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import org.ossiaustria.amigo.platform.domain.repositories.MultimediaRepository
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.ServiceError
import org.ossiaustria.amigo.platform.domain.services.files.FileStorage
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.time.ZonedDateTime
import java.util.*
import java.util.UUID.randomUUID

interface MultimediaService {

    fun getOne(id: UUID): Multimedia
    fun getAll(): List<Multimedia>
    fun findWithOwner(ownerId: UUID): List<Multimedia>

    /**
     * Creates a new Multimedia with an optional uploaded MultipartFile.
     * SenderId and ReceiverId might be the same, to indicate an non-shared creation in an album.
     */
    fun createMultimedia(
        ownerId: UUID,
        albumId: UUID?,
        name: String? = null,
        file: MultipartFile
    ): Multimedia

    fun uploadFile(multimedia: Multimedia, multipartFile: MultipartFile): Multimedia
    fun loadFile(multimedia: Multimedia): Resource?
    fun count(): Long
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

    override fun createMultimedia(
        ownerId: UUID,
        albumId: UUID?,
        name: String?,
        file: MultipartFile
    ): Multimedia {
        val filename = name ?: file.originalFilename
        StringValidator.validateNotBlank(filename)

        val newMultimedia = createNew(ownerId, filename, albumId)
        return uploadFile(newMultimedia, file)
    }

    private fun createNew(
        ownerId: UUID,
        filename: String,
        albumId: UUID?
    ) = Multimedia(
        id = randomUUID(),
        ownerId = ownerId,
        createdAt = ZonedDateTime.now(),
        filename = filename,
        albumId = albumId,
        type = MultimediaType.IMAGE
    )


    override fun loadFile(multimedia: Multimedia): Resource? {
        return fileStorage.loadFile(multimedia)
    }

    override fun count(): Long = repository.count()

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

    override fun getOne(id: UUID): Multimedia = repository.findByIdOrNull(id)
        ?: throw NotFoundException(ErrorCode.NotFound, "Multimedia $id not found!")

    override fun getAll(): List<Multimedia> = repository.findAll().toList().also {
        Log.info("getAll: -> ${it.size} results")
    }

    override fun findWithOwner(ownerId: UUID) = repository.findAllByOwnerIdOrderByCreatedAt(ownerId).also {
        Log.info("findWithSender: ownerId=$ownerId -> ${it.size} results")
    }

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
        val MAX_SIZE = 2 * 1024 * 1024L
        val MIN_SIZE = 1024L
    }
}