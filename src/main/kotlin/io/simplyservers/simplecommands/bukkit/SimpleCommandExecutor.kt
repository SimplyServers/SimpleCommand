package io.simplyservers.simplecommands.bukkit

import io.simplyservers.simplecommands.*
import java.util.*

/**
 * executeSender and sender should be same person
 */
class SimpleCommandExecutor<S>(
    private val baseNode: FunctionNode<S>,
    private val executeSender: S,
    val args: Array<String>
) {

    private val argsMap = HashMap<String, Any>()

    suspend fun run()  {
        matchingNode(baseNode, args)
    }

    //    class PermissionException : Throwable("A command was executed permission to do that command")
    class PermissionException : Throwable("A command was executed permission to do that command")

    data class CommandSyntaxException(val node: Node<*>): Throwable()

    private suspend fun matchingNode(nodeOn: Node<S>, currentArgs: Array<String>) {

        if (nodeOn is FunctionNode) {
            val permission = nodeOn.permission
            if (permission != null) {
                val hasPermission = permission(executeSender)
                if (!hasPermission) {
                    throw PermissionException()
                }
            }
        }

        if (nodeOn.nodes.isNotEmpty() && currentArgs.isNotEmpty()) {
            val firstArg = currentArgs.first()
            val nextArgs = currentArgs.sliceArray(1 until currentArgs.size)

            loop@ for (node in nodeOn.nodes) {
                when (node) {
                    is ArgumentPreNode -> {
                        argumentLoop@ for (argumentNode in node.nodes) {
                            when (argumentNode) {
                                is ArgumentNode<S, *> -> {
                                    val argumentType = argumentNode.argumentType
                                    val processed = argumentType.process(firstArg) ?: continue@argumentLoop
                                    argsMap[argumentNode.referenceName] = processed
                                    matchingNode(argumentNode, nextArgs)
                                    return // avoid end execution
                                }
                            }
                        }
                    }
                    is FunctionNode -> {
                        val matches = node.matches(firstArg)
                        if (!matches) continue@loop
                        matchingNode(node, nextArgs)
                        return // avoid end execution
                    }
                }
            }
        }
        when (nodeOn) {
            is BaseNode -> {
                if (nodeOn.executions.isEmpty()) { // We want to tell the player what they can do
                    throw CommandSyntaxException(nodeOn)
                } else {
                    argsMap["additionalArgs"] = currentArgs.toList()

                    nodeOn.executions.forEach { it(executeSender, nodeOn, argsMap) }
                }
            }
            else -> throw IllegalStateException("should never be on a non-base node")
        }


    }

    private fun BaseNode<*>.failMsg(): Nothing {
        TODO()
//        if (this.executions.isEmpty()) { // We want to tell the player what they can do
//            if (this is ArgumentNode<*, *>) {
//                val description = this.description
//                if (description != null) {
//
//                    sender.sendMessage("You entered $description.")
//                }
//            }
//            val helpMessage = nodeOn.nodes.helpMessage()
//            sender.sendMessage(helpMessage)
//            return
//        }
    }

    private fun Collection<Node<S>>.helpMessage() = buildString {

        if (this@helpMessage.isEmpty()) {
            append("You do not have any other options. Report this to an admin")
            return@buildString
        }
        for (node in this@helpMessage) {
            when (node) {
                is ArgumentPreNode -> {
                    append(node.helpMessageString() + "\n")
                }
                is FunctionNode -> {
                    append(node.helpMessageString() + "\n")
                }
            }
        }
    }

    private fun FunctionNode<*>.helpMessageString() = buildString {
        val names = sequenceOf(name, *(aliases)).joinToString(separator = "|")
        append("cmd: [$names]")
        if (description != null) {
            append(" - $description")
        }
    }

    private fun ArgumentPreNode<*>.helpMessageString() = buildString {
        append("arg: $referenceName")

        val argTypes = nodes
            .asSequence()
            .filterIsInstance<ArgumentNode<*, *>>()
            .map {
                it.description
            }
            .filterNotNull()
            .toList()
        if (description != null) {
            append(" - $description")
        }
        if (argTypes.isNotEmpty()) {
            val argTypesEnglish =
                argTypes.sentenceEndingWith("or", prefixPlural = "can either be", prefixSingular = "must be")
            append(" ... $argTypesEnglish")
        }
    }
}
