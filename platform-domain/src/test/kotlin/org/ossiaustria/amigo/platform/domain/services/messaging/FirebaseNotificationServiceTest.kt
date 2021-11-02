package org.ossiaustria.amigo.platform.domain.services.messaging

import com.google.firebase.messaging.FirebaseMessaging
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.services.AbstractServiceTest
import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import java.util.UUID.randomUUID

internal class FirebaseNotificationServiceTest : AbstractServiceTest() {


    private lateinit var service: FirebaseNotificationService

    @MockkBean(relaxed = true, relaxUnitFun = true)
    private lateinit var firebaseMessaging: FirebaseMessaging

    @MockkBean
    private lateinit var authService: AuthService


    @BeforeEach
    fun beforeEach() {
        cleanTables()

        service = FirebaseNotificationService(firebaseMessaging, authService)
        cleanTables()
    }


    @Test
    fun `messageSent should return true when FB Message and receiver's token are valid`() {

        every { authService.findOneByPersonId(personId2) } returns
            Account(randomUUID(), "email", "password", fcmToken = "valid")

        every { firebaseMessaging.send(any()) } returns "id"

        val result = service.messageSent(personId2, Message(existingId, personId1, personId2, "text"))
        assertThat(result).isEqualTo(true)
    }

    @Test
    fun `messageSent should return false when receiver's token is null`() {

        every { authService.findOneByPersonId(personId2) } returns
            Account(randomUUID(), "email", "password", fcmToken = null)

        verify(exactly = 0) { firebaseMessaging.send(any()) }

        val result = service.messageSent(personId2, Message(existingId, personId1, personId2, "text"))
        assertThat(result).isEqualTo(false)
    }

    @Test
    fun `messageSent should return false when fcm has an error`() {

        every { authService.findOneByPersonId(personId2) } returns
            Account(randomUUID(), "email", "password", fcmToken = "valid")

        every { firebaseMessaging.send(any()) } throws IllegalArgumentException("Some exception")

        val result = service.messageSent(personId2, Message(existingId, personId1, personId2, "text"))
        assertThat(result).isEqualTo(false)
    }

}
