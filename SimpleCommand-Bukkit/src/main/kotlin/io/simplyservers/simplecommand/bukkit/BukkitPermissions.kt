package io.simplyservers.simplecommand.bukkit

import io.simplyservers.simplecommand.core.PermissionGetter
import org.bukkit.command.CommandSender

/**
 * If the commandSender is op
 */
val isOp: PermissionGetter<CommandSender> = { sender, _ -> sender.isOp }



