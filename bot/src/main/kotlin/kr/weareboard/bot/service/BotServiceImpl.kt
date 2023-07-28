package kr.weareboard.bot.service

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kr.weareboard.bot.service.interfaces.BotService
import kr.weareboard.bot.service.interfaces.TextChannelService
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.managers.AudioManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class BotServiceImpl(
    private val textChannelService: TextChannelService
) : BotService {
    private val log = LoggerFactory.getLogger(BotServiceImpl::class.java)

    override fun play(track: AudioTrack): Boolean {
        TODO("Not yet implemented")
    }

    override fun joinVoiceChannelIfNotJoined(channel: TextChannel, member: Member, guild: Guild): Boolean {
        val selfVoiceState = member!!.voiceState
        println("selfVoiceState = $selfVoiceState")

        // 요청자가 음성 채널에 들어가있는가?
        if (!isMemberEnteredChannel(guild, channel)) return false

        // 봇이 이미 음성채널에 들어가있는가?
        if (isAlreadyConnectedChannel(guild)) return true

        // 요청자가 음성채널에 연결할 권한이 있는가?
        // if (!isAbleToConnectVoiceChannel(member, channel)) return false

        // 요청자가 음성채널에서 말할 권한이 있는가?
        // if (!isAbleToSpeakVoice(member, channel)) return false

        val audioManager: AudioManager = guild.audioManager

        // selfVoiceState는 CacheFlag에 VOICE_STATE가 포함되어 있어야 한다
        val voiceChannel = selfVoiceState!!.channel

        // 봇이 음성 채널에 들어가도록 함
        audioManager.openAudioConnection(voiceChannel!!)
        return true
    }

    override fun isAlreadyConnectedChannel(guild: Guild): Boolean {
        log.info("guild.selfMember = ${guild.selfMember}")
        return guild.selfMember.voiceState!!.inAudioChannel()
    }

    private fun isAbleToSpeakVoice(
        member: Member,
        channel: MessageChannel
    ): Boolean {
        if (!member.hasPermission(Permission.VOICE_SPEAK)) {
            channel.sendMessage("음성채널에서 말할 권한이 없습니다.").queue()
            return false
        }
        return true
    }

    private fun isAbleToConnectVoiceChannel(
        member: Member,
        channel: MessageChannel
    ): Boolean {
        if (!member.hasPermission(Permission.VOICE_CONNECT)) {
            channel.sendMessage("음성채널에 연결할 권한이 없습니다.").queue()
            return false
        }
        return true
    }

    override fun isMemberEnteredChannel(
        guild: Guild,
        channel: TextChannel
    ): Boolean {
        if (isAlreadyConnectedChannel(guild)) {
            textChannelService.sendPleaseEnterVoiceChannel(channel)
//            channel.sendMessage("음성채널에 들어가주세요.").queue()
            return false
        }
        return true
    }

    override fun leaveBot(guild: Guild, channel: TextChannel, member: Member?): Boolean {
        // 봇이 음성채널에 있으면서 같은 음성채널이라면, 음성채널에서 나간다.
        val audioManager = guild.audioManager

        // 음성채널에 봇이 존재하는지 확인
        if (!audioManager.isConnected) {
            textChannelService.sendEmbedWithDefaultMessage(
                channel = channel,
                title = "불가능한 요청입니다.",
                description = "봇이 음성채널에 존재하지 않습니다."
            )
            return false
        }

        // 만약 사용자가 요청했다면, 같은 음성채널인지 확인
        if (member != null &&  audioManager.connectedChannel!!.id != member.voiceState?.channel?.id) {
            textChannelService.sendEmbedWithDefaultMessage(
                channel = channel,
                title = "불가능한 요청입니다.",
                description = "같은 음성채널에 존재하지 않습니다."
            )
            return false
        }
        audioManager.closeAudioConnection()

        textChannelService.sendEmbedWithDefaultMessage(
            channel = channel,
            title = "봇이 음성채널에서 나갔습니다.",
            description = "봇이 음성채널에서 나갔습니다."
        )
        return true
    }
}
