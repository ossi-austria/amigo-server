package org.ossiaustria.amigo.platform.rest.v1.user


import io.micrometer.core.annotation.Timed
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.services.PersonService
import org.ossiaustria.amigo.platform.exceptions.UserNotFoundException
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Timed(value = "time.api.profile")
@RestController
@RequestMapping("/v1/profile", produces = ["application/json"], consumes = ["application/json"])
class PersonProfileApi(
    val personService: PersonService,
    val currentUserService: CurrentUserService
) {

    @GetMapping
    fun myProfile(account: Account): PersonDto =
        personService
            .findById(account.person().id)
            .orThrow(UserNotFoundException())
            .toDto()

    @PatchMapping
    fun updateProfile(
        account: Account,
        @RequestBody changePersonDto: ChangePersonDto,
        @RequestParam(required = false) personId: UUID? = null
    ): PersonDto =
        personService
            .changeNameAndAvatarUrl(account.person(personId), changePersonDto.name, changePersonDto.avatarUrl)
            .toDto()


    @PostMapping(
        "/avatar",
        consumes = [
            MediaType.IMAGE_GIF_VALUE,
            MediaType.APPLICATION_JSON_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            MediaType.APPLICATION_OCTET_STREAM_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadAvatar(
        account: Account,
        @RequestPart("file") file: MultipartFile
    ): PersonDto =
        personService
            .uploadAvatar(account.person(), file)
            .toDto()

}

data class ChangePersonDto(
    val name: String? = null,
    val avatarUrl: String? = null,
)

fun <T> T?.orThrow(e: Exception): T {
    if (this == null) throw e
    else return this
}