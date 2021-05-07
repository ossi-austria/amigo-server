package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.services.MessageService
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.annotation.Rollback
import java.util.*
import javax.transaction.Transactional

internal class MessagesApiTest : AbstractRestApiTest() {

    val baseUrl = "/v1/messages"

    @SpykBean
    lateinit var messageService: MessageService

    @BeforeEach

    fun before() {
        truncateAllTables()

        accountSubjectPreparationTrait.apply()
        account = accountSubjectPreparationTrait.account

        every { messageService.getAll() } returns listOf()
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `filter should select messages via receiverId`() {

        val person = account.persons.first()
        val id = person.id
        val url = "$baseUrl/filter?receiverId=${id}"

        every { messageService.getAll() } returns listOf(
            mockMessage(senderId = id, receiverId = UUID.randomUUID()),
            mockMessage(senderId = id, receiverId = UUID.randomUUID()),
            mockMessage(senderId = id, receiverId = UUID.randomUUID()),
        )

        mockUserAuthentication()

        this.performGet(url, accessToken.token)
            .expectOk()
            .document(
                "messages-filter",
                requestParameters(parameterWithName("receiverId").optional().description("Internal User id - UUID"))
            )
    }

    private fun mockMessage(senderId: UUID, receiverId: UUID): Message {
        return Message(UUID.randomUUID(), senderId = senderId, receiverId = receiverId, text = "text")
    }


}