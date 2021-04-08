package org.ossiaustria.amigo.platform.services.email

import java.time.ZonedDateTime
import java.util.*

data class Email(
    val id: UUID,
    val accountId: UUID,
    val userName: String,
    val recipientEmail: String,
    val nothing: Nothing?,
    val subject: String,
    val failedMessage: String?,
    val sentAt: ZonedDateTime?
) {

}
