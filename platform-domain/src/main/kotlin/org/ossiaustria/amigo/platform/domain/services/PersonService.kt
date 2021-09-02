package org.ossiaustria.amigo.platform.domain.services

import org.ossiaustria.amigo.platform.domain.models.Person
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.files.AvatarFileStorage
import org.ossiaustria.amigo.platform.domain.services.multimedia.MultimediaError
import org.ossiaustria.amigo.platform.domain.services.multimedia.MultimediaServiceImpl
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.net.URI
import java.util.*
import javax.transaction.Transactional

@Service
class PersonService {

    @Autowired
    private lateinit var personRepository: PersonRepository

    @Autowired
    private lateinit var avatarFileStorage: AvatarFileStorage

    fun findById(id: UUID): Person? = personRepository.findByIdOrNull(id)

    fun count(): Long = personRepository.count()

    @Transactional
    fun changeNameAndAvatarUrl(person: Person, name: String?, avatarUrl: String?): Person {
        var updatedPerson = if (name != null) changeName(person, name) else person
        updatedPerson = if (avatarUrl != null) changeAvatarUrl(updatedPerson, avatarUrl) else person
        return personRepository.save(updatedPerson)
    }

    fun changeName(person: Person, name: String): Person {
        if (name.matches(INVALID_NAME_CHARS)) throw PersonError.InvalidName(name)
        Log.info("Update person name: $person -> $name")
        return personRepository.save(person.copy(name = name))
    }

    fun changeAvatarUrl(person: Person, avatarUrl: String): Person {
        try {
            val uri = URI.create(avatarUrl)
            Log.info("Update person avatarUrl: $person -> $uri")
            avatarFileStorage.deleteAvatar(person.id)
            if (uri.isAbsolute) return personRepository.save(person.copy(avatarUrl = uri.toString()))
            else throw PersonError.NotAnUrl(avatarUrl)
        } catch (e: IllegalArgumentException) {
            throw PersonError.NotAnUrl(avatarUrl)
        }
    }

    fun uploadAvatar(person: Person, multipartFile: MultipartFile): Person {
        checkContentType(multipartFile.contentType)
        checkContent(multipartFile)
        avatarFileStorage.saveAvatar(person.id, multipartFile, overwrite = true)

        val fileSuffix = multipartFile.contentType!!.substringAfter("/")
        val avatarUrl = "avatar.$fileSuffix"
        return personRepository.save(
            person.copy(avatarUrl = avatarUrl)
        ).also {
            Log.info("uploaded avatar: $avatarUrl")
        }
    }

    internal fun loadAvatar(person: Person) = avatarFileStorage.loadAvatar(person.id)

    private fun checkContent(multipartFile: MultipartFile) {

        if (multipartFile.isEmpty || multipartFile.size < MIN_SIZE) {
            throw MultimediaError.UnsupportedContent()
        }

        if (multipartFile.size > MultimediaServiceImpl.MAX_SIZE) {
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

    fun findByViewerAndId(person: Person, id: UUID): Person {
        return personRepository.findByGroupIdAndId(person.groupId, id)
            ?: throw NotFoundException(ErrorCode.NotFound, "Other Person not found in same Group")
    }

    fun loadAvatar(viewer: Person, otherPersonId: UUID): PersonAvatar {
        val other = findByViewerAndId(viewer, otherPersonId)
        val resource = try {
            avatarFileStorage.loadAvatar(other.id)
        } catch (e: Exception) {
            null
        }
        return PersonAvatar(other, resource)
    }

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
        val MAX_SIZE = 2 * 1024 * 1024L
        val MIN_SIZE = 1024L
        val INVALID_NAME_CHARS = Regex("[_!¡?÷¿/\\\\+=@#\$%ˆ&*(){}|~<>;:\\[\\]]*\$")
    }
}

sealed class PersonError(errorName: String, message: String, cause: Throwable? = null) :
    ServiceError(errorName, message, cause) {
    class InvalidName(name: String) :
        PersonError("UNSUPPORTED_PERSON_NAME", "Name is not supported: $name")

    class NotAnUrl(url: String) :
        PersonError("UNSUPPORTED_PERSON_AVATAR_URL", "URL is invalid: $url")
}

data class PersonAvatar(
    val person: Person,
    val resource: Resource?,
) {
    val isUseless = person.avatarUrl.isNullOrBlank() && resource == null

    val filename = """${person.id}-${person.avatarUrl}"""

    val contentType = ("image/" + person.avatarUrl?.substringAfter("."))
}