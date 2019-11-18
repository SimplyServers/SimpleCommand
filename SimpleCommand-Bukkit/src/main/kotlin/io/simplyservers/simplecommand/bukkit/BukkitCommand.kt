package io.simplyservers.simplecommand.bukkit

import io.simplyservers.simplecommand.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class PlayerMessageException(message: String?) : Throwable(message)

fun bCmd(name: String, vararg aliases: String, block: FunctionNode<CommandSender>.() -> Unit) =
    cmd(name, *aliases, block = block)


fun FunctionNode<CommandSender>.register(plugin: Plugin, scope: CoroutineScope = GlobalScope) {
    val commandExecutor = CommandExecutor { sender, command, label, args ->
        scope.launch {
            try {
                commandExecutor(this@register, sender, sender::hasPermission, args)
            } catch (e: PermissionException) {
                sender.sendMessage("You do not have permission")
            } catch (e: CommandSyntaxException) {
                sender.sendMessage("Wrong syntax")
//                        sender.sendMessage(Formatter.generateHelpMessage(e).replace("\t", "  "))
            } catch (e: PlayerMessageException) {
                if (e.message != null) sender.sendMessage(e.message)
            }
        }
        true
    }
    val pluginCommand = plugin.server.getPluginCommand(name).apply {
        executor = commandExecutor
        aliases = aliases.toList()
    }
}

fun CommandSender.requirePlayer(message: String? = "Not a player") =
    this as? Player ?: throw PlayerMessageException(message)

fun <T> T?.requireNotNull(message: String? = "") = this ?: throw PlayerMessageException(message)

interface RequireMid<T> {
    fun onFail(block: T.() -> String): T
}
fun <T> T.require(block: T.() -> Boolean): RequireMid<T> {
    val result = block()
    return object: RequireMid<T> {
        override fun onFail(block: (T) -> String): T {
            if(result) return this@require
            throw PlayerMessageException(block(this@require))
        }
    }
}
