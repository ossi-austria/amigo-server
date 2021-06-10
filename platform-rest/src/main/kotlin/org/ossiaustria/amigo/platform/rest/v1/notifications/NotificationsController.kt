package org.ossiaustria.amigo.platform.rest.v1.notifications


import com.google.firebase.messaging.AndroidConfig
import org.ossiaustria.amigo.platform.domain.services.messaging.MessagingService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/notifications", produces = ["application/json"], consumes = ["application/json"])
class NotificationsController(
    val messagingService: MessagingService,
) {

    @PostMapping("/test-send")
    fun sendNotification(@RequestBody notificationRequest: NotificationRequest) = messagingService
        .sendNotification(
            data = notificationRequest.data,
            token = notificationRequest.token,
            topic = notificationRequest.topic,
            prio = notificationRequest.prio,
        )
}


data class NotificationRequest(
    val token: String,
    val topic: String? = null,
    val prio: AndroidConfig.Priority = AndroidConfig.Priority.HIGH,
    val data: Map<String, String> = emptyMap(),
)

