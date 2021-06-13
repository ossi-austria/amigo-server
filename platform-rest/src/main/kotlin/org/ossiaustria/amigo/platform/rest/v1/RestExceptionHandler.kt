package org.ossiaustria.amigo.platform.rest.v1

import org.hibernate.exception.ConstraintViolationException
import org.ossiaustria.amigo.platform.domain.services.ServiceError
import org.ossiaustria.amigo.platform.exceptions.*
import org.ossiaustria.amigo.platform.rest.v1.common.RestExceptionDto
import org.ossiaustria.amigo.platform.rest.v1.common.ValidationFailureDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.time.LocalDateTime


@ControllerAdvice
class RestExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(exception: NotFoundException): ResponseEntity<RestExceptionDto> {
        val error = RestExceptionDto(exception)
        return ResponseEntity(error, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(exception: UnauthorizedException): ResponseEntity<RestExceptionDto> {
        val error = RestExceptionDto(exception)
        return ResponseEntity(error, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(exception: ConstraintViolationException): ResponseEntity<RestExceptionDto> {
        val error = RestExceptionDto(RestException(ErrorCode.Conflict, "already exists"))
        return ResponseEntity(error, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(RestException::class)
    fun handleException(exception: RestException): ResponseEntity<RestExceptionDto> {
        val error = RestExceptionDto(exception)
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ServiceError::class)
    fun handleException(exception: ServiceError): ResponseEntity<RestExceptionDto> {
        val error = RestExceptionDto(exception)
        return ResponseEntity(error, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleException(exception: BadRequestException): ResponseEntity<RestExceptionDto> {
        return ResponseEntity(RestExceptionDto(exception), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(ConflictException::class)
    fun handleException(exception: ConflictException): ResponseEntity<RestExceptionDto> {
        return ResponseEntity(RestExceptionDto(exception), HttpStatus.CONFLICT)
    }

    @ExceptionHandler(ForbiddenContentException::class)
    fun handleException(exception: ForbiddenContentException): ResponseEntity<RestExceptionDto> {
        return ResponseEntity(RestExceptionDto(exception), HttpStatus.UNAVAILABLE_FOR_LEGAL_REASONS)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    fun validationError(ex: MethodArgumentNotValidException): ValidationFailureDto {
        val result = ex.bindingResult
        val fieldErrors = result.fieldErrors
        val arrayOfFieldErrors = fieldErrors.toTypedArray() as Array<FieldError?>
        return ValidationFailureDto(ValidationException(arrayOfFieldErrors), LocalDateTime.now())
    }

}


