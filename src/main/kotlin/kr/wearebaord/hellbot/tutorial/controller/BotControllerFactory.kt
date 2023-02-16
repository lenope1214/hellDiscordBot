package kr.wearebaord.hellbot.tutorial.controller

import kr.wearebaord.hellbot.tutorial.BotApplicationManager
import kr.wearebaord.hellbot.tutorial.BotGuildContext
import net.dv8tion.jda.api.entities.Guild

interface BotControllerFactory<T : BotController?> {
    val controllerClass: Class<T>?
    fun create(manager: BotApplicationManager?, state: BotGuildContext?, guild: Guild?): T
}