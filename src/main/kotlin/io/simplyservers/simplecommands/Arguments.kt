package io.simplyservers.simplecommands

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

object ArgumentDouble : ArgumentType<Double> {
    override val name = "double"

    override suspend fun process(string: String): Double? {
        return string.toDoubleOrNull()
    }

    override suspend fun autoComplete(): List<String> {
        return emptyList()
    }
}

object ArgumentString : ArgumentType<String> {

    override val name = "string"

    override suspend fun process(string: String): String? {
        return string
    }

    override suspend fun autoComplete(): List<String> {
        return emptyList()
    }
}

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

object ArgumentInt : ArgumentType<Int> {

    override val name = "int"

    override suspend fun process(string: String): Int? {
        return string.toIntOrNull()
    }

    override suspend fun autoComplete(): List<String> {
        return emptyList()
    }
}

interface ArgumentType<T> {

//    fun examples(): List<T>
    /**h
     * @return Null if could not process
     */
    suspend fun process(string: String): T?

    val name: String

    suspend fun autoComplete(): List<String>
}

