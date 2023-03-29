package kr.weareboard.bot.common

import kr.weareboard.bot.exception.InvalidTextChannel
import kr.weareboard.main.PREFIX
import kr.weareboard.main.TEXT_CHANNEL_NAME
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import org.slf4j.LoggerFactory

class BotCommands

private val log = LoggerFactory.getLogger(BotCommands::class.java)

fun doNotProcessMessage(command: String, commands: List<String>): Boolean {
    if (!(commands.contains(command))) {
        throw IllegalArgumentException("잘못된 명령어입니다.")
    }
    return true
}

fun isValidTextChannel(channel: MessageChannel) {
    if (channel.name != TEXT_CHANNEL_NAME) {
        throw InvalidTextChannel()
    }
}

fun isValidContentRaw(raw: String, commands: List<String>): String {
    val command = raw.let {
        if (it.isEmpty()) {
            throw IllegalArgumentException("raw is empty")
        }
        if (!it.isCorrectPrefix()) {
            throw IllegalArgumentException("raw is not correct prefix")
        }

        val prefix = raw.split(" ")[0]
        val checkCommand = prefix.substring(PREFIX.length)
        if (!commands.contains(checkCommand)) {
            throw IllegalArgumentException("raw is not correct message")
        }
        checkCommand
    }
    return command
}

fun joinVoiceChannelBot(channel: MessageChannel, member: Member, guild: Guild): Boolean {
    val selfVoiceState = member.voiceState
    println("selfVoiceState = $selfVoiceState")

    // 요청자가 음성 채널에 들어가있는가?
    if (!isMemberEnteredChannel(selfVoiceState, channel)) return false

    // 봇이 이미 음성채널에 들어가있는가?
    if (isAlreadyConnectedChannel(guild)) return true

    // 요청자가 음성채널에 연결할 권한이 있는가?
    // if (!isAbleToConnectVoiceChannel(member, channel)) return false

    // 요청자가 음성채널에서 말할 권한이 있는가?
    // if (!isAbleToSpeakVoice(member, channel)) return false

    val audioManager = guild.audioManager

    // selfVoiceState는 CacheFlag에 VOICE_STATE가 포함되어 있어야 한다
    val voiceChannel = selfVoiceState!!.channel

    // 봇이 음성 채널에 들어가도록 함
    audioManager.openAudioConnection(voiceChannel)
    return true
}

private fun isAlreadyConnectedChannel(guild: Guild): Boolean {
    log.info("guild.selfMember = ${guild.selfMember}")
    if (guild.selfMember.voiceState!!.inAudioChannel()) {
        return true
    }
    return false
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

fun isMemberEnteredChannel(
    selfVoiceState: GuildVoiceState?,
    channel: MessageChannel
): Boolean {
    if (selfVoiceState?.inAudioChannel() != true) {
        channel.sendMessage("음성채널에 들어가주세요.").queue()
        return false
    }
    return true
}
