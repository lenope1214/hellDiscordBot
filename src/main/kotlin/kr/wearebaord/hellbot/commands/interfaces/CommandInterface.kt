package kr.wearebaord.hellbot.commands.interfaces

import io.github.jdiscordbots.command_framework.command.CommandEvent

interface CommandInterface {
    fun action(event: CommandEvent): Unit
    fun help(): String
    fun allowExecute(event: CommandEvent): Boolean = true
}