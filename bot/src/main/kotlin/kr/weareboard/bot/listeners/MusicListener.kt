package kr.weareboard.bot.listeners

import kr.weareboard.bot.common.isCorrectPrefix
import kr.weareboard.bot.common.isValidTextChannel
import kr.weareboard.bot.common.parseCommand
import kr.weareboard.bot.domain.PlayerManager
import kr.weareboard.bot.domain.enums.EmojiValue
import kr.weareboard.bot.exception.InvalidTextChannel
import kr.weareboard.bot.listeners.music.PlayCommand
import kr.weareboard.bot.listeners.music.SkipCommand
import kr.weareboard.bot.listeners.music.StopCommand
import kr.weareboard.bot.music.status.getRepeatEmoji
import kr.weareboard.bot.music.status.getRepeatText
import kr.weareboard.bot.service.interfaces.BotService
import kr.weareboard.bot.service.interfaces.TextChannelService
import kr.weareboard.main.NOTICE_FLAG
import kr.weareboard.main.TEXT_CHANNEL_NAME
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

// 초기 메세지 전달 쓰레드 생성
val messageThread = Thread {
    Thread.sleep(1000)
}

@Component
class MusicListener(

) : ListenerAdapter() {
    private val log = LoggerFactory.getLogger(MusicListener::class.java)
}
