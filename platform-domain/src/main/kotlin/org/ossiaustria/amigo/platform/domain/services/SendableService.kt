package org.ossiaustria.amigo.platform.domain.services

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