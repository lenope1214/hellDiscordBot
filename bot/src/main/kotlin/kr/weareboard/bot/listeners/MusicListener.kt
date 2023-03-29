package kr.wearebaord.hellbot.listeners

import kr.wearebaord.hellbot.common.*
import kr.wearebaord.hellbot.domain.PlayerManager
import kr.wearebaord.hellbot.domain.enums.EmojiValue
import kr.wearebaord.hellbot.exception.InvalidTextChannel
import kr.wearebaord.hellbot.listeners.music.PlayCommand
import kr.wearebaord.hellbot.listeners.music.SkipCommand
import kr.wearebaord.hellbot.listeners.music.StopCommand
import kr.wearebaord.hellbot.music.status.getRepeatEmoji
import kr.wearebaord.hellbot.music.status.getRepeatText
import kr.weareboard.main.BOT_VERSION
import kr.weareboard.main.NOTICE_FLAG
import kr.weareboard.main.TEXT_CHANNEL_NAME
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

// 초기 메세지 전달 쓰레드 생성
val messageThread = Thread {
        Thread.sleep(1000)
}

object MusicListener : ListenerAdapter() {
    private val log = LoggerFactory.getLogger(MusicListener::class.java)

    override fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild
        val channels = guild.getTextChannelsByName(TEXT_CHANNEL_NAME, true)!! as List<TextChannel>
        channels.forEach { channel ->
            log.info("채널 ${channel.name}에 메세지를 삭제합니다.")
            channel.deleteAllMessages()
            log.info("채널 ${channel.name}에 메세지를 삭제했습니다.")
        }

        Thread.sleep(500)

        channels.forEach { channel ->
            log.info("채널 ${channel.name}에 메세지를 보냅니다.")
            channel.sendEmbed("${TEXT_CHANNEL_NAME}의 현재 버전은 $BOT_VERSION 입니다.")
            log.info("채널 ${channel.name}에 메세지를 보냈습니다.")
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val contentRaw = event.message.contentRaw
        val member = event.member // request user infomation

        try {
            isValidTextChannel(event.channel) // 불가능한 채널이면 InvalidTextChannel 예외 발생

            if (
                member?.user?.isBot == true ||  // 봇의 메세지는 처리하지 않음
                contentRaw.isNullOrEmpty() &&
                !contentRaw.contains(" ") &&
                !contentRaw.isCorrectPrefix() &&
                contentRaw.length < 3
            ) {
                return
            }
        } catch (e: InvalidTextChannel) {
            return
        }
        when (event.message.contentRaw.parseCommand()) {
//            in PlayCommand.commands
            in NOTICE_FLAG -> {
                // 공지사항은 아무런 처리를 하지 않음.
                return
            }
            in StopCommand.commands -> {
                StopCommand.onAction(event)
            }
            in SkipCommand.commands -> {
                SkipCommand.onAction(event)
            }
            // stop skip 외에는 플레이어 커맨드로 처리
            else -> {
                PlayCommand.onAction(event)
            }
        }
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

                PlayerManager.getInstance().jumpTo(event.channel as TextChannel, values[0])
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
                PlayerManager.getInstance().resume(event.channel as TextChannel)
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
                PlayerManager.getInstance().pause(event.channel as TextChannel)
            }
            "stopButton" -> {
                PlayerManager.getInstance().stop(event.channel as TextChannel, event.member!!.effectiveName)
            }
            "skipButton" -> {
                val isPlayNextTrack = PlayerManager.getInstance().next(event.channel as TextChannel)
            }
            "prevButton" -> {
                PlayerManager.getInstance().prevTrack(event.channel as TextChannel)
            }
            "repeatButton" -> {
                log.info("반복버튼 눌림")
                val beforeEmoji = event.button.emoji
                val isRepeat = beforeEmoji == EmojiValue.INFINITY.fromUnicode()
                try {
                    // 기존 버튼 수정
                    event.editButton(
                        event.component
                            .withLabel(getRepeatText(isRepeat))
                            .withEmoji(getRepeatEmoji(isRepeat))
                    ).queue()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                log.info("process repeat")
                PlayerManager.getInstance().repeat(event.channel as TextChannel)
                log.info("end process repeat")
            }
        }
    }
}
