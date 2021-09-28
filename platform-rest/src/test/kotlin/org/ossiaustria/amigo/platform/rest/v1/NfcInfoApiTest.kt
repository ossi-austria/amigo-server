package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.NfcInfo
import org.ossiaustria.amigo.platform.domain.models.enums.NfcInfoType
import org.ossiaustria.amigo.platform.domain.services.sendables.NfcInfoService
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType.STRING
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import java.util.UUID
import java.util.UUID.randomUUID

internal class NfcInfoApiTest : AbstractRestApiTest() {

    val baseUrl = "/v1/nfcs"

    @SpykBean
    lateinit var nfcInfoService: NfcInfoService

    @BeforeEach
    fun before() {
        every { nfcInfoService.findAllByOwner(eq(person1Id)) } returns listOf(
            mockNfcInfo(ownerId = person1Id, creatorId = person2Id),
            mockNfcInfo(ownerId = person1Id, creatorId = person2Id),
        )
        every { nfcInfoService.findAllByCreator(eq(person1Id)) } returns listOf(
            mockNfcInfo(ownerId = person2Id, creatorId = person1Id),
            mockNfcInfo(ownerId = person2Id, creatorId = person1Id),
        )
        mockUserAuthentication()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `createNfcInfo should create new NfcInfo`() {

        val ownerId = person1Id
        val creatorId = person1Id
        val name = "newname"

        // Cannot mock “RequestPart" name and file
        every { nfcInfoService.createNfc(eq(name), eq(ownerId), eq(creatorId)) } returns
            mockNfcInfo(name = name, ownerId = ownerId, creatorId = creatorId)

        val result = this.performPost(
            baseUrl, accessToken.token, personId = person1Id,
            body = NfcInfoApi.CreateNfcInfoRequest(
                name = name,
                ownerId = ownerId,
                creatorId = creatorId,
            ),
        )
            .expectOk()
            .document(
                "nfcs-create",
                requestFields(
                    field("creatorId", STRING, "UUID of creator - must be your person's id"),
                    field("ownerId", STRING, "UUID of owner - must be the Analogue's id"),
                    field("name", STRING, "Name of NFC Tag to be shown in UI").optional(),
                ),
                responseFields(nfcInfosResponseFields())
            )
            .returns(NfcInfoDto::class.java)

        assertThat(result).isNotNull
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `createNfcInfo needs authentication`() {
        val url = "$baseUrl?receiverId=${randomUUID()}&ownerId=${randomUUID()}&callType=VIDEO"
        this.performPost(url).expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `own should return nfcInfos of current user`() {

        val result = this.performGet("$baseUrl/own", accessToken.token, person1Id)
            .expectOk()
            .document(
                "nfcs-own",
                responseFields(nfcInfosResponseFields("[].")))
            .returnsList(NfcInfoDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.ownerId).isEqualTo(account.primaryPerson().id) }
        result.forEach { assertThat(it.creatorId).isEqualTo(person2Id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `created should return nfcInfos of current user`() {

        val result = this.performGet("$baseUrl/created", accessToken.token, person1Id)
            .expectOk()
            .document(
                "nfcs-created",
                responseFields(nfcInfosResponseFields("[].")),

                )
            .returnsList(NfcInfoDto::class.java)

        assertThat(result).isNotNull
        assertThat(result).isNotEmpty
        result.forEach { assertThat(it.ownerId).isEqualTo(person2Id) }
        result.forEach { assertThat(it.creatorId).isEqualTo(person1Id) }
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `own needs authentication`() {
        this.performGet("$baseUrl/own").expectUnauthorized()
    }

    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne should return NfcInfo visible by current user`() {
        val itemId = randomUUID()

        every { nfcInfoService.getOne(itemId) } returns mockNfcInfo(
            id = itemId, ownerId = person1Id, creatorId = person2Id
        )

        val result: NfcInfoDto = this.performGet("$baseUrl/$itemId", accessToken.token, person1Id)
            .expectOk()
            .document(
                "nfcs-one",
                responseFields(nfcInfosResponseFields())
            )
            .returns(NfcInfoDto::class.java)

        assertThat(result).isNotNull
    }


    @Test
    @Tag(TestTags.RESTDOC)
    fun `getOne needs authentication`() {
        this.performGet("$baseUrl/${randomUUID()}").expectUnauthorized()
    }

    private fun mockNfcInfo(
        id: UUID = randomUUID(),
        ownerId: UUID,
        creatorId: UUID,
        name: String = "filename",
        type: NfcInfoType = NfcInfoType.UNDEFINED
    ): NfcInfo {
        return NfcInfo(
            id,
            ownerId = ownerId,
            creatorId = creatorId,
            name = name,
            type = type
        )
    }

    private fun nfcInfosResponseFields(prefix: String = ""): List<FieldDescriptor> {
        return arrayListOf(
            field(prefix + "id", STRING, "UUID"),
            field(prefix + "ownerId", STRING, "UUID of Owner"),
            field(prefix + "creatorId", STRING, "UUID of Creator"),
            field(prefix + "type", STRING, "NfcInfoType"),
            field(prefix + "name", STRING, "File to name that file locally"),
            field(prefix + "linkedPersonId", STRING, "Optional linkedPerson").optional(),
            field(prefix + "linkedAlbumId", STRING, "Optional linkedAlbum").optional(),
            field(prefix + "createdAt", STRING, "LocalDateTime of NfcInfo creation"),
            field(prefix + "updatedAt", STRING, "LocalDateTime of NfcInfo update").optional()
        )
    }

}
