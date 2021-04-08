package org.ossiaustria.amigo.platform.rest.v1

import com.ninjasquad.springmockk.SpykBean
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.services.MessageService
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.rest.v1.auth.AccountDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.SimpleMailMessage
import org.springframework.restdocs.payload.PayloadDocumentation
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.requestParameters
import org.springframework.test.annotation.Rollback
import java.util.*
import javax.mail.internet.MimeMessage
import javax.transaction.Transactional

class MessagesApiTest : AbstractRestApiTest() {

    val baseUrl = "/v1/messages"

    @SpykBean
    lateinit var messageService: MessageService

    @Autowired
    private lateinit var accountSubjectPreparationTrait: AccountSubjectPreparationTrait


    @BeforeEach

    fun before() {
//        truncateAllTables()

        accountSubjectPreparationTrait.apply()

        account = accountSubjectPreparationTrait.account

        every { messageService.getAll() } returns listOf()
    }

    @Transactional
    @Rollback
    @Test
    @Tag(TestTags.RESTDOC)
    fun `Can request password reset by email`() {
        val existingUser = createMockUser()
        val id = existingUser.id
        val url = "$baseUrl/filter?receiverId=${id}"

        every { messageService.getAll() } returns listOf(
            mockMessage(senderId = id, receiverId = UUID.randomUUID()),
            mockMessage(senderId = id, receiverId = UUID.randomUUID()),
            mockMessage(senderId = id, receiverId = UUID.randomUUID()),
        )

        mockUserAuthentication()

        this.performPost(url, token)
            .expectOk()
            .document(
                "messages-filter",
                requestParameters(
                    parameterWithName("user_id").optional().description("Internal User id - UUID"),
                    parameterWithName("email").optional().description("User email"),
                    parameterWithName("user_name").optional().description("Username")
                )
            )
    }

    private fun mockMessage(senderId: UUID, receiverId: UUID): Message {
        return Message(UUID.randomUUID(), senderId = senderId, receiverId = receiverId, text = "text")
    }


}