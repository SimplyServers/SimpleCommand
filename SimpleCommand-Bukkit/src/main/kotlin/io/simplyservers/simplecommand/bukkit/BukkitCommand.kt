package io.simplyservers.simplecommand.bukkit

import io.simplyservers.simplecommand.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class PlayerMessageException(message: String?) : Throwable(message)

fun bCmd(name: String, vararg aliases: String, block: FunctionNode<CommandSender>.() -> Unit) =
    cmd(name, *aliases, block = block)


fun listPerms(sender: CommandSender): Sequence<String> {
    val effectivePermissions = sender.effectivePermissions
    return effectivePermissions.asSequence()
        .filter { it.value }
        .map { it.permission }
}

fun FunctionNode<CommandSender>.register(plugin: Plugin, scope: CoroutineScope = GlobalScope) {
    val commandExecutor = CommandExecutor { sender, command, label, args ->
        scope.launch {
            try {
                commandExecutor(this@register, sender, listPerms(sender), args)
            } catch (e: PermissionException) {
                sender.sendMessage("You do not have permission")
            } catch (e: CommandSyntaxException) {
                sender.sendMessage("Wrong syntax")
                val message = DefaultFormatter<CommandSender>(sender, listPerms(sender))
                    .generateHelpMessage(e)
                    .replace("\t", "  ")
                sender.sendMessage(message)
            } catch (e: PlayerMessageException) {
                if (e.message != null) sender.sendMessage(e.message)
            }
        }
        true
    }
    plugin.server.getPluginCommand(name)?.apply {
        executor = commandExecutor
        aliases = aliases.toList()
    } ?: throw IllegalArgumentException("The command $name must be registered in your plugin.yml")
}

fun CommandSender.requirePlayer(message: String? = "Not a player") =
    this as? Player ?: throw PlayerMessageException(message)

fun <T> T?.requireNotNull(message: String? = "") = this ?: throw PlayerMessageException(message)

interface RequireMid<T> {
    fun onFail(block: T.() -> String): T
}

fun <T> T.require(block: T.() -> Boolean): RequireMid<T> {
    val result = block()
    return object : RequireMid<T> {
        override fun onFail(block: (T) -> String): T {
            if (result) return this@require
            throw PlayerMessageException(block(this@require))
        }
    }
}
