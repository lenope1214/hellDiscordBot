package kr.weareboard.bot.listeners.music

import kr.weareboard.bot.domain.PlayerManager
import kr.weareboard.bot.domain.PlayerManagerImpl
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class SkipCommand(
    private val playerManager: PlayerManager
) : CommandInterface {
    val log = LoggerFactory.getLogger(SkipCommand::class.java)

    val commands: List<String> = listOf("sk", "skip", "나", "나ㅑㅔ", "넘기기", "다음", "next", "nt", "nxt")

    override fun onAction(event: MessageReceivedEvent) {
        log.info("skip command")

        val channel = event.channel
        val self = event.guild.selfMember
        val selfVoiceState = self.voiceState
        val member = event.member
        val memberVoiceState = member!!.voiceState

        if (!selfVoiceState!!.inAudioChannel()) {
            channel.sendMessage("'${member.effectiveName}'야 내가 음성채널에 없는데?!").queue()
            return
        }

        if (!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("'${member.effectiveName}'야 너가 음성채널에 없는데?!").queue()
            return
        }

        if (selfVoiceState.channel!!.id != memberVoiceState!!.channel!!.id) {
            channel.sendMessage("'${member.effectiveName}'야 너랑 같은 음성채널에 있지 않은데?").queue()
            return
        }

        // 다음 노래 재생
        playerManager.next(channel as TextChannel, event.member!!)
    }

    override fun onHelp(): String {
        TODO("Not yet implemented")
    }
}
