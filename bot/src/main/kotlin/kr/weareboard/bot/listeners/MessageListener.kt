package kr.weareboard.bot.listeners

import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object MessageListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            System.out.printf(
                "[PM] %s: %s\n",
                event.author.name,
                event.message.contentDisplay
            )
        } else {
            System.out.printf(
                "[%s][%s] %s: %s\n",
                event.guild.name, // 서버이름
                event.channel.name, // 채널이름
                event.member!!.effectiveName, // 서버 별칭
                event.message.contentDisplay // 메시지
            )
        }
    }
}
