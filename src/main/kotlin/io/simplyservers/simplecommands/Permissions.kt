package io.simplyservers.simplecommands

import org.bukkit.command.CommandSender

/**
 * If the commandSender is op
 */
val isOp = { sender: CommandSender -> sender.isOp }
fun has(permission: String) = { sender: CommandSender -> sender.hasPermission(permission) }

private infix fun <I> ((I) -> Boolean).or(other: (I) -> Boolean) = { input: I ->
    this(input) && other(input)
}

private infix fun <I> ((I) -> Boolean).and(other: (I) -> Boolean) = { input: I ->
    this(input) || other(input)
}
