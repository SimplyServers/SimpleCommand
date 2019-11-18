package io.simplyservers.simplecommand.core

/**
 * An argument which is a double
 */
private val doubleP = Processor.single { it.toDoubleOrNull() }

object ArgumentDouble : ArgumentType<Double>, Processor<Double> by doubleP {
    override val name = "double"

    override suspend fun autoComplete(): List<String> {
        return emptyList()
    }
}

private val stringP = Processor.single { it }

object ArgumentString : ArgumentType<String>, Processor<String> by stringP {

    override val name = "string"

    override suspend fun autoComplete(): List<String> {
        return emptyList()
    }
}

private val longP = Processor.single { it.toLongOrNull() }

object ArgumentLong : ArgumentType<Long>, Processor<Long> by longP {

    override val name = "long"

    override suspend fun autoComplete(): List<String> = emptyList()
}

object ArgumentSentence : ArgumentType<String> {
    override suspend fun process(string: List<String>): ProcessResult<String> {
        return ProcessResult.Success(string.joinToString(separator = " "), emptyList())
    }

    override val name: String
        get() = "words"

    override suspend fun autoComplete(): List<String> = emptyList()
}


private val intP = Processor.single { it.toIntOrNull() }

object ArgumentInt : ArgumentType<Int>, Processor<Int> by intP {

    override val name = "int"

    override suspend fun autoComplete(): List<String> {
        return emptyList()
    }
}

sealed class ProcessResult<T> {
    data class Success<T>(val result: T, val argsNext: List<String>) : ProcessResult<T>() {
        companion object {
            fun <T> single(result: T, argsPrevious: List<String>): Success<T> {
                val sliced = argsPrevious.slice(1 until argsPrevious.size)
                return Success(result, sliced)
            }
        }
    }

    class Failure<T> : ProcessResult<T>()
}

interface Processor<T> {
    suspend fun process(string: List<String>): ProcessResult<T>

    companion object {
        fun <T> single(block: suspend (String) -> T?): Processor<T> {
            return object : Processor<T> {
                override suspend fun process(args: List<String>): ProcessResult<T> {
                    val first = args.first()
                    val result = block(first) ?: return ProcessResult.Failure()
                    return ProcessResult.Success.single(result, args)
                }

            }
        }
    }
}

abstract class SingularArg<T>(override val name: String) : ArgumentType<T> {

    abstract suspend fun processArg(arg: String): T?

    private val processorSingle = Processor.single(::processArg)

    override suspend fun process(string: List<String>): ProcessResult<T> {
        return processorSingle.process(string)
    }
}

interface ArgumentType<T> : Processor<T> {

    /**
     * @return returns null if could not process. SimpleCommand will then move on to the next ArgumentType if one exists.
     * If none exists, it will fail.
     */

    /**
     * The name of the argument
     */
    val name: String

    /**
     * Give a list of possible arguments
     */
    suspend fun autoComplete(): List<String> = emptyList()
}

