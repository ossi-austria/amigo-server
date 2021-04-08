package org.ossiaustria.amigo.platform.exceptions

import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.annotation.ResponseStatus

enum class ErrorCode(val errorCode: Int, val errorName: String) {
    // authentication and general errors: 1xxx
    NotFound(1404, "Entity not found"),
    NotAllowed(1405, "Method NotAllowed "),
    Conflict(1409, "Entity already exists"),
    AccessDenied(1401, "Access denied exception"),
    ValidationFailed(1400, "ValidationFailed"),


    // Requests error
    BadParametersRequest(1601, "Bad request parameters"),

    // specific user management errors 2xxx
    UserAlreadyExisting(2001, "User already exists"),
    UserNotExisting(2002, "User does not exist"),
    UserBadCredentials(2003, "Username or password is incorrect"),
    GroupNotExisting(2004, "Group does not exist"),
    ProjectNotExisting(2005, "Project does not exist"),
    UserCreationFailedEmailOrUsernameUsed(2007, "Email or username is already in use"),
    GroupNameInvalidReserved(2008, "Cannot save group with a reserved name/slug"),

}

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Operation cannot be executed due to malformed input or invalid states.")
@Deprecated("Use BadRequestException or another specific type")
open class RestException(
    val errorCode: Int,
    val errorName: String,
    detailMessage: String? = null,
    cause: Throwable? = null) : RuntimeException(detailMessage, cause) {

    constructor(errorCode: ErrorCode) : this(errorCode.errorCode, errorCode.errorName)
    constructor(errorCode: ErrorCode, detailMessage: String) : this(errorCode.errorCode, errorCode.errorName, detailMessage)
}

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Operation cannot be executed due to malformed input or invalid states.")
class ValidationException(val validationErrors: Array<FieldError?>) : RestException(ErrorCode.ValidationFailed, validationErrors.joinToString("\n") { it.toString() })

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Cannot create entity due to a bad request")
class BadRequestException(errorCode: ErrorCode, detailMessage: String) : RestException(errorCode, detailMessage)

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "Unauthorized for the request")
class AccessDeniedException(message: String? = null) : RestException(ErrorCode.AccessDenied, message
    ?: "Access denied")

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "Bad credentials")
class IncorrectCredentialsException(message: String? = null) : RestException(ErrorCode.AccessDenied, message
    ?: "Access denied")

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Operation cannot be executed due to malformed input or invalid states.")
class InternalException(message: String? = null) : RestException(ErrorCode.ValidationFailed, message
    ?: "Internal server error")

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Entity not found")
open class NotFoundException(errorCode: ErrorCode, message: String) : RestException(errorCode, message)

@ResponseStatus(code = HttpStatus.METHOD_NOT_ALLOWED, reason = "Method not allowed or supported")
class MethodNotAllowedException(errorCode: ErrorCode, message: String) : RestException(errorCode, message)

@ResponseStatus(code = HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS, reason = "Reserved name forbidden to use")
class ForbiddenContentException(errorCode: ErrorCode, message: String) : RestException(errorCode, message)


@ResponseStatus(code = HttpStatus.CONFLICT, reason = "Cannot create entity due to a duplicate conflict:")
class ConflictException(errorCode: ErrorCode, message: String) : RestException(errorCode, message)

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "The state of internal db is inconsistent")
class NotConsistentInternalDb(message: String) : RestException(ErrorCode.Conflict, message)

@ResponseStatus(code = HttpStatus.CONFLICT, reason = "The state of internal db is inconsistent")
class UserAlreadyExistsException(username: String, email: String) : RestException(ErrorCode.UserCreationFailedEmailOrUsernameUsed, "'$username' or '$email' is already in use!")

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "User not found")
open class UnknownUserException(message: String? = null)
    : NotFoundException(ErrorCode.UserNotExisting, message ?: "User is unknown and does not exist")

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "User not found")
open class UserNotFoundException(message: String = "User is unknown and or not exist")
    : NotFoundException(ErrorCode.UserNotExisting, message)


@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Group not found")
open class UnknownGroupException(message: String? = null)
    : NotFoundException(ErrorCode.GroupNotExisting, message ?: "Group is unknown and does not exist")

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Project not found")
open class UnknownProjectException(message: String? = null)
    : NotFoundException(ErrorCode.ProjectNotExisting, message ?: "Project is unknown and does not exist")


