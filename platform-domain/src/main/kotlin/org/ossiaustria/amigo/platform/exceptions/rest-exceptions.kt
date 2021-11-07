package org.ossiaustria.amigo.platform.exceptions

import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ResponseStatus

enum class ErrorCode(val errorCode: Int, val errorName: String) {
    // authentication and general errors: 1xxx
    NotFound(1404, "Entity not found"),
    Conflict(1409, "Entity already exists"),
    Unauthorized(401, "Access denied exception"),
    ValidationFailed(1400, "ValidationFailed"),


    // Requests error
    BadParametersRequest(1601, "Bad request parameters"),

    // specific user management errors 2xxx
    UserNotExisting(2002, "User does not exist"),
    UserBadCredentials(2003, "Username or password is incorrect"),

    CallChangeNotSenderError(3001, "Call can just be manipulated by sender for this request"),
    CallChangeNotReceiverError(3002, "Call can just be manipulated by receiver for this request"),
    AlbumNotFound(4001, "Album not Found"),
}

@ResponseStatus(
    code = HttpStatus.BAD_REQUEST,
    reason = "Operation cannot be executed due to malformed input or invalid states."
)
open class RestException(
    val errorCode: Int,
    val errorName: String,
    detailMessage: String? = null,
    cause: Throwable? = null
) : RuntimeException(detailMessage, cause) {

    constructor(errorCode: ErrorCode, detailMessage: String) : this(
        errorCode.errorCode,
        errorCode.errorName,
        detailMessage
    )
}

@ResponseStatus(
    code = HttpStatus.BAD_REQUEST,
    reason = "Operation cannot be executed due to malformed input or invalid states."
)
class ValidationException(message: String, val validationErrors: Array<FieldError?> = arrayOf()) :
    RestException(ErrorCode.ValidationFailed, message) {
    constructor(validationErrors: Array<FieldError?>) :
        this(validationErrors.joinToString("\n") { it.toString() }, validationErrors)

}

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Cannot create entity due to a bad request")
class BadRequestException(errorCode: ErrorCode, detailMessage: String) : RestException(errorCode, detailMessage)

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Unauthorized")
class UnauthorizedException(message: String? = null) : RestException(
    ErrorCode.Unauthorized, message ?: "Unauthorized"
)


@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Entity not found")
open class NotFoundException(errorCode: ErrorCode, message: String) : RestException(errorCode, message)

@ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED, reason = "Method not allowed or supported")
class MethodNotAllowedException(errorCode: ErrorCode, message: String) : RestException(errorCode, message)

@ResponseStatus(code = HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS, reason = "Reserved name forbidden to use")
class ForbiddenContentException(errorCode: ErrorCode, message: String) : RestException(errorCode, message)


@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Cannot create entity due to a duplicate conflict:")
class ConflictException(errorCode: ErrorCode, message: String) : RestException(errorCode, message)

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "User not found")
open class UnknownUserException(message: String? = null) :
    NotFoundException(ErrorCode.UserNotExisting, message ?: "User is unknown and does not exist")

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "User not found")
open class UserNotFoundException(message: String = "User is unknown and or not exist") :
    NotFoundException(ErrorCode.UserNotExisting, message)

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Entity not found")
class DefaultNotFoundException(message: String = "Entity does not exist") :
    NotFoundException(ErrorCode.UserNotExisting, message)


