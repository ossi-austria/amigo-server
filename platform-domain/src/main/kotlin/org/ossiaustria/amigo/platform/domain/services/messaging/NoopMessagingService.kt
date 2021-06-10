package org.ossiaustria.amigo.platform.domain.services.messaging

import com.google.firebase.messaging.AndroidConfig

class NoopMessagingService : MessagingService {

    override fun sendNotification(
        data: Map<String, String>,
        token: String?,
        topic: String?,
        prio: AndroidConfig.Priority
    ): NotificationResult {
        return NotificationResult("id")
    }

}