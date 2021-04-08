package org.ossiaustria.amigo.platform.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import javax.annotation.PostConstruct

@Configuration
class MessagingConfig() {
    companion object {
        private val log = LoggerFactory.getLogger(MessagingConfig::class.java)
        const val REFRESH_USER_INFORMATION_TOPIC = "pubsub:queue:amigoplatform:userupdate"
        const val REFRESH_GROUP_INFORMATION_TOPIC = "pubsub:queue:amigoplatform:groupupdate"
        const val REFRESH_PROJECT_TOPIC = "pubsub:queue:amigoplatform:projectupdate"
    }

    @PostConstruct
    fun create() {
        log.debug("Messaging configuration is being created...")
    }

//    @Bean
//    fun userInformationMessageListenerAdapter(): MessageListenerAdapter {
//        return MessageListenerAdapter(sessionService)
//    }
//
//    @Bean
//    fun projectMessageListenerAdapter(): MessageListenerAdapter {
//        return MessageListenerAdapter(publicProjectsCacheService)
//    }

    @Bean
    @Qualifier("refreshUserInformation")
    fun refreshUserTopic(): ChannelTopic {
        return ChannelTopic(REFRESH_USER_INFORMATION_TOPIC)
    }

    @Bean
    @Qualifier("refreshGroupInformation")
    fun refreshGroupTopic(): ChannelTopic {
        return ChannelTopic(REFRESH_GROUP_INFORMATION_TOPIC)
    }

    @Bean
    @Qualifier("refreshProject")
    fun refreshProjectTopic(): ChannelTopic {
        return ChannelTopic(REFRESH_PROJECT_TOPIC)
    }

    @Bean
    fun redisContainer(): RedisMessageListenerContainer? {
        val container = RedisMessageListenerContainer()
//        container.connectionFactory = connectionFactory
//        container.addMessageListener(userInformationMessageListenerAdapter(), refreshUserTopic())
//        container.addMessageListener(userInformationMessageListenerAdapter(), refreshGroupTopic())
//        container.addMessageListener(projectMessageListenerAdapter(), refreshProjectTopic())
        return container
    }
}