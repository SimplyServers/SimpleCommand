package io.simplyservers.simplecommand.core

/**
 * An argument which is a double
 */
object ArgumentDouble : ArgumentType<Double> {
    override val name = "double"

    override suspend fun process(string: String): Double? {
        return string.toDoubleOrNull()
    }

    override suspend fun autoComplete(): List<String> {
        return emptyList()
    }
}

object ArgumentString : ArgumentType<String> {

    override val name = "string"

    override suspend fun process(string: String): String? {
        return string
    }

    override suspend fun autoComplete(): List<String> {
        return emptyList()
    }
}

object ArgumentInt : ArgumentType<Int> {

    override val name = "int"

    override suspend fun process(string: String): Int? {
        return string.toIntOrNull()
    }

    override suspend fun autoComplete(): List<String> {
        return emptyList()
    }
}

interface ArgumentType<T> {

    /**
     * @return returns null if could not process. SimpleCommand will then move on to the next ArgumentType if one exists.
     * If none exists, it will fail.
     */
    suspend fun process(string: String): T?

    /**
     * The name of the argument
     */
    val name: String

    /**
     * Give a list of possible arguments
     */
    suspend fun autoComplete(): List<String>
}

