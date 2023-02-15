package kr.wearebaord.hellbot

import kr.wearebaord.hellbot.configs.TOKEN
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.EventListener

class ReadyListener : EventListener {
    override fun onEvent(event: GenericEvent) {
        if (event is ReadyEvent) println("API is ready!")
    }

    companion object {
        @Throws(InterruptedException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            // Note: It is important to register your ReadyListener before building
            val jda = JDABuilder.createDefault(TOKEN)
                .addEventListeners(ReadyListener())
                .build()

            // optionally block until JDA is ready
            jda.awaitReady()
        }
    }
}