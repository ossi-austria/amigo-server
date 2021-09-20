package org.ossiaustria.amigo.platform.rest.v1.user


import io.micrometer.core.annotation.Timed
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.services.PersonService
import org.ossiaustria.amigo.platform.exceptions.BadRequestException
import org.ossiaustria.amigo.platform.exceptions.ErrorCode
import org.ossiaustria.amigo.platform.exceptions.NotFoundException
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import javax.servlet.http.HttpServletResponse

@Timed(value = "time.api.profile")
@RestController
@RequestMapping("/v1/persons")
class PersonsApi(
    val personService: PersonService,
    val currentUserService: CurrentUserService
) {

    @GetMapping("/{id}/avatar.*")
    fun profileAvatar(
        @PathVariable("id") id: UUID,
        @RequestHeader("Amigo-Person-Id", required = false) personId: UUID? = null,
        account: Account,
        request: HttpServletResponse
    ): ResponseEntity<Resource> {

        val personAvatar = personService.loadAvatar(account.person(personId), id)

        if (personAvatar.isUseless) {
            throw NotFoundException(ErrorCode.NotFound, "This Person has no avatar (yet).")
        } else if (personAvatar.resource != null) {
            try {
                val resource = personAvatar.resource!!
                val header = "attachment; filename=${personAvatar.filename}"
                val toString = resource.contentLength().toString()
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, header)
                    .header(HttpHeaders.CONTENT_TYPE, personAvatar.contentType)
                    .header(HttpHeaders.CONTENT_LENGTH, toString)
                    .body(resource)
            } catch (e: Exception) {
                throw BadRequestException(ErrorCode.NotFound, e.message!!)
            }
        } else {
            request.setHeader("Location", personAvatar.person.avatarUrl)
            request.status = 302
            return ResponseEntity.status(HttpStatus.TEMPORARY_REDIRECT).build()
        }
    }
}
