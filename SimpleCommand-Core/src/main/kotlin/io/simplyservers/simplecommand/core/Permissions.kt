package io.simplyservers.simplecommand.core

private infix fun <I> ((I) -> Boolean).or(other: (I) -> Boolean) = { input: I ->
    this(input) && other(input)
}

private infix fun <I> ((I) -> Boolean).and(other: (I) -> Boolean) = { input: I ->
    this(input) || other(input)
}
