package io.simplyservers.simplecommand.core

import java.util.*

typealias PermissionGetter<_USER> = (_USER, Permissible) -> Boolean

fun <T> has(permission: String): PermissionGetter<T> = { _, hasPermission -> hasPermission(permission) }

class FunctionNode<_USER>(val name: String, vararg val aliases: String, val group: String? = null) : BaseNode<_USER>() {
    var description: String? = null
    var permission: PermissionGetter<_USER>? = null
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

class ArgumentPreNode<_USER>(val referenceName: String) : Node<_USER>() {

    init {
        require(referenceName.oneWord) { "referenceName must be one word" }
    }

    var description: String? = null

    fun <_ARGTYPE> ifType(argumentType: ArgumentType<_ARGTYPE>, block: ArgumentNode<_USER, _ARGTYPE>.() -> Unit) {
        val argumentNode = ArgumentNode<_USER, _ARGTYPE>(referenceName, argumentType)
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

class ArgumentNode<S, T>(val referenceName: String, val argumentType: ArgumentType<T>) : BaseNode<S>() {
    var description: String? = null
    override fun toString(): String {
        return "ArgumentNode(referenceName='$referenceName', argumentType=$argumentType, description=$description)"
    }
}

sealed class BaseNode<_USER> : Node<_USER>() {

    val executions = ArrayList<suspend (sender: _USER, node: Node<_USER>, args: Map<String, Any?>) -> Unit>()

    fun subCmd(name: String, vararg aliases: String, block: FunctionNode<_USER>.() -> Unit) {
        require(name.oneWord) { "The name must be one word" }
        cmd<_USER>(name, *aliases) {
            block(this)
            this@BaseNode.children.add(this)
        }
    }

    fun subCmd(name: String, vararg aliases: String, functionNode: FunctionNode<_USER>) {
        require(name.oneWord) { "The name must be one word" }
        children.add(functionNode)
    }

    fun <T> argWithType(
        referenceName: String,
        type: ArgumentType<T>,
        description: String? = null,
        block: ArgumentNode<_USER, T>.() -> Unit
    ) {

        val arg = ArgumentPreNode<_USER>(referenceName)
        arg.description = description
        arg.ifType(type, block)

        this@BaseNode.children.add(arg)
    }

    fun arg(referenceName: String, block: ArgumentPreNode<_USER>.() -> Unit) {
        val arg = ArgumentPreNode<_USER>(referenceName)
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
    block: FunctionNode<S>.() -> Unit = {}
): FunctionNode<S> {
    val functionNode = FunctionNode<S>(name, *aliases, group = group)
    block(functionNode)
    return functionNode
}

