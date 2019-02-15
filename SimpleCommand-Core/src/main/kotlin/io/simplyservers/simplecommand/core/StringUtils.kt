package io.simplyservers.simplecommand.core

fun List<String>.sentenceEndingWith(word: String, prefixPlural: String = "", prefixSingular: String = ""): String {
    if(isEmpty()) return ""
    if(size == 1) return "$prefixSingular ${first()}"
    val firstPart = this.slice(0 until (size - 1))
    return "$prefixPlural ${firstPart.joinToString()}, $word ${last()}"
}

val String.oneWord get() = !this.contains(" ")
