package org.ossiaustria.amigo.platform.domain.services.sendables

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import org.ossiaustria.amigo.platform.domain.repositories.MultimediaRepository
import org.ossiaustria.amigo.platform.domain.services.files.DiskFileStorage
import org.ossiaustria.amigo.platform.domain.services.files.FileInfo
import org.ossiaustria.amigo.platform.domain.services.files.FileStorageError
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile
import java.util.UUID.randomUUID

internal class MultimediaServiceTest : SendableServiceTest<Multimedia, MultimediaService>() {

    @SpykBean
    lateinit var fileStorage: DiskFileStorage

    @Autowired
    override lateinit var service: MultimediaService

    @Autowired
    private lateinit var repository: MultimediaRepository

    private val toTypedArray: ByteArray = "content ".repeat(1024).toByteArray()

    val multipartFile: MockMultipartFile = mockMultipartFile("image/png", content = toTypedArray)

    val bigFile: MockMultipartFile =
        MockMultipartFile(
            "name", "name", "image/png",
            "content ".repeat(1024 * 1024).toByteArray()
        )

    @BeforeEach
    fun beforeEach() {
        cleanTables()
        repository.deleteAll()

        mockPersons()

        repository.save(Multimedia(existingId, personId1, personId2, personId1, MultimediaType.IMAGE, "filename"))
        repository.save(
            Multimedia(
                randomUUID(),
                personId2,
                personId1,
                personId2,
                MultimediaType.IMAGE,
                "filename2"
            )
        )

        every {
            fileStorage.saveFile(any(), any(), any())
        } returns FileInfo(100, "absolutePath")


    }

    @Test
    fun `createMultimedia should save a Multimedia with text`() {
        val result = service.createMultimedia(personId1, personId2, null, "name", multipartFile)
        assertThat(result).isNotNull
    }

    @Test
    fun `createMultimedia should allow when sender and receiver are the same`() {
        val result = service.createMultimedia(personId1, personId1, null, "name", multipartFile)
        assertThat(result).isNotNull
    }

    @Test
    fun `createMultimedia should throw when MultipartFile is empty`() {
        assertThrows<MultimediaError.UnsupportedContent> {
            service.createMultimedia(
                personId1,
                personId2,
                null,
                "name",
                mockMultipartFile("image/png", "".toByteArray())
            )
        }
    }

    @Test
    fun `createMultimedia should throw when MultipartFile is suspiciously small`() {
        assertThrows<MultimediaError.UnsupportedContent> {
            service.createMultimedia(
                personId1,
                personId2,
                null,
                "name",
                mockMultipartFile("image/png", "asdfasdf".toByteArray())
            )
        }
    }

    @Test
    fun `createMultimedia should throw when MultipartFile is too big`() {
        assertThrows<MultimediaError.FileSizeExceeded> {
            service.createMultimedia(
                personId1,
                personId2,
                null,
                "name",
                bigFile
            )
        }
    }

    @Test
    fun `createMultimedia should support jpg for IMAGE`() {

        service.createMultimedia(personId1, personId2, null, "name", mockMultipartFile("image/jpg", toTypedArray)).let {
            assertThat(it).isNotNull
            assertThat(it.type).isEqualTo(MultimediaType.IMAGE)
        }

        service.createMultimedia(personId1, personId2, null, "name", mockMultipartFile("image/jpeg", toTypedArray))
            .let {
                assertThat(it).isNotNull
                assertThat(it.type).isEqualTo(MultimediaType.IMAGE)
            }
    }

    @Test
    fun `createMultimedia should support png for IMAGE`() {

        service.createMultimedia(personId1, personId2, null, "name", mockMultipartFile("image/png", toTypedArray)).let {
            assertThat(it).isNotNull
            assertThat(it.type).isEqualTo(MultimediaType.IMAGE)
        }

    }

    @Test
    fun `createMultimedia should not support PDF or ZIP`() {

        assertThrows<MultimediaError.UnsupportedContentType> {
            service.createMultimedia(
                personId1, personId2, null, "name", mockMultipartFile(
                    "application/pdf",
                    toTypedArray
                )
            )
        }
        assertThrows<MultimediaError.UnsupportedContentType> {
            service.createMultimedia(
                personId1, personId2, null, "name", mockMultipartFile(
                    "application/zip",
                    toTypedArray
                )
            )
        }
    }

    @Test
    fun `createMultimedia should throw when send and receiver are not in same group`() {
        assertThrows<SendableError.PersonsNotInSameGroup> {
            service.createMultimedia(personId1, personId3, null, "name", multipartFile)
        }
    }

    @Test
    fun `loadFile should throw error when file does not exist`() {
        val id = randomUUID()
        val multimedia = Multimedia(id, personId1, personId2, personId1, MultimediaType.IMAGE, "filename")
        assertThrows<FileStorageError.FileNotFound> {
            service.loadFile(multimedia)
        }
    }

    private fun mockMultipartFile(contentType: String, content: ByteArray = toTypedArray) =
        MockMultipartFile("name", "name", contentType, content)

}