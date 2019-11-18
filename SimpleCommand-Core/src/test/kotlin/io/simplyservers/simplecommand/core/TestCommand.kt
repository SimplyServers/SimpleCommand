package io.simplyservers.simplecommand.core

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestCommand {

    private val testCommand by lazy {
        cmd<MockPlayer>("simpletest", "a") {
            basePath = "simpletest.use"
            subCmd("subcmd1") {
                execute { sender, _, _ ->
                    sender.sendMessage("Ran subcmd 1")
                }
            }
            subCmd("subcmd2") {
                execute { sender, _, _ ->
                    sender.sendMessage("Ran subcmd 2")
                }
                // no permission
            }
            subCmd("subcmd3") {
                subCmd("subsubcmd1") {
                    execute { sender, _, _ ->
                        sender.sendMessage("did something")
                    }
                }
                subCmd("subsubcmd2", "sc2") {
                    description = "NOW THIS IS EPIC"
                }
                argWithType("anArg", ArgumentDouble, description = "a really cool arg") {
                    description = "hello there"
                }
            }
            argWithType("yeet", ArgumentString) {
                execute { sender, _, args ->
                    val yeet: String by args
                    sender.sendMessage("YEET: $yeet")
                }
            }
            execute { sender, _, _ ->
                sender.sendMessage("Ran nothing")
            }
        }
    }

    @Test
    fun `test help`() {
        val sender = BaseMockPlayer()
        try {
            runBlocking {
                commandExecutor(testCommand, sender, sender.permissionsSeq, "subcmd3".toArgs())
            }
        } catch (e: CommandSyntaxException) {
            val message = DefaultFormatter<MockPlayer>(sender, sender.permissionsSeq).generateHelpMessage(e).length
            val ideal = 124
            Assertions.assertEquals(ideal, message)
        }
    }

    @Test
    fun `test no perms`() {
        val sender = BaseMockPlayer()
        assertThrows<PermissionException> {
            runBlocking {
                commandExecutor(testCommand, sender, sender.permissionsSeq, "subcmd1 test".toArgs())
            }
        }
        assertThrows<PermissionException> {
            runBlocking {
                commandExecutor(testCommand, sender, sender.permissionsSeq, "subcmd1".toArgs())
            }
        }
    }

    @Test
    fun `test has perms`() {
        val sender = BaseMockPlayer("simpletest.use.subcmd1")
        runBlocking {
            commandExecutor(testCommand, sender, sender.permissionsSeq, "subcmd1 test".toArgs())
            commandExecutor(testCommand, sender, sender.permissionsSeq, "subcmd1".toArgs())
        }
        assertThrows<PermissionException> {
            runBlocking {
                commandExecutor(testCommand, sender, sender.permissionsSeq, "subcmd2".toArgs())
            }
        }
    }
}
