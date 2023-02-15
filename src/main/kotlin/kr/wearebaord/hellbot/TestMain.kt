package kr.wearebaord.hellbot

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.events.onCommand
import dev.minn.jda.ktx.jdabuilder.light
import kr.wearebaord.hellbot.configs.TOKEN
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class TestMain {
}

@OptIn(ExperimentalTime::class)
fun main() {
    val jda = light(TOKEN)

    jda.onCommand("ping") { event ->
        val time = measureTime {
            event.reply("Pong!").await() // suspending
        }.inWholeMilliseconds

        event.hook.editOriginal("Pong: $time ms").queue()
    }
}