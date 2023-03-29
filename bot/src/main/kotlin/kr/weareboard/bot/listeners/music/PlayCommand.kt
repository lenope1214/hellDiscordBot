package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.common.isHttpUrl
import kr.wearebaord.hellbot.common.isMemberEnteredChannel
import kr.wearebaord.hellbot.common.isValidTextChannel
import kr.wearebaord.hellbot.common.joinVoiceChannelBot
import kr.wearebaord.hellbot.exception.InvalidTextChannel
import kr.wearebaord.hellbot.domain.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory

object PlayCommand : CommandInterface {
    val log = LoggerFactory.getLogger(PlayCommand::class.java)
    override fun onAction(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        log.info("play command by ${event.member!!.effectiveName}")

        val channel = event.channel
        val bot = event.guild.selfMember // bot infomation
        val member = event.member // request user infomation
        val memberVoiceState = member!!.voiceState

        event.message.delete().queueAfter(1, java.util.concurrent.TimeUnit.SECONDS) // 1초 뒤 메세지 삭제

        log.info("member : $member")
        log.info("bot : $bot")
        if (member.user.isBot)  return // 봇의 메세지는 처리하지 않음

        // 요청자가 음성 채널에 들어가있는가?
        if(!isMemberEnteredChannel(memberVoiceState, channel)) return

        // 사용자가 보낸 메세지 삭제

        play(event, raw)
    }

    override fun onHelp(): String {
        TODO("Not yet implemented")
    }

    private fun play(event: MessageReceivedEvent, url: String) {
        val channel = event.channel

        val bot = event.guild!!.selfMember // bot infomation
        val selfVoiceState = bot!!.voiceState
        var url = url

        log.info("url: $url")
        if (!url.isHttpUrl()) {
            log.info("url is not HTTP, search by ytsearch")
            url = "ytsearch:$url"
        }

        try {
            // 헬파티 봇이 음성 채널에 없다면 음성 채널에 참가시킨다.
            if (!selfVoiceState!!.inAudioChannel()) {
                joinVoiceChannelBot(event.channel, event.member!!, event.guild!!)
            }
            PlayerManager.getInstance()
                .loadAndPlay(channel as TextChannel, url, event.member!!)
        } catch (e: Exception) {
            e.printStackTrace()
            log.error("error: ${e.message}")
        } finally {
        }
    }
}
