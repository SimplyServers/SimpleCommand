package io.simplyservers.simplecommand.test

interface MockPlayer {
    fun hasPermission(permission: String): Boolean
    fun sendMessage(message: String)
}

class BaseMockPlayer(private vararg val permissions: String, private val messageBlocker: (String) -> Unit = {}) : MockPlayer {
    override fun sendMessage(message: String) {
        messageBlocker(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return permissions.contains(permission)
    }
}

fun has(permission: String) = { sender: MockPlayer -> sender.hasPermission(permission) }
