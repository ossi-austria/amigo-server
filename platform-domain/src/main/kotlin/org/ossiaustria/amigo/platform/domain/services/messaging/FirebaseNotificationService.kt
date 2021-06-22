package org.ossiaustria.amigo.platform.domain.services.messaging

import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import org.ossiaustria.amigo.platform.domain.models.Multimedia
import org.ossiaustria.amigo.platform.domain.models.Sendable
import org.ossiaustria.amigo.platform.domain.services.AccountService
import org.slf4j.LoggerFactory
import java.util.*

class FirebaseNotificationService(
    private val firebaseMessaging: FirebaseMessaging,
    private val accountService: AccountService,
) : NotificationService {

    override fun messageSent(
        receiverId: UUID,
        message: org.ossiaustria.amigo.platform.domain.models.Message
    ) = sendableSent(receiverId, message)

    override fun multimediaSent(receiverId: UUID, multimedia: Multimedia) =
        sendableSent(receiverId, multimedia)

    private fun <S> sendableSent(receiverId: UUID, sendable: Sendable<S>): Boolean {
        val receiver = accountService.findOneByPersonId(receiverId)
        val data = NotificationBuilder.buildSendableSent(sendable)
        val notification: Message? = buildMessagePush(data, receiver?.fcmToken)
        return if (notification != null) sendNotification(notification) else false
    }

    override fun sendNotification(
        data: Map<String, String>,
        token: String?,
        topic: String?,
        prio: AndroidConfig.Priority
    ): Boolean {
        val notification: Message? = buildCallPush(data, token)
        return if (notification != null) sendNotification(notification) else false
    }

    private fun sendNotification(notification: Message) = try {
        firebaseMessaging.send(notification)
        true
    } catch (e: Exception) {
        Log.error("Could not sendNotification", e)
        false
    }

    private fun buildCallPush(data: Map<String, String>, token: String?): Message? =
        buildPush(data, token, AndroidConfig.Priority.HIGH, 10)

    private fun buildMessagePush(data: Map<String, String>, token: String?): Message? =
        buildPush(data, token, AndroidConfig.Priority.HIGH, 60 * 5L)

    private fun buildPush(
        data: Map<String, String>,
        token: String?,
        prio: AndroidConfig.Priority,
        ttl: Long = 15
    ): Message? {
        if (token.isNullOrBlank()) {
            Log.error("Cannot create FCM message without valid token: '$token'")
            return null
        }
        val builder = Message.builder()
        builder.putAllData(data).setAndroidConfig(androidConfig(prio, ttl))
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