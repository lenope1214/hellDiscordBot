package kr.wearebaord.hellbot.funcs

import io.github.jdiscordbots.command_framework.command.CommandEvent
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.MessageHistory


class TextChannelFuncs {
    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(TextChannelFuncs::class.java)

        fun deleteMessage(channel: MessageChannel, event: CommandEvent?) {
            try {
                val history = MessageHistory.getHistoryFromBeginning(channel).complete()
                val mess: List<Message> = history.retrievedHistory
                log.info("mess : $mess")
                for (m in mess) {
                    log.info("m : $m")
                    m.delete().queue()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                log.error("error: ${e.message}")
            } finally {
                // delete command message
                event?.message?.delete()?.let { it.queue() }
            }
        }
    }
}