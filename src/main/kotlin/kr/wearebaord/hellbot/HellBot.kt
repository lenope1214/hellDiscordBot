package kr.wearebaord.hellbot

import io.github.jdiscordbots.command_framework.CommandFramework
import kr.wearebaord.hellbot.configs.Config
import kr.wearebaord.hellbot.listeners.DefaultListener
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.time.LocalDateTime
import kotlin.system.exitProcess

var JDA: net.dv8tion.jda.api.JDA? = null
val TOKEN: String = Config.getEnvByKey("token")!!
val TEXT_CHANNEL_NAME: String = Config.getEnvByKey("text_channel_name")?.let { it } ?: "헬파티봇"
val PREFIX: String = Config.getEnvByKey("prefix")!!
val OWNER_ID: String = Config.getEnvByKey("owner_id") ?: "0"

fun makeMessage(event: SlashCommandInteractionEvent, message: String) {
    event.reply(message).setEphemeral(false).queue()
}

suspend fun main() {

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
    JDA = configureMemoryUsage(jdaBuilder)
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
            CacheFlag.ACTIVITY,
            CacheFlag.VOICE_STATE, // voice state caching??
            CacheFlag.EMOJI,
            CacheFlag.CLIENT_STATUS,
            CacheFlag.MEMBER_OVERRIDES,
            CacheFlag.ROLE_TAGS,
        )
        // cache를 사용하지 않는다면, 이벤트를 받을 수 없다.
        .disableCache(
            CacheFlag.STICKER
        )
        .addEventListeners(
//            HelloBot,
//            framework.build(),
            DefaultListener,
//            CommandListener(),
//            PlayListener(),
        )
        .build()
        .awaitReady()

    if (JDA == null) {
        println("JDA is null. Exiting...")
        exitProcess(1)
    }

    // 채팅 채널 이름이 "헬파티봇" 인 채널은 5초마다 메세지를 전부 삭제한다.
    // 5초마다 메세지를 삭제하는 이유는, 채팅이 쌓이는 것을 방지하기 위함이다.


    // logging application id
    println("TOKEN = ${TOKEN}")
    println("PREFIX = ${PREFIX}")
    println("OWNER_ID = ${OWNER_ID}")
    println("TEXT_CHANNEL_NAME = ${TEXT_CHANNEL_NAME}")
    println("Application ID: ${JDA!!.selfUser.id}")
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