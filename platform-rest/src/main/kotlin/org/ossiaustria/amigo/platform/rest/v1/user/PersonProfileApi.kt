package org.ossiaustria.amigo.platform.rest.v1.user


import io.micrometer.core.annotation.Timed
import io.swagger.annotations.ApiOperation
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

    @ApiOperation("Get own Person information (=Profile) for primary or given personId")
    @GetMapping
    fun myProfile(
        account: Account,
        @RequestHeader("Amigo-Person-Id",required = false) personId: UUID? = null
    ): PersonDto =
        personService
            .findById(account.person(personId).id)
            .orThrow(UserNotFoundException())
            .toDto()

    @ApiOperation("Update Profile for primary or given personId")
    @PatchMapping
    fun updateProfile(
        account: Account,
        @RequestBody changePersonDto: ChangePersonDto,
        @RequestHeader("Amigo-Person-Id",required = false) personId: UUID? = null
    ): PersonDto =
        personService
            .changeNameAndAvatarUrl(account.person(personId), changePersonDto.name, changePersonDto.avatarUrl)
            .toDto()


    @ApiOperation("Upload Profile's avatar for primary or given personId")
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
        @RequestPart("file") file: MultipartFile,
        @RequestHeader("Amigo-Person-Id",required = false) personId: UUID? = null
    ): PersonDto =
        personService
            .uploadAvatar(account.person(personId), file)
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
