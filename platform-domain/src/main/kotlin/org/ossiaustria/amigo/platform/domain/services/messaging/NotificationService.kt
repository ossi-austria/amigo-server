package org.ossiaustria.amigo.platform.domain.services.messaging

import com.google.firebase.messaging.AndroidConfig
import org.ossiaustria.amigo.platform.domain.models.Message
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import java.util.*

interface NotificationService {

    fun messageSent(receiverId: UUID, message: Message): Boolean
    fun multimediaSent(receiverId: UUID, multimedia: Multimedia): Boolean

    fun sendNotification(
        data: Map<String, String>,
        token: String? = null,
        topic: String? = null,
        prio: AndroidConfig.Priority = AndroidConfig.Priority.HIGH
    ): Boolean
}