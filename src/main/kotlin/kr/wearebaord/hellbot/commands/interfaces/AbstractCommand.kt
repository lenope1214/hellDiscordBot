package kr.wearebaord.hellbot.commands.interfaces

import io.github.jdiscordbots.command_framework.command.CommandEvent
import io.github.jdiscordbots.command_framework.command.ICommand
import net.dv8tion.jda.api.Permission

abstract class AbstractCommand : ICommand {
    override fun isAvailableToEveryone(): Boolean {
        return false
    }

    override fun getRequiredPermissions(): Set<Permission> {
        return setOf(Permission.MANAGE_ROLES)
    }

    override fun allowExecute(event: CommandEvent): Boolean {
        for (perm in requiredPermissions) {
            if (event.member.hasPermission(perm)) {
                return true
            }
        }
        return false
    }
}