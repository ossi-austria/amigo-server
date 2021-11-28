package org.ossiaustria.amigo.platform.rest.v1.user


import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import org.ossiaustria.amigo.platform.domain.models.Account
import org.ossiaustria.amigo.platform.domain.services.PersonProfileService
import org.ossiaustria.amigo.platform.exceptions.UserNotFoundException
import org.ossiaustria.amigo.platform.rest.CurrentUserService
import org.ossiaustria.amigo.platform.rest.v1.common.Headers
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/v1/profile", produces = ["application/json"])
class PersonProfileApi(
    val personService: PersonProfileService,
    val currentUserService: CurrentUserService
) {

    @ApiOperation("Get own Person information (=Profile) for primary or given personId")
    @GetMapping
    fun myProfile(
        @ApiParam(hidden = true)
        account: Account,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null
    ): PersonDto =
        personService
            .findById(account.person(personId).id)
            .orThrow(UserNotFoundException())
            .toDto()

    @ApiOperation("Update Profile for primary or given personId")
    @PatchMapping
    fun updateProfile(
        @ApiParam(hidden = true)
        account: Account,

        @RequestBody
        changePersonDto: ChangePersonDto,
        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null
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
        @ApiParam(hidden = true)
        account: Account,

        @RequestPart("file")
        file: MultipartFile,

        @ApiParam(required = false, value = "Optional personId")
        @RequestHeader(Headers.PID, required = false)
        personId: UUID? = null
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
