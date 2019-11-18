package io.simplyservers.simplecommand.core

interface MockPlayer {
    val permissionsSeq: Sequence<String>
    fun sendMessage(message: String)
}

class BaseMockPlayer(vararg permissions: String, private val messageBlocker: (String) -> Unit = {}) : MockPlayer {
    override val permissionsSeq = permissions.asSequence()
    override fun sendMessage(message: String) {
        messageBlocker(message)
    }
}

