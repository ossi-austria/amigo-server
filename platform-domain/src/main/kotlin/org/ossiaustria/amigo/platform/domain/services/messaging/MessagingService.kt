package org.ossiaustria.amigo.platform.domain.services.messaging

import com.google.firebase.messaging.AndroidConfig

interface MessagingService {

    fun sendNotification(
        data: Map<String, String>,
        token: String? = null,
        topic: String? = null,
        prio: AndroidConfig.Priority = AndroidConfig.Priority.HIGH
    ): NotificationResult
}