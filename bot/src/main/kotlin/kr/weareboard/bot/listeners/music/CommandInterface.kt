package kr.wearebaord.hellbot.listeners.music

import kr.weareboard.main.NOTICE_FLAG
import net.dv8tion.jda.api.events.message.MessageReceivedEvent

interface CommandInterface {
    fun onAction(event: MessageReceivedEvent)

    fun onHelp(): String

    fun isNoticeMessage(raw: String): Boolean = raw.startsWith(NOTICE_FLAG)
}
