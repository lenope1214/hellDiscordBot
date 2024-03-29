package kr.weareboard.bot.listeners.music

import kr.weareboard.bot.domain.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class StopCommand(
    private val playerManager: PlayerManager
) : CommandInterface {
    val log: Logger = LoggerFactory.getLogger(StopCommand::class.java)

    val commands: List<String> = listOf("s", "stop", "ㄴ", "ㄴ새ㅔ", "중지")

    override fun onAction(event: MessageReceivedEvent) {
        val channel = event.channel
        val self = event.guild.selfMember
        val selfVoiceState = self.voiceState
        val member = event.member
        val memberVoiceState = member!!.voiceState

        if (!selfVoiceState!!.inAudioChannel()) {
            log.info("selfVoiceState is not in audio channel")
            channel.sendMessage("'${member.effectiveName}'야 내가 음성채널에 없는데?!!").queue()
            return
        }

        if (!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("'${member.effectiveName}'야 너가 음성채널에 없는데?!!").queue()
            return
        }

        if (selfVoiceState!!.channel!!.id != memberVoiceState!!.channel!!.id) {
            channel.sendMessage("'${member.effectiveName}'야 너랑 같은 음성채널에 있지 않은데?").queue()
            return
        }

        playerManager.stop(channel as TextChannel, event.member!!)
        self.guild.audioManager.closeAudioConnection()
    }

    override fun onHelp(): String {
        TODO("Not yet implemented")
    }
}
