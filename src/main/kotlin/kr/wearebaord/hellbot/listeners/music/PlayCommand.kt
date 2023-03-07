package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.common.*
import kr.wearebaord.hellbot.exception.InvalidTextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

object PlayCommand: CommandInterface {
    val log = LoggerFactory.getLogger(PlayCommand::class.java)

    override fun onAction(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        try{
            isValidTextChannel(event.channel)
        }catch (e: InvalidTextChannel){
            return
        }
        log.info("play command by ${event.member!!.effectiveName}")

        val channel = event.channel
        val bot = event.guild.selfMember // bot infomation
        val member = event.member // request user infomation
        val memberVoiceState = member!!.voiceState

        if(member == bot){ return }

        if (!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("음성채널에 들어가주세요.").queue()
            return
        }

        play(event, raw)
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
            PlayerManager.INSTANCE
                .loadAndPlay(channel as TextChannel, url, event.member!!)
        } catch (e: Exception) {
            e.printStackTrace()
            log.error("error: ${e.message}")
        } finally {
            // delete command message
            //  event.message.delete().queue()
        }
    }
}