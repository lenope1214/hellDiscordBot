package kr.wearebaord.hellbot

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent

class MessageListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            System.out.printf(
                "[PM] %s: %s\n", event.author.name,
                event.message.contentDisplay
            )
        } else {
            System.out.printf(
                "[%s][%s] %s: %s\n", event.guild.name,
                event.channel.name, event.member!!.effectiveName,
                event.message.contentDisplay
            )
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val jda = JDABuilder.createDefault(TOKEN)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT) // enables explicit access to message.getContentDisplay()
                .build()
            //You can also add event listeners to the already built JDA instance
            // Note that some events may not be received if the listener is added after calling build()
            // This includes events such as the ReadyEvent
            jda.addEventListener(MessageListener())
        }
    }
}