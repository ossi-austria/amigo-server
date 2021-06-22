package org.ossiaustria.amigo.platform.domain.services.messaging

import com.google.firebase.messaging.AndroidConfig
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.slf4j.LoggerFactory
import java.util.*

class NoopNotificationService : NotificationService {

    override fun messageSent(receiverId: UUID, message: Message): Boolean {
        Log.info("NOOP: messageSent")
        return false
    }

    override fun multimediaSent(receiverId: UUID, multimedia: Multimedia): Boolean {
        Log.info("NOOP: multimediaSent")
        return false
    }

    @Deprecated("Use only on development servers")
    override fun sendNotification(
        data: Map<String, String>,
        token: String?,
        topic: String?,
        prio: AndroidConfig.Priority
    ): Boolean {
        Log.info("NOOP: sendNotification")
        return false
    }

    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }
}