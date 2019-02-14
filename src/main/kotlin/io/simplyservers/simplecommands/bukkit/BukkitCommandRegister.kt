package io.simplyservers.simplecommands.bukkit

import io.simplyservers.simplecommands.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class BukkitCommandRegister(private val functionNode: FunctionNode) : Registerable {

    override fun register(plugin: Plugin) {

        functionNode.subCmd("help") {
            execute { sender, node, args ->
                val additionalArgs: List<String> by args
                val finalNode = additionalArgs.fold(functionNode as? BaseNode) { acc, s -> acc?.findNode(s) }
                if (finalNode == null) {
                    sender.sendMessage("invalid help message")
                    return@execute
                }
                val argsSpaced = additionalArgs.joinToString(" ")
                finalNode.helpMsg("/${functionNode.name} help $argsSpaced", sender)
            }
        }

        val commandExecutor = CommandExecutor { sender, command, name, args ->
            GlobalScope.launch {
                BukkitCommandExecutor(functionNode, sender, args).run()
            }
            return@CommandExecutor true
        }
        plugin.server.getPluginCommand(functionNode.name).apply {
            executor = commandExecutor
            aliases = functionNode.aliases.toList()
        }
    }

    private fun BaseNode.helpMsg(path: String, commandSender: CommandSender) {

        val nodes = this.nodes

        val subCommands = nodes.filterIsInstance<FunctionNode>()
        val argNodeBase = nodes.filterIsInstance<ArgumentPreNode>().firstOrNull()

        val component = ComponentBuilder("HELP ... $path")

        if (subCommands.isNotEmpty()) {
            component.append("\nSUBCOMMANDS").color(ChatColor.BLUE).bold(true)
            for (subCommand in subCommands) {
                val mainName = subCommand.name
                component.append("\n" + mainName).color(ChatColor.WHITE).bold(false)

                val description = subCommand.description

                val hover = ComponentBuilder("CLICK FOR MORE INFORMATION").color(ChatColor.RED).bold(true)

                val aliases = subCommand.aliases
                if (aliases.isNotEmpty()) {
                    hover.append("\naliases: ${aliases.joinToString(prefix = "{", postfix = "}")}").color(ChatColor.WHITE)
                        .bold(false)
                }
                description?.also { hover.append("\ndescription: $it").color(ChatColor.WHITE).bold(false) }

                component.event(HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()))
                component.event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "$path $mainName"))
            }
        }
        if (argNodeBase != null) {
            component.color(ChatColor.BLUE).bold(true).append("\nARGUMENT ").append(argNodeBase.referenceName)
                .color(ChatColor.WHITE).bold(false)
            argNodeBase.description?.also { component.append("\n" + it).color(ChatColor.WHITE).bold(false) }

            component.append("\n")

            argNodeBase
                .nodes
                .asSequence()
                .filterIsInstance<ArgumentNode<*>>()
                .forEach {
                    component.append("\n" + it.argumentType.name)

                    val hover = ComponentBuilder(it.description).color(ChatColor.RED).bold(true)
                    if (it.description != null) component.event(HoverEvent(HoverEvent.Action.SHOW_TEXT, hover.create()))
                    component.event(ClickEvent(ClickEvent.Action.RUN_COMMAND, "$path type:${it.argumentType.name}"))
                }
        }

        component.append("\n")

        when (commandSender) {
            is Player -> commandSender.spigot().sendMessage(*component.create())
        }
    }

    private fun BaseNode.findNode(name: String): BaseNode? {
        val split = name.split("type:")
        if (split.size == 2) {
            val typeName = split[1]
            val arg = this.nodes.filterIsInstance<ArgumentPreNode>().firstOrNull() ?: return null
            return arg
                .nodes
                .asSequence()
                .filterIsInstance<ArgumentNode<*>>()
                .filter { it.argumentType.name == typeName }
                .firstOrNull()
        }
        return this
            .nodes
            .filterIsInstance<FunctionNode>().firstOrNull { it.matches(name) }
    }
}
