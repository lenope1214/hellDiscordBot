package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.common.isValidContentRaw
import kr.wearebaord.hellbot.common.isValidTextChannel
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

object SkipCommand : CommandInterface{
    val log = LoggerFactory.getLogger(SkipCommand::class.java)

    val commands: List<String> = listOf("sk", "skip", "나", "나ㅑㅔ", "넘기기", "다음", "next", "nt", "nxt")

    override fun onAction(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        try {
            isValidTextChannel(event.channel)
            isValidContentRaw(raw, commands)
        } catch (e: IllegalArgumentException) {
            log.error("skip command error message: ${e.message}, raw : ${raw}" )
            return
        }
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

        if (selfVoiceState!!.channel!!.id != memberVoiceState!!.channel!!.id) {
            channel.sendMessage("'${member.effectiveName}'야 너랑 같은 음성채널에 있지 않은데?").queue()
            return
        }

        // 다음 노래 재생
        PlayerManager.INSTANCE.next(channel as TextChannel)
    }
}