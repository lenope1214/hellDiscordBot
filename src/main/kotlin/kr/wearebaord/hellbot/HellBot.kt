package kr.wearebaord.hellbot

import io.github.jdiscordbots.command_framework.CommandFramework
import kr.wearebaord.hellbot.configs.Config
import kr.wearebaord.hellbot.listeners.CommandListener
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.time.LocalDateTime

val TOKEN = Config.getEnvByKey("token")
val botTextChannel = Config.getEnvByKey("text_channel_name")
val PREFIX: String = Config.getEnvByKey("prefix")!!
val OWNER_ID = Config.getEnvByKey("owner_id") ?: "0"

fun makeMessage(event: SlashCommandInteractionEvent, message: String) {
    event.reply(message).setEphemeral(false).queue()
}

fun main() {

    /**
     * command framework
     * command annotation을 쓰기 위해 추가
     */
    val framework: CommandFramework =
        CommandFramework() // Step 1
            /* Step 2 */
            .setMentionPrefix(true) // Allow mention prefix, Default: true
            .setPrefix(PREFIX) // Default: !
            .setOwners(
                // Set owners ids for permissions system, Default: {}
                "262951571053084673",
                "796395869192454166",
            )


    val jdaBuilder = JDABuilder.createDefault(TOKEN)
    val jda = configureMemoryUsage(jdaBuilder)
        .setActivity(
            Activity.playing(
                "열정을 다해 놀리기를\n${
                    LocalDateTime.now().format(
                        // HH:mm
                        java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                    )
                }부터"
            )
        )
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
        // cache를 사용하지 않는다면, 이벤트를 받을 수 없다.
        .disableCache(
//            CacheFlag.ACTIVITY,
//            CacheFlag.CLIENT_STATUS,
            CacheFlag.EMOJI,
//            CacheFlag.MEMBER_OVERRIDES,
//            CacheFlag.VOICE_STATE,
        )
        .addEventListeners(
            framework.build(),
//            DefaultListener(),
//            CommandListener(),
//            PlayListener(),
        )
        .build()
            .awaitReady()

    // logging application id
    println("Application ID: ${jda.selfUser.id}")
}

fun configureMemoryUsage(builder: JDABuilder): JDABuilder {
    // Disable cache for member activities (streaming/games/spotify)
    builder.disableCache(CacheFlag.ACTIVITY)

    // Only cache members who are either in a voice channel or owner of the guild
    builder.setMemberCachePolicy(MemberCachePolicy.VOICE.or(MemberCachePolicy.OWNER))

    // Disable member chunking on startup
    builder.setChunkingFilter(ChunkingFilter.NONE)

    // Disable presence updates and typing events
    builder.disableIntents(GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING)

    // Consider guilds with more than 50 members as "large".
    // Large guilds will only provide online members in their setup and thus reduce bandwidth if chunking is disabled.
    builder.setLargeThreshold(50)

    return builder
}