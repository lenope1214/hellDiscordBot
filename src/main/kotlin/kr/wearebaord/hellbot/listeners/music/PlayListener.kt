package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.PREFIX
import kr.wearebaord.hellbot.makeMessage
import kr.wearebaord.hellbot.utils.KoreanUtil
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory
import kr.wearebaord.hellbot.common.isInvalidMessage
import kr.wearebaord.hellbot.common.joinVoiceChannelBot
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import java.net.URI
import java.net.URISyntaxException

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
        val content: String = args[1]


        when (command) {
            PREFIX + "play" -> {
                event.channel.sendMessage("play").queue()
            }
        }
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

    private fun play(event: MessageReceivedEvent, url: String) {
        val channel = event.channel
        // bot infomation
        val self = event.guild!!.selfMember
        val selfVoiceState = self!!.voiceState
        var url = url

        log.info("url: $url")
        if (!isUrl(url)) {
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


    private fun isUrl(url: String): Boolean {
        // check if url is valid
        try {
            // is url
            URI(url)
            // check start with https://www.youtube.com/watch?v=
            log.info("url.startsWith(\"https://www.youtube.com/watch?v=\") : ${url.startsWith("https://www.youtube.com/watch?v=")}")
            if (!url.startsWith("https://www.youtube.com/watch?v=")) {
                throw URISyntaxException(url, "url is not youtube url")
            }

            return true
        } catch (e: URISyntaxException) {
            log.error("Uri is not valid! ${e.message}")
            return false
        }
    }
}