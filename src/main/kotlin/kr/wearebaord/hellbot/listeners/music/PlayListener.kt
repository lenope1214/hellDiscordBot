package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.PREFIX
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import kr.wearebaord.hellbot.common.isInvalidMessage
import kr.wearebaord.hellbot.common.isYoutubeUrl
import kr.wearebaord.hellbot.common.joinVoiceChannelBot
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

object PlayListener : ListenerAdapter() {
    val log = LoggerFactory.getLogger(PlayListener::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        var args: List<String> = raw.split(" ")

        if (isInvalidMessage(event)) return

        if (args.size < 2) {
            event.channel.sendMessage("사용법: !!p title or link").queue()
            return
        }

        val command: String = args[0]

        when (command) {
            "${PREFIX}p" -> {
                val content: String = args[1]

                val channel = event.channel

                if (args.isEmpty()) {
                    log.info("play command args is empty")
                    channel.sendMessage("사용법: !!p title or link").queue()
                    return
                }

                val member = event.member
                val memberVoiceState = member!!.voiceState
                log.info("member : $member")
                log.info("memberVoiceState: $memberVoiceState")

                if (!memberVoiceState!!.inAudioChannel()) {
                    channel.sendMessage("음성채널에 들어가주세요.").queue()
                    return
                }

                play(event, content)
            }
        }
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