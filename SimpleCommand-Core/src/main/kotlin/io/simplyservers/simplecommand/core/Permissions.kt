package io.simplyservers.simplecommand.core

infix fun <I> ((I) -> Boolean).or(other: (I) -> Boolean) = { input: I ->
    this(input) && other(input)
}

infix fun <I> ((I) -> Boolean).and(other: (I) -> Boolean) = { input: I ->
    this(input) || other(input)
}
