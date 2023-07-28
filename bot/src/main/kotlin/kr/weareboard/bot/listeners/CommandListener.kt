package kr.weareboard.bot.listeners

import kr.weareboard.bot.common.isValidContentRaw
import kr.weareboard.bot.service.interfaces.BotService
import kr.weareboard.bot.service.interfaces.TextChannelService
import kr.weareboard.main.OWNER_ID
import kr.weareboard.main.PREFIX
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CommandListener(
    private val botService: BotService,
    private val textChannelService: TextChannelService
) : ListenerAdapter() {
    val log = LoggerFactory.getLogger(CommandListener::class.java)

    override fun onReady(event: ReadyEvent) {
        log.info("Logged in as ${event.jda.selfUser.name}")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        isValidContentRaw(raw, listOf("help", "shutdown", "join", "leave"))

        when (raw.lowercase()) {
            PREFIX + "help" -> {
                event.channel.sendMessage("""
                    ${PREFIX}help : 도움말을 보여줍니다.
                    ${PREFIX}join : 봇이 명령어 입력한 사람이 존재하는 음성채널에 들어갑니다.
                    ${PREFIX}leave : 봇이 음성채널에서 나갑니다.
                """.trimIndent()).queue()
            }

            PREFIX + "shutdown" -> {
                if (!(event.author.id == OWNER_ID || event.member?.hasPermission(Permission.ADMINISTRATOR) == true)) {
                    event.message.delete().queue()
                    event.channel.sendMessage("권한이 없습니다.").queue()
                }

                log.info("Shutdown command received from ${event.author.name}")
                event.jda.shutdown()
            }

            PREFIX + "join" -> {
                log.info("join bot by username : ${event.author.name}")
                botService.joinVoiceChannelIfNotJoined(event.channel as TextChannel, event.member!!, event.guild)
            }

            PREFIX + "leave" -> {
                log.info("leave bot by username : ${event.author.name}")
                val channel = event.channel as TextChannel
                botService.leaveBot(event.guild, channel, event.member)
            }
        }
    }
}
