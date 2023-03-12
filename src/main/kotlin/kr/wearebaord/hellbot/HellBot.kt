package kr.wearebaord.hellbot

import kr.wearebaord.hellbot.configs.Config
import kr.wearebaord.hellbot.configs.EnvTypes
import kr.wearebaord.hellbot.listeners.DefaultListener
import kr.wearebaord.hellbot.listeners.MessageListener
import kr.wearebaord.hellbot.listeners.MusicListener
import kr.wearebaord.hellbot.listeners.music.PlayCommand
import kr.wearebaord.hellbot.listeners.music.SkipCommand
import kr.wearebaord.hellbot.listeners.music.StopCommand
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess

var PROFILE: EnvTypes = EnvTypes.PROD
lateinit var JDA: net.dv8tion.jda.api.JDA
lateinit var BOT_VERSION: String
lateinit var TOKEN: String
lateinit var BOT_ID: String
lateinit var TEXT_CHANNEL_NAME: String
lateinit var PREFIX: String
lateinit var OWNER_ID: String
lateinit var NOTICE_FLAG: String
var VOLUME: Int = 50
var SHOW_BUTTONS: Boolean = false

fun makeMessage(event: SlashCommandInteractionEvent, message: String) {
    event.reply(message).setEphemeral(false).queue()
}

suspend fun main(vararg args: String) {
    // 프로필 설정
    args.forEach {
        println(it)
    }
    if (args.isNotEmpty()) {
        PROFILE = when (args[0].uppercase(locale = java.util.Locale.getDefault())) {
            "DEV" -> {
                EnvTypes.DEV
            }
            "PROD" -> {
                EnvTypes.PROD
            }
            else -> {
                EnvTypes.PROD
            }
        }
    }

    // 환경 설정, prod, dev에 맞게 설정
    initEnvironment(PROFILE)


    val jdaBuilder = JDABuilder.createDefault(TOKEN)
    JDA = configureMemoryUsage(jdaBuilder)
        .setActivity(
            Activity.playing(
                "버전 : ${BOT_VERSION} 도움말 : ${PREFIX}도움말"
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
            DefaultListener,
//            MessageListener,
            MusicListener,
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
    println("PROFILE = ${PROFILE}")
    println("TOKEN = ${TOKEN}")
    println("PREFIX = ${PREFIX}")
    println("OWNER_ID = ${OWNER_ID}")
    println("TEXT_CHANNEL_NAME = ${TEXT_CHANNEL_NAME}")
    println("Application ID: ${JDA!!.selfUser.id}")
    println("VOLUME = ${VOLUME}")
    println("SHOW_BUTTONS = ${SHOW_BUTTONS}")
}

fun initEnvironment(profile: EnvTypes) {
    Config.onProfile(profile)
    BOT_VERSION = Config.getEnvByKey("bot_version") ?: LocalDateTime.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        .toString()
    TOKEN = Config.getEnvByKey("token")!!
    BOT_ID = Config.getEnvByKey("bot_id")!!
    TEXT_CHANNEL_NAME = Config.getEnvByKey("text_channel_name")?.let { it } ?: "헬파티봇"
    PREFIX = Config.getEnvByKey("prefix")!!
    OWNER_ID = Config.getEnvByKey("owner_id") ?: "0"
    VOLUME = (Config.getEnvByKey("volume") ?: "10").toInt()
    SHOW_BUTTONS = (Config.getEnvByKey("show_buttons") ?: "true").toBoolean()
    NOTICE_FLAG = Config.getEnvByKey("notice_flag") ?: "[공지]"
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
