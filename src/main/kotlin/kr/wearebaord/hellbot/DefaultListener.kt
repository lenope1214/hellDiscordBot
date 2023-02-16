package kr.wearebaord.hellbot

import kr.wearebaord.hellbot.configs.Config
import kr.wearebaord.hellbot.utils.KoreanUtil
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import org.slf4j.LoggerFactory

class DefaultListener : ListenerAdapter() {
    val PREFIX: String = Config.getEnvByKey("prefix")!!
    val OWNER_ID = Config.getEnvByKey("owner_id") ?: "0"

    val log = LoggerFactory.getLogger(DefaultListener::class.java)

    override fun onReady(event: ReadyEvent) {
        log.info("Logged in as ${event.jda.selfUser.name}")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw


        if (event.author.isBot) return // 봇이면 진행하지 않는다.
        // raw의 대소문자에 상관없이 prefix로 시작하는지 확인한다.
        if (!raw.startsWith(PREFIX, ignoreCase = true)) return
        if (!(event.author.id == OWNER_ID || event.member?.hasPermission(Permission.ADMINISTRATOR) == true)) {
            event.message.delete().queue()
            event.channel.sendMessage("권한이 없습니다.").queue()
        }

        // prefix + "shutdown"이라면 봇을 종료한다.
        if (raw.equals(PREFIX + "shutdown", ignoreCase = true)) {
            log.info("Shutdown command received from ${event.author.name}")
            event.jda.shutdown()
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        // make sure we handle the right command
        when (event.name) {
            "ping" -> {
                val time = System.currentTimeMillis()
                event.reply("Pong!").setEphemeral(false) // reply or acknowledge
                    .flatMap { event.hook.editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time) }
                    .queue()
            }

            "놀리기" -> {
                if (!event.member?.hasPermission(Permission.MESSAGE_SEND)!!) {
                    event.reply("You cannot Send Message ;)").setEphemeral(true).queue()
                }
                val nickname = event.getOption("nickname", OptionMapping::getAsString)
                if (nickname.isNullOrBlank()) {
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

                makeMessage(event, str)
            }

            "사실부탁임" -> {
                makeMessage(
                    event, """
                    사실 부탁임...

                    근데 어쩔 수 없음 ㅠㅠ

                    내가 뭘 할 수 있는 게 없음 ㅜㅜㅜㅜ

                    난 좆밥 새끼임 ㅠ

                    존나 화내거나 욕하는 거 말고 할 수 있는 게 없음 ㅠㅠ
                """.trimIndent()
                )
            }
        }
    }
}