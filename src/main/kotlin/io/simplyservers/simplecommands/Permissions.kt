package io.simplyservers.simplecommands

import org.bukkit.command.CommandSender

val isOp = { sender: CommandSender -> sender.isOp }
fun has(permission: String) = { sender: CommandSender -> sender.hasPermission(permission) }
