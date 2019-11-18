package io.simplyservers.simplecommand.core

import java.util.*

typealias PermissionGetter<_USER> = (_USER, Permissible) -> Boolean

private fun String.splitPerm() = split(".")

private fun String.matches(input: List<String>): Boolean {
    val splitTest = splitPerm()
    val lastIndex = input.size - 1
    for(i in splitTest.indices){
        val testElem = splitTest[i]
        if(testElem == "*") return true // test is a.* and input is a.b.c.d
        if(input[i] != testElem) return false // test is a.b and input is a.c
        if(i == lastIndex) return true // test is a.b.c and input is a.b
    }
    return false // test is a.b and input is a.b.c
}

fun <T> has(permission: String): PermissionGetter<T> = { _, permissions ->
    val splitPerm = permission.splitPerm()
    permissions.any { it.matches(splitPerm) }
}

fun <T> none(): PermissionGetter<T> = { _, _ -> true }

class FunctionNode<_USER>(
    val name: String,
    vararg val aliases: String,
    var group: String? = null,
    private val previousPath: String? = null
) : BaseNode<_USER>() {
    var description: String? = null
    var permission: PermissionGetter<_USER>? = has(path)

    override val path get() = basePath ?: genPath(previousPath)

    var basePath: String? = null

    override fun toString(): String {
        return "FunctionNode(name='$name', aliases=${aliases.contentToString()}, description=$description, permission=$permission)"
    }

}

val FunctionNode<*>.equivalentNames
    get() = sequence {
        yield(name)
        yieldAll(aliases.iterator())
    }

fun FunctionNode<*>.matches(matchName: String): Boolean =
    equivalentNames.any { it.equals(matchName, ignoreCase = true) }

class ArgumentPreNode<_USER>(val referenceName: String, private val previousPath: String) : Node<_USER>() {

    init {
        require(referenceName.oneWord) { "referenceName must be one word" }
    }

    var description: String? = null

    fun <_ARGTYPE> ifType(argumentType: ArgumentType<_ARGTYPE>, block: ArgumentNode<_USER, _ARGTYPE>.() -> Unit) {
        val argumentNode = ArgumentNode<_USER, _ARGTYPE>(referenceName, argumentType, previousPath)
        children.add(argumentNode)
        block(argumentNode)
    }
}

@DslMarker
annotation class CommandTagMarker

@CommandTagMarker
sealed class Node<S> {
    val children = ArrayList<Node<S>>()
}

class ArgumentNode<S, T>(
    val referenceName: String,
    val argumentType: ArgumentType<T>,
    private val previousPath: String
) : BaseNode<S>() {
    var description: String? = null
    override val path get() = "$previousPath.${argumentType.name}"
    override fun toString(): String {
        return "ArgumentNode(referenceName='$referenceName', argumentType=$argumentType, description=$description)"
    }
}

sealed class BaseNode<_USER> : Node<_USER>() {

    open val path: String get() = ""
    val executions = ArrayList<suspend (sender: _USER, node: Node<_USER>, args: Map<String, Any?>) -> Unit>()

    fun subCmd(name: String, vararg aliases: String, block: FunctionNode<_USER>.() -> Unit) {
        require(name.oneWord) { "The name must be one word" }
        cmd<_USER>(name, *aliases, startingPath = path) {
            block(this)
            this@BaseNode.children.add(this)
        }
    }

    fun <T> argWithType(
        referenceName: String,
        type: ArgumentType<T>,
        description: String? = null,
        block: ArgumentNode<_USER, T>.() -> Unit
    ) {

        val arg = ArgumentPreNode<_USER>(referenceName, path)
        arg.description = description
        arg.ifType(type, block)

        this@BaseNode.children.add(arg)
    }

    fun arg(referenceName: String, block: ArgumentPreNode<_USER>.() -> Unit) {
        val arg = ArgumentPreNode<_USER>(referenceName, path)
        block(arg)
        // TODO: fix
        this@BaseNode.children.add(arg)
    }

    @CommandTagMarker
    fun execute(block: suspend (sender: _USER, node: Node<_USER>, args: Map<String, Any?>) -> Unit) {
        executions.add(block)
    }
}

fun <S> cmd(
    name: String,
    vararg aliases: String,
    group: String? = null,
    startingPath: String? = null,
    block: FunctionNode<S>.() -> Unit = {}
): FunctionNode<S> {
    val functionNode = FunctionNode<S>(name, *aliases, group = group, previousPath = startingPath)
    block(functionNode)
    return functionNode
}

private fun FunctionNode<*>.genPath(path: String? = null) =
    listOfNotNull(path, group, name)
        .mapNotNull { if (it == "") null else it }
        .joinToString(separator = ".")
