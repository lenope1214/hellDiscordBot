package kr.wearebaord.hellbot.listeners.music

import dev.minn.jda.ktx.interactions.components.button
import kr.wearebaord.hellbot.BOT_VERSION
import kr.wearebaord.hellbot.PREFIX
import kr.wearebaord.hellbot.TEXT_CHANNEL_NAME
import kr.wearebaord.hellbot.common.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import kr.wearebaord.hellbot.music.PlayerManager
import kr.wearebaord.hellbot.music.enums.EmojiValue
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent

object PlayListener : ListenerAdapter() {
    val log = LoggerFactory.getLogger(PlayListener::class.java)

    private val commands: List<String> = listOf("p", "play", "ㅔ", "ㅔㅣ묘", "재생", "노래")

    override fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild
        val channels = guild.getTextChannelsByName(TEXT_CHANNEL_NAME, true)!! as List<TextChannel>
        channels.forEach {
            it.deleteAllMessages()
            it.sendMessage("봇이 재시작되었습니다. 현재 버전 : ${BOT_VERSION}").queue()
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        val command = try {
            parseCommand(raw)
        } catch (e: IllegalArgumentException) {
            return
        }
        if (command.isNullOrBlank()) return
        val content = parseContent(raw)

        // 아래 두 개는 한 쌍
        if (doNotProcessMessage(command, commands)) return
        log.info("isInvalidMessage(event) : ${isInvalidMessage(event)}")
        if (isInvalidMessage(event)) {
            event.message.delete().queue()
            return
        }
        log.info("play command by ${event.member!!.effectiveName}")

        val channel = event.channel
        val member = event.member
        val memberVoiceState = member!!.voiceState

        if (!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("음성채널에 들어가주세요.").queue()
            return
        }


        play(event, content)
    }

    override fun onGenericSelectMenuInteraction(event: GenericSelectMenuInteractionEvent<*, *>) {
        log.info("onGenericSelectMenuInteraction - ${event.componentId}")
        log.info("getComponent : ${event.component}")
        when (event.componentId) {
            "trackBox" -> {
                val values: List<Int> = event.values.map {
                    it as String
                    // 숫자 외 제거
                    it.replace("/[^0-9]/g", "")
                    log.info("value: ${it.toLong()}")
                    it.toInt()
                }

                PlayerManager.INSTANCE.jumpTo(event.channel as TextChannel, values[0])
            }
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        // logging event info
        log.info("onButtonInteraction - ${event.componentId}")
//        log.info("${event.interaction}")
        when (event.componentId) {
            "playButton" -> {
                event.editMessage("⏸").queue()
                PlayerManager.INSTANCE.resume(event.channel as TextChannel)
            }
            "pauseButton" -> {
                event.editMessage("▶").queue()
                PlayerManager.INSTANCE.pause(event.channel as TextChannel)
            }
            "stopButton" -> {
                PlayerManager.INSTANCE.stop(event.channel as TextChannel, event.member!!.effectiveName)
            }
            "skipButton" -> {
                PlayerManager.INSTANCE.next(event.channel as TextChannel)
            }
            "prevButton" -> {
                PlayerManager.INSTANCE.prevTrack(event.channel as TextChannel)
            }
            "repeatButton" -> {
                log.info("반복버튼 눌림")
                val beforeEmoji = event.button.emoji
                log.info("beforeEmoji: $beforeEmoji")
                val isRepeat = beforeEmoji == EmojiValue.INFINITY.fromUnicode()
                log.info("isRepeat: $isRepeat")
                try {
                    event.editButton(event.component.withEmoji(if (isRepeat) EmojiValue.EXIT.fromUnicode() else EmojiValue.INFINITY.fromUnicode())).queue()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                log.info("process repeat")
                PlayerManager.INSTANCE.repeat(event.channel as TextChannel)
                log.info("end process repeat")
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
            //  event.message.delete().queue()
        }
    }
}