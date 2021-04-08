package org.ossiaustria.amigo.platform.services

import org.ossiaustria.amigo.platform.repositories.MessageRepository
import org.ossiaustria.amigo.platform.domain.models.Message
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service



interface SendableService {

}

@Service
class SendableServiceImpl : SendableService {

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }

}