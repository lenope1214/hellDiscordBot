package kr.wearebaord.hellbot.common

import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.button
import kr.wearebaord.hellbot.domain.enums.ComponentTypes
import kr.wearebaord.hellbot.domain.enums.EmojiValue
import kr.wearebaord.hellbot.exception.InvalidTextChannel
import kr.wearebaord.hellbot.music.PlayTrackInfo
import kr.wearebaord.hellbot.music.status.getRepeatEmoji
import kr.wearebaord.hellbot.music.status.getRepeatText
import kr.weareboard.main.PREFIX
import kr.weareboard.main.SHOW_BUTTONS
import kr.weareboard.main.TEXT_CHANNEL_NAME
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.awt.Color
import java.time.OffsetDateTime
import java.util.concurrent.TimeUnit

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

fun MessageChannel.deleteAllMessages() {
    val channel = this as TextChannel
    channel.deleteAllMessages()
}
