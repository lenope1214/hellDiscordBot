package kr.wearebaord.hellbot

import kr.wearebaord.hellbot.configs.TOKEN
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag


fun makeMessage(event: SlashCommandInteractionEvent, message: String) {
    event.reply(message).setEphemeral(false).queue()
}

fun main() {

    val jdaBuilder = JDABuilder.createDefault(TOKEN)
    val jda: JDA = configureMemoryUsage(jdaBuilder)
        .addEventListeners(Bot())
        .build()

    // Sets the global command list to the provided commands (removing all others)
    jda.updateCommands().addCommands(
        Commands.slash("ping", "Calculate ping of the bot"),
        Commands.slash("놀리기", "대상을 놀립니다.")
            .addOption(
                OptionType.STRING,
                "nickname",
                "놀릴 대상의 닉네임(이름)을 입력해주세요",
                true
            ),
        Commands.slash("사실부탁임", "사실부탁임 출력")
    ).queue()
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