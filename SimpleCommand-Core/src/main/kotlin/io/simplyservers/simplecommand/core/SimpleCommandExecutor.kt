package io.simplyservers.simplecommand.core

import java.util.*


class PermissionException : Throwable("A command was executed permission to do that command")

interface Formatter {
    fun generateHelpMessage(commandSyntaxException: CommandSyntaxException): String
    companion object Default : Formatter {

        private fun FunctionNode<*>.helpMessageString(builder: StringBuilder) {
            val equivalent = sequenceOf(name, *(aliases)).toList()
            if(equivalent.size == 1) builder.append("\t${equivalent.first()}")
            else {
                val names = equivalent.joinToString(separator = "|")
                builder.append("\t[$names]")
            }
            if (description != null) builder.append(" - $description")
            builder.appendln()
        }

        private fun ArgumentPreNode<*>.helpMessageString(builder: StringBuilder) {
            builder.append("\t<$referenceName>")
            if(description != null) builder.append(" - $description")
            builder.appendln()

            children
                .asSequence()
                .filterIsInstance<ArgumentNode<*, *>>()
                .forEach { node ->
                    val argumentType = node.argumentType
                    builder.append("\t\t:${argumentType.name}")
                    if (node.description != null) builder.append(" - ${node.description}")
                    builder.appendln()
                }
        }

        override fun generateHelpMessage(commandSyntaxException: CommandSyntaxException): String {
            val node = commandSyntaxException.node
            val subCommands = LinkedList<FunctionNode<*>>()
            val argumentPreNodes = LinkedList<ArgumentPreNode<*>>()

            node.children.forEach {
                when (it) {
                    is FunctionNode<*> -> subCommands.add(it)
                    is ArgumentPreNode<*> -> argumentPreNodes.add(it)
                }
            }

            return buildString {
                if (node.children.isEmpty()) append("You do not have any other options. Report this to an admin")
                if (subCommands.isNotEmpty()) {
                    appendln("Subcommands")
                    subCommands.forEach {
                        it.helpMessageString(this)
                    }
                }
                argumentPreNodes.forEach {
                    appendln("Arguments")
                    it.helpMessageString(this)
                }
            }
        }


    }
}

data class CommandSyntaxException(val node: BaseNode<*>) : Throwable()

fun String.toArgs() = trim().split(" ").toTypedArray()

suspend fun <S> commandExecutor(baseNode: FunctionNode<S>, executeSender: S, args: Array<String>) {
    val argsMap = HashMap<String, Any>()

    suspend fun matchingNode(nodeOn: Node<S>, currentArgs: Array<String>) {

        if (nodeOn is FunctionNode) {
            val permissionGetter = nodeOn.permission
            if (permissionGetter != null) {
                val hasPermission = permissionGetter(executeSender)
                if (!hasPermission) {
                    throw PermissionException()
                }
            }
        }

        if (nodeOn.children.isNotEmpty() && currentArgs.isNotEmpty()) {
            val firstArg = currentArgs.first()
            val nextArgs = currentArgs.sliceArray(1 until currentArgs.size)

            loop@ for (node in nodeOn.children) {
                when (node) {
                    is ArgumentPreNode -> {
                        argumentLoop@ for (argumentNode in node.children) {
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

    matchingNode(baseNode, args)

}

