package org.ossiaustria.amigo.platform.domain.services.messaging

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.ossiaustria.amigo.platform.domain.models.Call
import org.ossiaustria.amigo.platform.domain.models.Sendable
import org.ossiaustria.amigo.platform.domain.services.auth.AuthService
import org.slf4j.LoggerFactory
import java.util.UUID

class FirebaseNotificationService(
    private val firebaseMessaging: FirebaseMessaging,
    private val authService: AuthService,
) : NotificationService {

    override fun messageSent(
        receiverId: UUID,
        message: org.ossiaustria.amigo.platform.domain.models.Message
    ) = sendableChanged(receiverId, message)

    override fun callChanged(receiverId: UUID, call: Call): Boolean =
        sendableChanged(receiverId, call)

    private fun <S> sendableChanged(receiverId: UUID, sendable: Sendable<S>): Boolean {
        val receiver = authService.findOneByPersonId(receiverId)
        val data = NotificationBuilder.buildSendableSent(sendable)
        val notification: Message? = buildSendablePush(data, receiver?.fcmToken)
        return if (notification != null) sendNotification(notification) else false
    }

    override fun sendNotification(
        data: Map<String, String>,
        token: String?,
        topic: String?,
        prio: AndroidConfig.Priority
    ): Boolean {
        val notification: Message? = buildSendablePush(data, token)
        return if (notification != null) sendNotification(notification) else false
    }

    private fun sendNotification(notification: Message) = try {
        firebaseMessaging.send(notification)
        true
    } catch (e: Exception) {
        Log.error("Could not sendNotification", e)
        false
    }


    private fun buildSendablePush(
        data: Map<String, String>,
        token: String?,
        priority: AndroidConfig.Priority = AndroidConfig.Priority.HIGH,
        ttl: Long = 60 * 5L
    ): Message? {
        if (token.isNullOrBlank()) {
            Log.error("Cannot create FCM message without valid token: '$token'")
            return null
        }
        val builder = Message.builder()
        builder.putAllData(data).setAndroidConfig(androidConfig(priority, ttl))
        builder.setToken(token)
        return builder.build()
    }

    private fun androidConfig(prio: AndroidConfig.Priority, ttl: Long) = AndroidConfig
        .builder()
        .setDirectBootOk(prio == AndroidConfig.Priority.HIGH)
        .setPriority(prio)
        .setTtl(ttl)
        .build()


    companion object {
        private val Log = LoggerFactory.getLogger(this::class.java)
    }
}
