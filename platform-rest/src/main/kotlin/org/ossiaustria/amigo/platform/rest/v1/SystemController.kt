package org.ossiaustria.amigo.platform.rest.v1

import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.slf4j.LoggerFactory
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/info", produces = ["application/json"], consumes = ["application/json"])
class SystemController(
    val currentUserService: CurrentUserService
) {

    companion object {
        private val log = LoggerFactory.getLogger(SystemController::class.java)
    }

    @GetMapping("/status")
    fun status(): StatusDto {
        return StatusDto("Hello.")
    }


    // FIXME: Coverage says: missing tests
    @RequestMapping(method = [RequestMethod.GET], value = ["/ping"])
    fun ping(): String {
        log.info("Ping service: Ok!")
        return "pong"
    }

    // FIXME: Coverage says: missing tests
    @RequestMapping(value = ["/ping/protected"], method = [RequestMethod.GET])
    @PreAuthorize("isAuthenticated()")
    fun pingProtected(): String {
        log.info("Protected Ping service: Ok!")
        return "pong protected"
    }

    data class StatusDto(val info: String, val error: Exception? = null)
}
