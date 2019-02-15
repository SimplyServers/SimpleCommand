package io.simplyservers.simplecommand.bukkit

import org.bukkit.command.CommandSender

/**
 * If the commandSender is op
 */
val isOp = { sender: CommandSender -> sender.isOp }

fun has(permission: String) = { sender: CommandSender -> sender.hasPermission(permission) }
