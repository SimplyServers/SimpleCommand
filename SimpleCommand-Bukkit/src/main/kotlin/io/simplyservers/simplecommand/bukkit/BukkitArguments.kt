package io.simplyservers.simplecommand.bukkit

import io.simplyservers.simplecommand.core.ArgumentType
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class ArgumentOnlinePlayer(private val plugin: Plugin) : ArgumentType<Player> {
    override suspend fun process(string: String): Player? {
        return plugin.server.getPlayer(string)
    }

    override suspend fun autoComplete(): List<String> {
        return plugin.server.onlinePlayers.map { it.name }
    }

    override val name: String get() = "online player"
}

class ArgumentOfflinePlayer(private val plugin: Plugin) :
    ArgumentType<OfflinePlayer> {

    override suspend fun autoComplete(): List<String> {
        TODO("not implemented")
    }

    override val name: String get() = TODO()

    override suspend fun process(string: String): OfflinePlayer? {

        val uuid = try {
            UUID.fromString(string)
        } catch (e: IllegalArgumentException) {
            null
        }

        return if (uuid == null) {
            plugin.server.getOfflinePlayer(string)
        } else {
            plugin.server.getOfflinePlayer(uuid)
        }
    }
}
