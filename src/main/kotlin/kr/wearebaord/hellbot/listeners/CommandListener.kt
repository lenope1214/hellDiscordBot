package kr.wearebaord.hellbot.listeners

import kr.wearebaord.hellbot.OWNER_ID
import kr.wearebaord.hellbot.PREFIX
import kr.wearebaord.hellbot.common.isValidContentRaw
import kr.wearebaord.hellbot.common.joinVoiceChannelBot
import kr.wearebaord.hellbot.common.leaveBot
import kr.wearebaord.hellbot.common.sendEmbed
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

class CommandListener : ListenerAdapter() {
    val log = LoggerFactory.getLogger(CommandListener::class.java)

    override fun onReady(event: ReadyEvent) {
        log.info("Logged in as ${event.jda.selfUser.name}")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        isValidContentRaw(raw, listOf("help", "shutdown", "join", "leave"))

        when (raw.lowercase()) {
            PREFIX + "help" -> {
                event.channel.sendMessage("도움말").queue()
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
                joinVoiceChannelBot(event.channel, event.member!!, event.guild)
            }

            PREFIX + "leave" -> {
                log.info("leave bot by username : ${event.author.name}")
                val channel = event.channel as TextChannel
                leaveBot(event.guild, channel)
            }
        }

//        // prefix + "join"이라면 음성 채널에 참여
//        if (raw.equals(PREFIX + "join", ignoreCase = true)) {
//
//        }

    }
}