package org.ossiaustria.amigo.platform.config

fun String?.censor(): String? {
    if (this == null)
        return null
    val censoringShortener = 3
    return if (this.length >= censoringShortener) {
        val censoringBegin = this.length / censoringShortener
        this.replaceRange(censoringBegin, this.length - 1, "*****")
    } else {
        "**"
    }
}






