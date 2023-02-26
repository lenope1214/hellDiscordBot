package kr.wearebaord.hellbot.listeners

import kr.wearebaord.hellbot.BOT_VERSION
import kr.wearebaord.hellbot.TEXT_CHANNEL_NAME
import kr.wearebaord.hellbot.common.deleteAllMessages
import kr.wearebaord.hellbot.common.isCorrectPrefix
import kr.wearebaord.hellbot.common.parseCommand
import kr.wearebaord.hellbot.listeners.music.PlayCommand
import kr.wearebaord.hellbot.listeners.music.SkipCommand
import kr.wearebaord.hellbot.listeners.music.StopCommand
import kr.wearebaord.hellbot.music.PlayerManager
import kr.wearebaord.hellbot.music.enums.EmojiValue
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object MusicListener : ListenerAdapter() {

    override fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild
        val channels = guild.getTextChannelsByName(TEXT_CHANNEL_NAME, true)!! as List<TextChannel>
        channels.forEach {
            it.deleteAllMessages()
            it.sendMessage("봇이 재시작되었습니다. 현재 버전 : $BOT_VERSION").queue()
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val contentRaw = event.message.contentRaw
        if(
            contentRaw.isNullOrEmpty()
            && !contentRaw.contains(" ")
            && !contentRaw.isCorrectPrefix()
            && contentRaw.length < 3
        )return
        when(event.message.contentRaw.parseCommand()) {
            in PlayCommand.commands -> {
                PlayCommand.onAction(event)
            }
            in StopCommand.commands -> {
                StopCommand.onAction(event)
            }
            in SkipCommand.commands -> {
                SkipCommand.onAction(event)
            }
        }
    }

    override fun onGenericSelectMenuInteraction(event: GenericSelectMenuInteractionEvent<*, *>) {
        PlayCommand.log.info("onGenericSelectMenuInteraction - ${event.componentId}")
        PlayCommand.log.info("getComponent : ${event.component}")
        when (event.componentId) {
            "trackBox" -> {
                val values: List<Int> = event.values.map {
                    it as String
                    // 숫자 외 제거
                    it.replace("/[^0-9]/g", "")
                    PlayCommand.log.info("value: ${it.toLong()}")
                    it.toInt()
                }

                PlayerManager.INSTANCE.jumpTo(event.channel as TextChannel, values[0])
            }
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        // logging event info
        PlayCommand.log.info("onButtonInteraction - ${event.componentId}")
//        log.info("${event.interaction}")
        when (event.componentId) {
            "playButton" -> {
                try {
                    // 기존 버튼 수정
                    event.editButton(
                        event.component
                            .withId("pauseButton")
                            .withLabel("일시정지")
                            .withEmoji(EmojiValue.PAUSE.fromUnicode())
                    ).queue()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                PlayerManager.INSTANCE.resume(event.channel as TextChannel)
            }
            "pauseButton" -> {
                try {
                    // 기존 버튼 수정
                    event.editButton(
                        event.component
                            .withId("playButton")
                            .withLabel("재생")
                            .withEmoji(EmojiValue.PLAY.fromUnicode())
                    ).queue()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                PlayerManager.INSTANCE.pause(event.channel as TextChannel)
            }
            "stopButton" -> {
                PlayerManager.INSTANCE.stop(event.channel as TextChannel, event.member!!.effectiveName)
            }
            "skipButton" -> {
                val isPlayNextTrack = PlayerManager.INSTANCE.next(event.channel as TextChannel)
            }
            "prevButton" -> {
                PlayerManager.INSTANCE.prevTrack(event.channel as TextChannel)
            }
            "repeatButton" -> {
                PlayCommand.log.info("반복버튼 눌림")
                val beforeEmoji = event.button.emoji
                PlayCommand.log.info("beforeEmoji: $beforeEmoji")
                val isRepeat = beforeEmoji == EmojiValue.INFINITY.fromUnicode()
                PlayCommand.log.info("isRepeat: $isRepeat")
                try {
                    // 기존 버튼 수정
                    event.editButton(
                        event.component
                            .withLabel(if (isRepeat) "반복해제" else "반복")
                            .withEmoji(if (isRepeat) EmojiValue.EXIT.fromUnicode() else EmojiValue.INFINITY.fromUnicode())
                    ).queue()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                PlayCommand.log.info("process repeat")
                PlayerManager.INSTANCE.repeat(event.channel as TextChannel)
                PlayCommand.log.info("end process repeat")
            }
        }
    }
}