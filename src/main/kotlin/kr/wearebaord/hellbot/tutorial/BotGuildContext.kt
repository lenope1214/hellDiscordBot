package kr.wearebaord.hellbot.tutorial

import kr.wearebaord.hellbot.tutorial.controller.BotController

class BotGuildContext(val guildId: Long) {
    val controllers: MutableMap<Class<out BotController?>, BotController?>

    init {
        controllers = HashMap()
    }
}