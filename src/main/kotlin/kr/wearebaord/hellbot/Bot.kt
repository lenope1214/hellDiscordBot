package kr.wearebaord.hellbot

import kr.wearebaord.hellbot.utils.KoreanUtil
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import org.slf4j.LoggerFactory

class Bot : ListenerAdapter() {

    val log = LoggerFactory.getLogger(Bot::class.java)

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