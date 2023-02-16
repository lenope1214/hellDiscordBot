package kr.wearebaord.hellbot.tutorial

import kr.wearebaord.hellbot.CommandListener
import kr.wearebaord.hellbot.DefaultListener
import kr.wearebaord.hellbot.configs.Config
import kr.wearebaord.hellbot.configs.Config.Companion.getEnvByKey
import kr.wearebaord.hellbot.configureMemoryUsage
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.cache.CacheFlag

object Bootstrap {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val jdaBuilder = JDABuilder.createDefault(Config.getEnvByKey("token"))
        val jda: JDA = configureMemoryUsage(jdaBuilder)
            .setActivity(Activity.playing("열정을 다해 놀리기를"))
            .enableIntents(
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_MESSAGE_REACTIONS,
                GatewayIntent.GUILD_VOICE_STATES,
            )
            .enableCache(
                CacheFlag.VOICE_STATE, // voice state caching??
            )
            .addEventListeners(
                DefaultListener(),
                CommandListener(),
            )
            .build()
    }
}