package io.simplyservers.simplecommand.test

import io.simplyservers.simplecommand.core.ArgumentString
import io.simplyservers.simplecommand.core.SimpleCommandExecutor
import io.simplyservers.simplecommand.core.cmd
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TestCommand {

    private val cmd1 by lazy {
        cmd<MockPlayer>("simpletest", "a") {
            subCmd("subcmd1") {
                permission = has("simpletest.use.subcmd")
                execute { sender, _, args ->
                    sender.sendMessage("Ran subcmd 1")
                }
            }
            subCmd("subcmd2") {
                execute { sender, _, args ->
                    sender.sendMessage("Ran subcmd 2")
                }
                // no permission
            }
            argWithType("yeet", ArgumentString) {
                execute { sender, _, args ->
                    val yeet: String by args
                    sender.sendMessage("YEET: $yeet")
                }
            }
            execute { sender, _, args ->
                sender.sendMessage("Ran nothing")
            }
        }
    }

    @Test
    fun `test subcmd`(){
        // TODO
//        val sender = BaseMockPlayer()
    }


    @Test
    fun `test no perms`() {
        val sender = BaseMockPlayer()
        assertThrows<SimpleCommandExecutor.PermissionException> {
            runBlocking {
                SimpleCommandExecutor(cmd1, BaseMockPlayer {

                }, "subcmd1 test").run()
            }
        }

        assertThrows<SimpleCommandExecutor.PermissionException> {
            runBlocking {
                SimpleCommandExecutor(cmd1, sender, "subcmd1").run()
            }
        }
    }

    @Test
    fun `test has perms`() {
        val sender = BaseMockPlayer("simpletest.use.subcmd")
        runBlocking {
            SimpleCommandExecutor(cmd1, sender, "subcmd1 test").run()
            SimpleCommandExecutor(cmd1, sender, "subcmd1").run()
        }
    }
}
