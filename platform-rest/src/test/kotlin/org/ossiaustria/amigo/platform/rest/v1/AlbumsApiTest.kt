package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Album
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.enums.MultimediaType
import org.ossiaustria.amigo.platform.domain.services.multimedia.AlbumService
import org.ossiaustria.amigo.platform.rest.v1.multimedias.AlbumDto
import org.ossiaustria.amigo.platform.rest.v1.multimedias.AlbumsApi
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType.ARRAY
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import java.util.UUID
import java.util.UUID.randomUUID

internal class AlbumsApiTest : AbstractRestApiTest() {

    val baseUrl = "/v1/albums"

    @SpykBean
    lateinit var albumService: AlbumService

    @BeforeEach
    fun before() {
        every { albumService.findWithOwner(eq(person1Id)) } returns listOf(
            mockAlbum(ownerId = person1Id),
            mockAlbum(ownerId = person1Id),
        )
        every { albumService.findWithAccess(eq(person1Id)) } returns listOf(
            mockAlbum(ownerId = person2Id),
            mockAlbum(ownerId = person2Id),
        )
        mockUserAuthentication()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `createAlbum should create new Album`() {

        val ownerId = person1Id
        val name = "newname"

        // Cannot mock “RequestPart" name and file
        every { albumService.createAlbum(eq(ownerId), eq(name)) } returns
            mockAlbum(ownerId = ownerId, name = name)

        val result = this.performPost(
            baseUrl, accessToken.token, personId = person1Id,
            body = AlbumsApi.CreateAlbumRequest(
                name = name
            ),
        )
            .expectOk()
            .document(
                "albums-create",
                requestFields(
                    field("name", STRING, "Name of album").optional(),
                ),
                responseFields(albumsResponseFields())
            )
            .returns(AlbumDto::class.java)

        assertThat(result).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `createAlbum needs authentication`() {
        val url = "$baseUrl?receiverId=${randomUUID()}&ownerId=${randomUUID()}&callType=VIDEO"
        this.performPost(url).expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `own should return albums of current user`() {

        val result = this.performGet("$baseUrl/own", accessToken.token, person1Id)
            .expectOk()
            .document(
                "albums-own",
                responseFields(albumsResponseFields("[]."))
            )
            .returnsList(AlbumDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.ownerId).isEqualTo(account.primaryPerson().id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `shared should return accessible albums by current user`() {

        val result = this.performGet("$baseUrl/shared", accessToken.token, person1Id)
            .expectOk()
            .document(
                "albums-shared",
                responseFields(albumsResponseFields("[]."))
            )
            .returnsList(AlbumDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.ownerId).isNotEqualTo(account.primaryPerson().id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `own needs authentication`() {
        this.performGet("$baseUrl/own").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne should return Album visible by current user`() {
        val msgId = randomUUID()

        every { albumService.getOne(person1Id, msgId) } returns mockAlbum(
            id = msgId, ownerId = person1Id,
        )

        val result: AlbumDto = this.performGet("$baseUrl/$msgId", accessToken.token, person1Id)
            .expectOk()
            .document(
                "albums-one",
                responseFields(albumsResponseFields())
            )
            .returns(AlbumDto::class.java)

        assertThat(result).isNotNull
    }


    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne needs authentication`() {
        this.performGet("$baseUrl/${randomUUID()}").expectUnauthorized()
    }

    private fun mockAlbum(
        id: UUID = randomUUID(),
        ownerId: UUID,
        name: String = "filename",
    ): Album {
        return Album(
            id, ownerId = ownerId, name = name, items = listOf(
                Multimedia(id = randomUUID(), ownerId = ownerId, type = MultimediaType.IMAGE, filename = "filename"),
                Multimedia(id = randomUUID(), ownerId = ownerId, type = MultimediaType.IMAGE, filename = "filename"),
            )
        )
    }

    private fun albumsResponseFields(prefix: String = ""): List<FieldDescriptor> {
        return arrayListOf(
            field(prefix + "id", STRING, "UUID"),
            field(prefix + "ownerId", STRING, "UUID of Owner"),
            field(prefix + "items", ARRAY, "List of contained Multimedias"),
            field(prefix + "name", STRING, "File to name that file locally"),
            field(prefix + "createdAt", STRING, "LocalDateTime of Album creation"),
            field(prefix + "updatedAt", STRING, "LocalDateTime of Album update").optional()
        ).apply {
            addAll(multimediasResponseFields(prefix + "items[]."))
        }
    }

}
