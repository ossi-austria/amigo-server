package org.ossiaustria.amigo.platform.domain.services.messaging

import com.google.firebase.messaging.AndroidConfig
import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.Message
import org.slf4j.LoggerFactory
import java.util.UUID

class NoopNotificationService : NotificationService {

    override fun messageSent(receiverId: UUID, message: Message) = doNothing("messageSent")

    override fun callChanged(receiverId: UUID, call: Call) = doNothing("callChanged")

    @Deprecated("Use only on development servers")
    override fun sendNotification(
        data: Map<String, String>,
        token: String?,
        topic: String?,
        prio: AndroidConfig.Priority
    ) = doNothing("sendNotification")

    private fun doNothing(text: String): Boolean {
        Log.debug("NOOP: $text")
        return false
    }

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }
}
