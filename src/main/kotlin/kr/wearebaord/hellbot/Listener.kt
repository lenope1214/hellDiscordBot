package kr.wearebaord.hellbot

import dev.minn.jda.ktx.util.SLF4J
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object Listener : ListenerAdapter() {
    private val log by SLF4J

    override fun onMessageReceived(event: MessageReceivedEvent) {
        log.info("[{}] {}: {}", event.channel.name, event.author.asTag, event.message.contentDisplay)
    }
}