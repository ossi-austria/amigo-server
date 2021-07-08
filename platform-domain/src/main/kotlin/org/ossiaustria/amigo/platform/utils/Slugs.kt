package org.ossiaustria.amigo.platform.utils

object Slugs {

    const val validPattern = """[a-z0-9\-+]{3,40}"""

    val validPatternRegex = validPattern.toRegex()
    val replacePatternRegexDash = """[^a-z0-9\-+]""".toRegex()

    fun isValid(string: String): Boolean {
        return validPatternRegex.matches(string)
    }

    fun toSlug(string: String): String {
        return replacePatternRegexDash
            .replace(string.lowercase(), "-")
    }
}