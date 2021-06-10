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
import org.springframework.core.io.ClassPathResource

@Configuration
@Profile(ApplicationProfiles.NOT_TEST)
class FirebaseMessagingConfig {

    @Bean
    fun firebaseMessaging(): FirebaseMessaging {
        val googleCredentials = GoogleCredentials
            .fromStream(ClassPathResource("firebase-service-account.json").inputStream)
        val firebaseOptions = FirebaseOptions
            .builder()
            .setCredentials(googleCredentials)
            .build()
        val app = FirebaseApp.initializeApp(firebaseOptions, "my-app")
        return FirebaseMessaging.getInstance(app)
    }

    @Bean
    fun firebaseMessagingService(firebaseMessaging: FirebaseMessaging): MessagingService {
        return FirebaseMessagingService(firebaseMessaging)
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