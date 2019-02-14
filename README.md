# SimpleCommand
Make commands, simply.

A simple library for easily making complicated commands for those who ❤️ Kotlin. Inspiration from [brigadier](https://github.com/Mojang/brigadier).

## Why use instead of other libraries?
- looks 🔥
- Commands are automatically called asyncronously (with Kotlin coroutines and suspend functions)
  - async arg processing and command execution. Nice for web requests and database access
- Property delegation to process args is ❄️
  - ```kotlin
      val group: Group by args
      val permission: String by args
      val section: String by args
      ```
- the only (clean) solution I could find for making the command below.


## Example
```kotlin

// A custom argument. Note uses suspending functions which are nice if you have to do database requests.
class ArgumentGroup(private val database: SPDatabase) : ArgumentType<Group> { 
    override val name = "group"

    override suspend fun process(string: String): Group? {
        return database.groupByName(string)
    }

    override suspend fun autoComplete(): List<String> {
        return database.groups
            .limit(100)
            .map { it.name }
            .toList()
    }
}

cmd("simplepermissions", "sp") { // The base command
        description = "manage permissions" // The description 
        permission = has("simplepermissions.use") or isOp // The base permission. We implement "or/and" infix functions to combine perms
        subCmd("listgroups", "lg") { // command: "sp lg"
            execute { sender, _, _ ->
                val groups = database.groups.limit(10).toList().joinToString { it.name }
                sender.sendMessage(
                    LangBukkit.listGroups.formatParams(
                        "groups" to groups
                    )
                )
            }
        }
        subCmd("group", "g") { // command: "sp g"
            description = "manage groups"
            arg("group") { // an argument not a sub command... referenced later with "val group: Group by args"
                description = "the group you want to add"
                ifType(ArgumentGroup(database)) {  // This gets called if the argument is an existing group
                    description = "an existing group"
                    subCmd("perms", "p") { // command: "sp g {existing group} p"
                        execute { sender, _, args ->
                            val group: Group by args

                            // send permission message
                        }
                    }
                    subCmd("section", "s") {
                        argWithType("section", ArgumentString) { // a shortcut for if the argument only has one type
                            subCmd("addperm", "ap") {
                                argWithType("permission", ArgumentString) {
                                    execute { sender, _, args ->
                                        val group: Group by args
                                        val permission: String by args
                                        val section: String by args

                                        val permissionSection = group.getSection(section)
                                        permissionSection.permissions[permission] = true
                                        database.saveGroup(group)

                                        sender.sendSuccess(LangBukkit.success)
                                    }
                                }
                            }
                            subCmd("negateperm", "np") {
                                arg("permission") {
                                    ifType(ArgumentString) {
                                        execute { sender, _, args ->
                                            val group: Group by args
                                            val permission: String by args
                                            val section: String by args

                                            val permissionSection = group.getSection(section)

                                            permissionSection.permissions.remove(permission)

                                            database.saveGroup(group)

                                            sender.sendSuccess(LangBukkit.success)
                                        }
                                    }
                                }
                            }
                            subCmd("removeperm", "rp") {
                                arg("permission") {
                                    ifType(ArgumentString) {
                                        execute { sender, _, args ->
                                            val group: Group by args
                                            val permission: String by args
                                            val section: String by args

                                            group.getSection(section).permissions.remove(permission)

                                            sender.sendSuccess(LangBukkit.success)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ifType(ArgumentString) { // This gets called if the argument is NOT existing group (creating a group which did not exist)
                    description = "a non-existing group"
                    subCmd("create", "c") {
                        arg("priority") {
                            ifType(ArgumentInt) {
                                execute { sender, _, args ->
                                    val group: String by args
                                    val priority: Int by args

                                    if (database.groupByName(group) != null) {
                                        LangBukkit.groupAlreadyExists.formatParams(
                                            "group" to group
                                        )
                                        return@execute
                                    }
                                    database.saveGroup(Group(group, priority))

                                    sender.sendSuccess(
                                        LangBukkit.createdGroupSuccess.formatParams(
                                            "group" to group
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        subCmd("player", "p") { 
            description = "manage players"
            argWithType("player", ArgumentOfflinePlayer()) {
                subCmd("addgroup", "ag") {
                    argWithType("group", ArgumentGroup(database)) {
                        execute { sender, _, args ->
                            val group: Group by args
                            val player: OfflinePlayer by args

                            val uuid = player.uniqueId
                            val userByUUID = database.permissionUserByUUID(uuid) ?: PermissionUser(uuid.toString())

                            userByUUID.groups.add(group.name.toId())

                            sender.sendSuccess(LangBukkit.success)
                        }
                    }
                }
                subCmd("removegroup", "rg") {
                    arg("group") {
                        execute { sender, _, args ->
                            val group: Group by args
                            val player: OfflinePlayer by args

                            val uuid = player.uniqueId
                            val permissionUser = database.permissionUserByUUID(uuid)
                            if (permissionUser == null) {
                                sender.sendWarning(LangBukkit.notInGroup)
                                return@execute
                            }

                            if (permissionUser.groups.remove(group.name.toId())) {
                                sender.sendSuccess(LangBukkit.success)
                            } else {
                                sender.sendWarning(LangBukkit.notInGroup)
                            }
                        }
                    }
                }
                subCmd("listgroups", "lg") {
                    execute { sender, _, args ->
                        val player: OfflinePlayer by args

                        val permissionUser = database.permissionUserByUUID(player.uniqueId) ?: return@execute
                        val groupsString = permissionUser.groups
                            .map { database.groupByName(it.toString()) }
                            .joinToString()

                        sender.sendMessage("Groups: $groupsString")
                    }
                }
            }
        }
```
