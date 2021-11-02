package org.ossiaustria.amigo.platform.domain.services

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.repositories.PersonRepository
import org.ossiaustria.amigo.platform.domain.services.files.DiskAvatarFileStorage
import org.ossiaustria.amigo.platform.domain.services.files.FileInfo
import org.ossiaustria.amigo.platform.domain.services.files.FileStorageError
import org.ossiaustria.amigo.platform.domain.services.multimedia.MultimediaError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile

internal class PersonServiceTest : AbstractServiceTest() {

    @SpykBean
    lateinit var fileStorage: DiskAvatarFileStorage

    @Autowired
    lateinit var service: PersonProfileService

    @Autowired
    private lateinit var repository: PersonRepository

    private val toTypedArray: ByteArray = "content ".repeat(1024).toByteArray()

    val bigFile: MockMultipartFile = MockMultipartFile(
        "name", "name", "image/png",
        "content ".repeat(1024 * 1024).toByteArray()
    )

    @BeforeEach
    fun beforeEach() {
        cleanTables()
        repository.deleteAll()

        mockPersons()

        every { fileStorage.saveAvatar(any(), any(), any()) } returns FileInfo(100, "absolutePath")
    }

    @Test
    fun `changeName should save a valid new name`() {
        val result = service.changeName(person1, "new-name")
        assertThat(result).isNotNull
        assertThat(result.name).isEqualTo("new-name")
    }

    @Test
    fun `changeName must not save a invalid new name`() {
        assertThrows<PersonError.InvalidName> {
            service.changeName(person1, "#")
        }
    }

    @Test
    fun `changeAvatarUrl should save a valid image url`() {
        val result = service.changeAvatarUrl(person1, "http://orf.at/image.png")
        assertThat(result).isNotNull
        assertThat(result.avatarUrl).isEqualTo("http://orf.at/image.png")
    }

    @Test
    fun `changeAvatarUrl must not save a invalid url`() {
        assertThrows<PersonError.NotAnUrl> {
            service.changeAvatarUrl(person1, "localhost/image.png")
        }
    }

    @Test
    fun `uploadAvatar should save an JPG and store avatarUrl`() {
        val result = service.uploadAvatar(person1, mockMultipartFile("image/jpg", toTypedArray))
        assertThat(result).isNotNull
        assertThat(result.avatarUrl).contains((System.currentTimeMillis() / 10000).toString())
        assertThat(result.avatarUrl).endsWith(".jpg")
    }

    @Test
    fun `uploadAvatar should save an PNG and store avatarUrl`() {
        val result = service.uploadAvatar(person1, mockMultipartFile("image/png", toTypedArray))
        assertThat(result).isNotNull
        assertThat(result.avatarUrl).contains((System.currentTimeMillis() / 10000).toString())
        assertThat(result.avatarUrl).endsWith(".png")
    }

    @Test
    fun `uploadAvatar should throw when MultipartFile is empty`() {
        assertThrows<MultimediaError.UnsupportedContent> {
            service.uploadAvatar(person1, mockMultipartFile("image/png", "".toByteArray()))
        }
    }

    @Test
    fun `uploadAvatar should throw when MultipartFile is suspiciously small`() {
        assertThrows<MultimediaError.UnsupportedContent> {
            service.uploadAvatar(person1, mockMultipartFile("image/png", "asdfasdf".toByteArray()))
        }
    }

    @Test
    fun `uploadAvatar should throw when MultipartFile is too big`() {
        assertThrows<MultimediaError.FileSizeExceeded> {
            service.uploadAvatar(person1, bigFile)
        }
    }

    @Test
    fun `uploadAvatar should support jpg for IMAGE`() {
        service.uploadAvatar(person1, mockMultipartFile("image/jpg", toTypedArray)).let {
            assertThat(it).isNotNull
        }

        service.uploadAvatar(person1, mockMultipartFile("image/jpeg", toTypedArray))
            .let {
                assertThat(it).isNotNull
            }
    }

    @Test
    fun `uploadAvatar should support png for IMAGE`() {
        service.uploadAvatar(person1, mockMultipartFile("image/png", toTypedArray)).let {
            assertThat(it).isNotNull
        }
    }

    @Test
    fun `uploadAvatar should not support PDF or ZIP`() {
        assertThrows<MultimediaError.UnsupportedContentType> {
            service.uploadAvatar(person1, mockMultipartFile("application/pdf", toTypedArray))
        }
        assertThrows<MultimediaError.UnsupportedContentType> {
            service.uploadAvatar(person1, mockMultipartFile("application/zip", toTypedArray))
        }
    }

    @Test
    fun `loadFile should throw error when file does not exist`() {
        assertThrows<FileStorageError.FileNotFound> {
            service.loadAvatar(person1)
        }
    }

    private fun mockMultipartFile(contentType: String, content: ByteArray = toTypedArray) =
        MockMultipartFile("name", "name", contentType, content)

}
