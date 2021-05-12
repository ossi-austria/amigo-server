package org.ossiaustria.amigo.platform.domain.models

class ValidationException(message: String) : Exception(message)

interface Validator<T> {

    @Throws(ValidationException::class)
    fun validate(item: T)
}

object EmailValidator : Validator<String> {
    private val regex = """^\S*\.?\S+@\S+\.\S+$""".toRegex()

    override fun validate(item: String) {
        regex.matches(item) || throw ValidationException("Invalid email")
    }
}

object StringValidator {

    fun validateNotBlank(item: String?) {
        if (item.isNullOrBlank()) throw ValidationException("Blank string")
    }

    fun validateLength(item: String?, min: Int = 0, max: Int = 120) {
        validateNotBlank(item)
        if (item!!.length < min) throw ValidationException("Less than $min chars")
        if (item.length > max) throw ValidationException("More than $max chars")
    }
}