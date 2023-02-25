package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.common.isValidContentRaw
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

object StopCommand : CommandInterface {
    val log = LoggerFactory.getLogger(StopCommand::class.java)

    val commands: List<String> = listOf("s", "stop", "ㄴ", "ㄴ새ㅔ", "중지")

    override fun onAction(event: MessageReceivedEvent) {
        val channel = event.channel
        val self = event.guild.selfMember
        val selfVoiceState = self.voiceState
        val member = event.member
        val memberVoiceState = member!!.voiceState

        if (!selfVoiceState!!.inAudioChannel()) {
            channel.sendMessage("'${member.effectiveName}'야 내가 음성채널에 없는데?").queue()
            return
        }

        if (!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("'${member.effectiveName}'야 너가 음성채널에 없는데?").queue()
            return
        }

        if (!selfVoiceState!!.channel!!.id.equals(memberVoiceState!!.channel!!.id)) {
            channel.sendMessage("'${member.effectiveName}'야 너랑 같은 음성채널에 있지 않은데?").queue()
            return
        }

        PlayerManager.INSTANCE.stop(channel as TextChannel, event.member!!.effectiveName)
        self.guild.audioManager.closeAudioConnection()
    }
}