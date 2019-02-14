package io.simplyservers.simplecommands

import io.simplyservers.simplecommands.bukkit.BukkitCommandRegister
import org.bukkit.command.CommandSender
import java.util.ArrayList

typealias PermissionAcceptor = (CommandSender) -> Boolean

class FunctionNode(val name: String, vararg val aliases: String) : BaseNode() {
    var description: String? = null
    var permission: PermissionAcceptor? = null // TODO: implement
}

fun FunctionNode.matches(matchName: String): Boolean {
    return sequence {
        yield(name)
        yieldAll(aliases.iterator())
    }.any { it.equals(matchName, ignoreCase = true) }
}

class ArgumentPreNode(val referenceName: String) : Node() {


    var description: String? = null


    //    fun description(string: String) {}
    fun <A> ifType(argumentType: ArgumentType<A>, block: ArgumentNode<A>.() -> Unit) {
        val argumentNode = ArgumentNode(referenceName, argumentType)
        nodes.add(argumentNode)
        block(argumentNode)
    }
}


typealias ExecuteInfo = suspend (sender: CommandSender, node: Node, args: Map<String, Any?>) -> Unit

sealed class Node {
    val nodes = ArrayList<Node>()
}

class ArgumentNode<T>(val referenceName: String, val argumentType: ArgumentType<T>) : BaseNode() {
    var description: String? = null
}

sealed class BaseNode : Node() {

    val executions = ArrayList<ExecuteInfo>()

    fun subCmd(name: String, vararg aliases: String, block: FunctionNode.() -> Unit) {
        cmd(name, *aliases) {
            block(this)
            this@BaseNode.nodes.add(this)
        }
    }

    fun <T> argWithType(referenceName: String, type: ArgumentType<T>, blocK: ArgumentNode<T>.() -> Unit) {
        val arg = ArgumentPreNode(referenceName)
        arg.ifType(type, blocK)

        this@BaseNode.nodes.add(arg)
    }

    fun arg(referenceName: String, block: ArgumentPreNode.() -> Unit) {
        val arg = ArgumentPreNode(referenceName)
        block(arg)
        // TODO: fix
        this@BaseNode.nodes.add(arg)
    }


    fun execute(block: ExecuteInfo) {
        executions.add(block)
    }
}

fun cmd(name: String, vararg aliases: String, block: FunctionNode.() -> Unit = {}): Registerable {
    val functionNode = FunctionNode(name, *aliases)
    block(functionNode)
    return BukkitCommandRegister(functionNode)
}


