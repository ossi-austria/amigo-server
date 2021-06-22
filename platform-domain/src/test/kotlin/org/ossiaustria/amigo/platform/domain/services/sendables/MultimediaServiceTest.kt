package org.ossiaustria.amigo.platform.domain.services.sendables

import com.ninjasquad.springmockk.MockkBean
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
import org.ossiaustria.amigo.platform.domain.services.messaging.NotificationService
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

    @MockkBean
    private lateinit var notificationService: NotificationService

    private val toTypedArray: ByteArray = "content ".repeat(1024).toByteArray()

    val multipartFile: MockMultipartFile = mockMultipartFile("image/png", content = toTypedArray)

    val bigFile: MockMultipartFile = MockMultipartFile(
        "name", "name", "image/png",
        "content ".repeat(1024 * 1024).toByteArray()
    )

    @BeforeEach
    fun beforeEach() {
        cleanTables()
        repository.deleteAll()

        mockPersons()
        every { notificationService.multimediaSent(eq(personId2), any()) } returns true

        repository.save(Multimedia(existingId, personId1, personId2, personId1, MultimediaType.IMAGE, "filename"))
        val multimedia = Multimedia(randomUUID(), personId2, personId1, personId2, MultimediaType.IMAGE, "filename2")
        repository.save(multimedia)

        every {
            fileStorage.saveFile(any(), any(), any())
        } returns FileInfo(100, "absolutePath")


    }

    @Test
    fun `createMultimedia should save a Multimedia with a file`() {
        val result = service.createMultimedia(personId1, personId2, null, "name", multipartFile)
        assertThat(result).isNotNull
        assertThat(result.senderId).isEqualTo(personId1)
        assertThat(result.receiverId).isEqualTo(personId2)
        assertThat(result.createdAt).isNotNull
        assertThat(result.retrievedAt).isNull()
    }

    @Test
    fun `createMultimedia should allow when sender and receiver are the same`() {
        every { notificationService.multimediaSent(eq(personId1), any()) } returns true
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
    fun `createMultimedia should sent notification and update Multimedia with sentAt `() {
        every { notificationService.multimediaSent(eq(personId2), any()) } returns true

        val result = service.createMultimedia(personId1, personId2, null, null, multipartFile)
        assertThat(result).isNotNull
        assertThat(result.sentAt).isNotNull
    }

    @Test
    fun `createMultimedia should sent notification and update Multimedia without sentAt`() {
        every { notificationService.multimediaSent(eq(personId2), any()) } returns false

        val result = service.createMultimedia(personId1, personId2, null, null, multipartFile)
        assertThat(result).isNotNull
        assertThat(result.sentAt).isNull()
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