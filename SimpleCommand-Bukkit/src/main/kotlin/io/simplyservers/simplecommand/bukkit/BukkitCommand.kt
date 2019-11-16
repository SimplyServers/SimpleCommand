package io.simplyservers.simplecommand.bukkit
import io.simplyservers.simplecommand.core.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin

// fun <S> cmd(name: String, vararg aliases: String, block: FunctionNode<S>.() -> Unit = {}): Registerable {
fun bukkitCommand(
    name: String,
    plugin: Plugin,
    vararg aliases: String,
    block: FunctionNode<CommandSender>.() -> Unit = {}
): Registerable {
    val cmd = cmd(name, *aliases, block = block)
    return object : Registerable {
        override fun register() {
            val commandExecutor = CommandExecutor { sender, command, label, args ->
                GlobalScope.launch {
                    try {
                        commandExecutor(cmd, sender, args)
                    } catch (e: PermissionException) {
                        sender.sendMessage("You do not have permission")
                    } catch (e: CommandSyntaxException) {
                        sender.sendMessage("Wrong syntax")
                        sender.sendMessage(Formatter.generateHelpMessage(e))
                    }
                }
                true
            }

            val pluginCommand = plugin.server.getPluginCommand(name)
            pluginCommand.executor = commandExecutor
            pluginCommand.aliases = aliases.toList()

        }
    }
}
