package io.simplyservers.simplecommand.core

import java.util.*

//typealias PermissionAcceptor = (S) -> Boolean

class FunctionNode<S>(val name: String, vararg val aliases: String) : BaseNode<S>() {
    var description: String? = null
    var permission: ((S) -> Boolean)? = null
}

fun FunctionNode<*>.matches(matchName: String): Boolean {
    return sequence {
        yield(name)
        yieldAll(aliases.iterator())
    }.any { it.equals(matchName, ignoreCase = true) }
}

class ArgumentPreNode<S>(val referenceName: String) : Node<S>() {

    init {
        require(referenceName.oneWord) { "referenceName must be one word" }
    }

    var description: String? = null

    fun <A> ifType(argumentType: ArgumentType<A>, block: ArgumentNode<S, A>.() -> Unit) {
        val argumentNode = ArgumentNode<S, A>(referenceName, argumentType)
        nodes.add(argumentNode)
        block(argumentNode)
    }
}

sealed class Node<S> {
    val nodes = ArrayList<Node<S>>()
}

class ArgumentNode<S, T>(val referenceName: String, val argumentType: ArgumentType<T>) : BaseNode<S>() {
    var description: String? = null
}

sealed class BaseNode<S> : Node<S>() {

    val executions = ArrayList<suspend (sender: S, node: Node<S>, args: Map<String, Any?>) -> Unit>()

    fun subCmd(name: String, vararg aliases: String, block: FunctionNode<S>.() -> Unit) {
        require(name.oneWord) { "The name must be one word" }
        cmd<S>(name, *aliases) {
            block(this)
            this@BaseNode.nodes.add(this)
        }
    }

    fun <T> argWithType(referenceName: String, type: ArgumentType<T>, block: ArgumentNode<S, T>.() -> Unit) {

        val arg = ArgumentPreNode<S>(referenceName)
        arg.ifType(type, block)

        this@BaseNode.nodes.add(arg)
    }

    fun arg(referenceName: String, block: ArgumentPreNode<S>.() -> Unit) {
        val arg = ArgumentPreNode<S>(referenceName)
        block(arg)
        // TODO: fix
        this@BaseNode.nodes.add(arg)
    }


    fun execute(block: suspend (sender: S, node: Node<S>, args: Map<String, Any?>) -> Unit) {
        executions.add(block)
    }
}

fun <S> cmd(name: String, vararg aliases: String, block: FunctionNode<S>.() -> Unit = {}): FunctionNode<S> {
    val functionNode = FunctionNode<S>(name, *aliases)
    block(functionNode)
    return functionNode
}


