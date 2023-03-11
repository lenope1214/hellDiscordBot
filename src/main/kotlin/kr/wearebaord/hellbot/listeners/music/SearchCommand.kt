package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.common.*
import kr.wearebaord.hellbot.exception.InvalidTextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory

object SearchCommand : CommandInterface {
    val log = LoggerFactory.getLogger(SearchCommand::class.java)

    val commands: List<String> = listOf("search", "sc", "ㄴㄷㅁㄱ초", "ㄴㅊ", "검색", "유튜브검색")

    override fun onAction(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        try {
            isValidTextChannel(event.channel)
        } catch (e: InvalidTextChannel) {
            return
        }
        log.info("play command by ${event.member!!.effectiveName}")

        val channel = event.channel
        val bot = event.guild.selfMember // bot infomation
        val member = event.member // request user infomation
        val memberVoiceState = member!!.voiceState

        if (member == bot) {
            return
        }

        // 요청자가 음성 채널에 들어가있는가?
        !isMemberEnteredChannel(memberVoiceState, channel)

        search(event, raw)
    }

    override fun onHelp(): String {
        TODO("Not yet implemented")
    }


    private fun search(event: MessageReceivedEvent, url: String) {

    }
}
