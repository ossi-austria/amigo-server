package org.ossiaustria.amigo.platform.domain.services.messaging

import com.google.firebase.messaging.AndroidConfig
import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.Message
import java.util.UUID

interface NotificationService {

    fun messageSent(receiverId: UUID, message: Message): Boolean
    fun callChanged(receiverId: UUID, call: Call): Boolean

    fun sendNotification(
        data: Map<String, String>,
        token: String? = null,
        topic: String? = null,
        prio: AndroidConfig.Priority = AndroidConfig.Priority.HIGH
    ): Boolean
}
