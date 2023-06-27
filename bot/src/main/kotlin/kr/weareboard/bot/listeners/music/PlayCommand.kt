package kr.weareboard.bot.listeners.music

import kr.weareboard.bot.common.isHttpUrl
import kr.weareboard.bot.common.isMemberEnteredChannel
import kr.weareboard.bot.domain.PlayerManager
import kr.weareboard.bot.service.interfaces.BotService
import kr.weareboard.bot.service.interfaces.TextChannelService
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PlayCommand(
    private val playerManager: PlayerManager,
    private val botService: BotService,
    private val textChannelService: TextChannelService
) : CommandInterface {
    val log = LoggerFactory.getLogger(PlayCommand::class.java)
    override fun onAction(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        log.info("play command by ${event.member!!.effectiveName}")

        val channel = event.channel
        val guild = event.guild
        val bot = guild.selfMember // bot infomation
        val member = event.member // request user infomation

        event.message.delete().queueAfter(1, java.util.concurrent.TimeUnit.SECONDS) // 1초 뒤 메세지 삭제

        log.info("member : $member")
        log.info("bot : $bot")
        if(member == null) return
        if (member.user.isBot) return // 봇의 메세지는 처리하지 않음

        // 요청자가 음성 채널에 들어가있는가?
        botService.isMemberEnteredChannel(
            guild = guild,
            channel = channel as TextChannel
        )
//        if(!botService.isAlreadyConnectedChannel(guild)){
//            textChannelService.sendEmbed(
//                channel = channel as TextChannel,
//                title = "음성채널에 들어가주세요.",
//                description = "노래를 추가하기 전에 음성채널에 들어가주세요.",
//            )
//        }
        // 사용자가 보낸 메세지 삭제

        play(event, raw)
    }

    override fun onHelp(): String {
        TODO("Not yet implemented")
    }

    private fun play(event: MessageReceivedEvent, url: String) {
        var url = url // url을 대입할 수 있게 함
        val channel = event.channel

        val bot = event.guild.selfMember // bot infomation
        val selfVoiceState = bot.voiceState

        log.info("url: $url")
        val isYoutubeSearch = !url.isHttpUrl()
        if (isYoutubeSearch) {
            log.info("url is not HTTP, search by ytsearch")
            url = "ytsearch:$url"
        }

        try {
            // 헬파티 봇이 음성 채널에 없다면 음성 채널에 참가시킨다.
            if (!selfVoiceState!!.inAudioChannel()) {
                botService.joinVoiceChannelIfNotJoined(event.channel as TextChannel, event.member!!, event.guild)
            }
            playerManager
                .loadAndPlay(
                    guild = event.guild,
                    channel = channel as TextChannel,
                    trackUrl = url,
                    addedBy = event.member!!,
                    isYoutubeSearch = isYoutubeSearch,
                )
        } catch (e: Exception) {
            e.printStackTrace()
            log.error("error: ${e.message}")
        } finally {
        }
    }
}
