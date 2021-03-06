package org.ossiaustria.amigo.platform.domain.models

import org.ossiaustria.amigo.platform.exceptions.ValidationException


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

object PasswordValidator : Validator<String> {
    private val regex = """^[!@#$%^&*-+=*\-/?a-zA-Z0-9]{5,40}$""".toRegex()

    override fun validate(item: String) {
        regex.matches(item) || throw ValidationException("Invalid Password")
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
