package kr.weareboard.bot.common

import kr.weareboard.bot.exception.InvalidTextChannel
import kr.weareboard.main.PREFIX
import kr.weareboard.main.TEXT_CHANNEL_NAME
import net.dv8tion.jda.api.entities.GuildVoiceState
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
