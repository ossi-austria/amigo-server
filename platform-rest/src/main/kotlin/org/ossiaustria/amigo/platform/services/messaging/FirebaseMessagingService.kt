package org.ossiaustria.amigo.platform.services.messaging

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import org.ossiaustria.amigo.platform.domain.services.messaging.MessagingService
import org.ossiaustria.amigo.platform.domain.services.messaging.NotificationResult

class FirebaseMessagingService(
    private val firebaseMessaging: FirebaseMessaging
) : MessagingService {

    @Throws(FirebaseMessagingException::class)
    override fun sendNotification(
        data: Map<String, String>,
        token: String?,
        topic: String?,
        prio: AndroidConfig.Priority
    ): NotificationResult {
        val message: Message = buildCallPush(data, token!!)
        val id = firebaseMessaging.send(message)
        return NotificationResult(id)
    }

    private fun buildCallPush(data: Map<String, String>, token: String): Message =
        buildPush(data, token, AndroidConfig.Priority.HIGH, 10)

    private fun buildMessagePush(data: Map<String, String>, token: String): Message =
        buildPush(data, token, AndroidConfig.Priority.NORMAL, 60 * 5L)

    private fun buildPush(
        data: Map<String, String>,
        token: String,
        prio: AndroidConfig.Priority,
        ttl: Long = 15
    ): Message {
        val builder = Message.builder()
        builder.putAllData(data).setAndroidConfig(androidConfig(prio, ttl))
        builder.setToken(token)
        return builder.build()
    }

    private fun androidConfig(prio: AndroidConfig.Priority, ttl: Long): AndroidConfig? {
        return AndroidConfig
            .builder()
            .setDirectBootOk(prio == AndroidConfig.Priority.HIGH)
            .setPriority(prio)
            .setTtl(ttl)
            .build()

    }
}