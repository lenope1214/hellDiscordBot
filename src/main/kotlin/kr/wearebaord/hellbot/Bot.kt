package kr.wearebaord.hellbot

import kr.wearebaord.hellbot.configs.TOKEN
import kr.wearebaord.hellbot.utils.KoreanUtil
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import net.dv8tion.jda.api.utils.cache.CacheFlag


class Bot : ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        // make sure we handle the right command
        when (event.name) {
            "ping" -> {
                val time = System.currentTimeMillis()
                event.reply("Pong!").setEphemeral(false) // reply or acknowledge
                    .flatMap { _: InteractionHook? ->
                        event.hook.editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)
                    } // then edit original
                    .queue() // Queue both reply and edit
            }
            "myinfo" -> {
                val target: User? = event.getOption("user", OptionMapping::getAsUser)
                // optionally check for member information
                // optionally check for member information
                val member: Member? = event.getOption("user", OptionMapping::getAsMember)

                log.info("target: $target")
                log.info("member: $member")

                val time = System.currentTimeMillis()
                event.reply("Pong!").setEphemeral(false) // reply or acknowledge
                    .flatMap { _: InteractionHook? ->
                        event.hook.editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time)
                    } // then edit original
                    .queue() // Queue both reply and edit
            }
            "놀리기" -> {
                if (!event.member?.hasPermission(Permission.MESSAGE_SEND)!!) {
                    event.reply("You cannot Send Message ;)").setEphemeral(true).queue()
                }
                val nickname = event.getOption("nickname", OptionMapping::getAsString)
                if(nickname.isNullOrBlank()){
                    event.reply("닉네임을 입력해주세요").setEphemeral(true).queue()
                }
                // optionally check for member information
                val str = """
                    부럽다

                    부러워

                    앞에 걷는 저 사람이 부러워

                    뒷모습밖에 보이지 않지만 부러워

                    나도 되고 싶어

                    나도 `${nickname}`${KoreanUtil.getCompleteWord(nickname!!, "이", "가")} 되고 싶어

                    너였구나

                    앞에서 걷던 그 사람이 너였어

                    너만 보면 내 세상이 무너져

                    그리고

                    지금도 무너지고 있다
                """.trimIndent() // 앞뒤공백제거

                makeMessage(event, "놀리기", str)
            }
            "사실부탁임" -> {
                makeMessage(event, "사실부탁임", """
                    사실 부탁임...

                    근데 어쩔 수 없음 ㅠㅠ

                    내가 뭘 할 수 있는 게 없음 ㅜㅜㅜㅜ

                    난 좆밥 새끼임 ㅠ

                    존나 화내거나 욕하는 거 말고 할 수 있는 게 없음 ㅠㅠ
                """.trimIndent())
            }
        }
    }
}

fun makeMessage(event: SlashCommandInteractionEvent, command: String, message: String){
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
        Commands.slash("myinfo", "내 정보 서버에 로깅"),
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