package kr.weareboard.bot.listeners

import kr.weareboard.bot.common.isCorrectPrefix
import kr.weareboard.bot.common.isValidTextChannel
import kr.weareboard.bot.common.parseCommand
import kr.weareboard.bot.domain.PlayerManager
import kr.weareboard.bot.domain.enums.EmojiValue
import kr.weareboard.bot.exception.InvalidTextChannel
import kr.weareboard.bot.listeners.music.GambleCommand
import kr.weareboard.bot.listeners.music.PlayCommand
import kr.weareboard.bot.listeners.music.SkipCommand
import kr.weareboard.bot.listeners.music.StopCommand
import kr.weareboard.bot.music.status.getRepeatEmoji
import kr.weareboard.bot.music.status.getRepeatText
import kr.weareboard.bot.service.interfaces.TextChannelService
import kr.weareboard.bot.utils.KoreanUtil
import kr.weareboard.main.BOT_ID
import kr.weareboard.main.NOTICE_FLAG
import kr.weareboard.main.TEXT_CHANNEL_NAME
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class DefaultListener(
    private val playerManager: PlayerManager,
    private val playCommand: PlayCommand,
    private val skipCommand: SkipCommand,
    private val stopCommand: StopCommand,
    private val gambleCommand: GambleCommand,
    private val textChannelService: TextChannelService,
) : ListenerAdapter() {
    val log = LoggerFactory.getLogger(DefaultListener::class.java)


    override fun onGuildReady(event: GuildReadyEvent) {
        val guild = event.guild
        val channels = guild.getTextChannelsByName(TEXT_CHANNEL_NAME, true) as List<TextChannel>
        channels.forEach { channel ->
            log.info("채널 ${channel.name}에 메세지를 삭제합니다.")
            textChannelService.deleteAllMessages(channel)
            log.info("채널 ${channel.name}에 메세지를 삭제했습니다.")
        }

        Thread.sleep(500)

        channels.forEach { channel ->
            log.info("채널 ${channel.name}에 메세지를 보냅니다.")
            textChannelService.sendFirstMessage(channel = channel)
            log.info("채널 ${channel.name}에 메세지를 보냈습니다.")
        }
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        // 멤버가 나갔을 때 취할 행동
        // 해당 음성 채널에 모두가 나갔다면 메세지를 남기고 leave
//        log.info("==============멤버가 들어오거나 나갔음===============")
        // 어떠한 멤버가 들어왔거나 나갔을 때,
        val channelLeft = event.channelLeft // 나갔다면 null이 아님,
//        val channelJoined = event.channelJoined // 들어왔다면 null이 아님

        // 나갔다면
        if (channelLeft != null) {
            val leftChannel = channelLeft.asVoiceChannel()
            val leftMemberSize = leftChannel.members.size
            if (leftMemberSize == 1) {
                val leftMember = leftChannel.members[0]

                // 만약 헬파티봇이 혼자 남아있다면 내쫓는다.
                if (leftMember.id == BOT_ID) {
                    val textChannel = leftChannel.guild.textChannels.find {
                        it.name == TEXT_CHANNEL_NAME
                    }
                    if (textChannel != null) {
                        playerManager.leftChannel(
                            guild = leftChannel.guild,
                            channel = textChannel
                        )
                    }
                }
            }
        }
    }


    override fun onMessageReceived(event: MessageReceivedEvent) {
        val contentRaw = event.message.contentRaw
        val member = event.member // request user infomation

        try {
            isValidTextChannel(event.channel) // 불가능한 채널이면 InvalidTextChannel 예외 발생

            if (
                member?.user?.isBot == true || // 봇의 메세지는 처리하지 않음
                contentRaw.isEmpty() &&
                !contentRaw.contains(" ") &&
                !contentRaw.isCorrectPrefix() &&
                contentRaw.length < 3
            ) {
                return
            }
        } catch (e: InvalidTextChannel) {
            return
        }
        var command = event.message.contentRaw.parseCommand()
        if (command == "") {
            command = "playcommand"
        }
        when (command) {
            in NOTICE_FLAG -> {
                // 공지사항은 아무런 처리를 하지 않음.
                return
            }

            in stopCommand.commands -> {
                stopCommand.onAction(event)
            }

            in skipCommand.commands -> {
                skipCommand.onAction(event)
            }

            in gambleCommand.commands -> {
                gambleCommand.onAction(event)
            }
            // stop skip 외에는 플레이어 커맨드로 처리
            else -> {
                log.info("play 커맨드로 넘어옴")
                playCommand.onAction(event)
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

                playerManager.jumpTo(
                    event.channel as TextChannel,
                    values[0],
                    event.member!!
                )
            }
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        // logging event info
        log.info("onButtonInteraction - ${event.componentId}")
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
                playerManager.resume(event.channel as TextChannel)
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
                playerManager.pause(event.channel as TextChannel)
            }

            "stopButton" -> {
                playerManager.stop(event.channel as TextChannel, event.member!!)
            }

            "skipButton" -> {
//                val isPlayNextTrack =
                playerManager.next(event.channel as TextChannel, event.member!!)
            }

            "prevButton" -> {
                playerManager.prevTrack(event.channel as TextChannel)
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
                playerManager.repeat(event.channel as TextChannel)
                log.info("end process repeat")
            }
        }
    }
}
