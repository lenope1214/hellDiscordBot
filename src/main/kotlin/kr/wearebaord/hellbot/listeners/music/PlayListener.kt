package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.PREFIX
import kr.wearebaord.hellbot.common.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

object PlayListener : ListenerAdapter() {
    val log = LoggerFactory.getLogger(PlayListener::class.java)

    private val commands: List<String> = listOf("p", "play", "ㅔ", "ㅔㅣ묘", "재생", "노래",)
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        val command = parseCommand(raw)
        val content = parseContent(raw)
        if(isInvalidMessage(event)) return
        if (!commands.contains(command)) return
        println("play command")

        val channel = event.channel
        val member = event.member
        val memberVoiceState = member!!.voiceState

        if (!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("음성채널에 들어가주세요.").queue()
            return
        }


        play(event, content)
    }

    private fun play(event: MessageReceivedEvent, url: String) {
        val channel = event.channel
        // bot infomation
        val self = event.guild!!.selfMember
        val selfVoiceState = self!!.voiceState
        var url = url

        log.info("url: $url")
        if (!url.isYoutubeUrl()) {
            log.info("url is not url, ytsearch")
            url = "ytsearch:$url"
        }

        try {
            if (!selfVoiceState!!.inAudioChannel()) {
                joinVoiceChannelBot(event.channel, event.member!!, event.guild!!)
            }
            PlayerManager.INSTANCE
                .loadAndPlay(channel as TextChannel, url)
        } catch (e: Exception) {
            e.printStackTrace()
            log.error("error: ${e.message}")
        } finally {
            // delete command message
            event.message.delete().queue()
        }
    }
}