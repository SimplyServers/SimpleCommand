package io.simplyservers.simplecommands.bukkit

import io.simplyservers.simplecommands.FunctionNode
import io.simplyservers.simplecommands.Registerable
import io.simplyservers.simplecommands.cmd
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
            val commandExecutor = object : CommandExecutor {
                override fun onCommand(
                    sender: CommandSender,
                    command: Command?,
                    label: String?,
                    args: Array<String>
                ): Boolean {
                    val simpleCommandExecutor = SimpleCommandExecutor(cmd, sender, args)
                    GlobalScope.launch {
                        try {
                            simpleCommandExecutor.run()
                        } catch (e: SimpleCommandExecutor.PermissionException) {
                            sender.sendMessage("You do not have permission")
                        } catch (e: SimpleCommandExecutor.CommandSyntaxException) {
                            sender.sendMessage("Wrong syntax")
                        }
                    }
                    return true
                }
            }

            val pluginCommand = plugin.server.getPluginCommand(name)
            pluginCommand.executor = commandExecutor
            pluginCommand.aliases = aliases.toList()

        }
    }
}
