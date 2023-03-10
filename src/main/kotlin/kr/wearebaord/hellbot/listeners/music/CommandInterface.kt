package kr.wearebaord.hellbot.listeners.music

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface CommandInterface {
    fun onAction(event: MessageReceivedEvent)

    fun onHelp(): String
}