package kr.weareboard.bot.listeners.music

import kr.weareboard.bot.domain.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GambleCommand(
    private val playerManager: PlayerManager
) : CommandInterface {
    val log: Logger = LoggerFactory.getLogger(GambleCommand::class.java)

    val commands: List<String> = listOf(
        "dd", "ㅇㅇ", "올인", // 올인
        "gv", "ㅎㅍ", "하프", // 하프
        "q", "ㅃ", "ㅂ", "삥", // 삥
        "rr", "ㄱㄱ", "rtt", "eqr", "ㄷㅂㄱ", // 구걸, 기생수, 돈받기 기초자산 수급받기
        "e", "ㄷ", "돈", // 돈 현황 확인,
        "td", "ㅅㅇ", "순위", // 순위 현황 확인
        "rq", "ㄱㅂ", "기부" // 기부하기 (돈버리기)
    )

    override fun onAction(event: MessageReceivedEvent) {
        val channel = event.channel
        val self = event.guild.selfMember
        val selfVoiceState = self.voiceState
        val member = event.member
        val memberVoiceState = member!!.voiceState

        log.info("겜블 명령어 실행")
    }

    override fun onHelp(): String {
        TODO("Not yet implemented")
    }
}
