package org.ossiaustria.amigo.platform.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.ossiaustria.amigo.platform.ApplicationProfiles
import org.ossiaustria.amigo.platform.domain.services.messaging.MessagingService
import org.ossiaustria.amigo.platform.domain.services.messaging.NoopMessagingService
import org.ossiaustria.amigo.platform.services.messaging.FirebaseMessagingService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.FileUrlResource
import java.util.logging.Logger

@Configuration
@Profile(ApplicationProfiles.NOT_TEST)
class FirebaseMessagingConfig {

    @Bean
    fun messagingService(): MessagingService =
        try {
            val googleCredentials = GoogleCredentials
                .fromStream(FileUrlResource(FIREBASE_CONFIG_FILE).inputStream)
            val firebaseOptions = FirebaseOptions
                .builder()
                .setCredentials(googleCredentials)
                .build()
            val app = FirebaseApp.initializeApp(firebaseOptions, "my-app")
            val firebaseMessaging = FirebaseMessaging.getInstance(app)
            FirebaseMessagingService(firebaseMessaging)
        } catch (e: Exception) {
            log.warning("# MISSING CONFIG: $FIREBASE_CONFIG_FILE")
            log.warning("# Cannot use Firebase messaging for this server instance")
            NoopMessagingService()
        }


    companion object {
        private val log: Logger = Logger.getLogger(this::class.simpleName)
        private const val FIREBASE_CONFIG_FILE: String = "configs/firebase-service-account.json"
    }
}

@Configuration
@Profile(ApplicationProfiles.TEST)
class NoopMessagingConfig {

    @Bean
    fun noopMessagingService(): MessagingService {
        return NoopMessagingService()
    }

}