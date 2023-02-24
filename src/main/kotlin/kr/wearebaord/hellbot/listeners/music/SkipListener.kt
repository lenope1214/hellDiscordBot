package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.common.doNotProcessMessage
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import kr.wearebaord.hellbot.common.isInvalidMessage
import kr.wearebaord.hellbot.common.parseCommand
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

object SkipListener : ListenerAdapter() {
    val log = LoggerFactory.getLogger(SkipListener::class.java)

    private val commands: List<String> = listOf("sk", "skip", "나", "나ㅑㅔ", "넘기기", "다음", "next", "nt", "nxt")

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        val command = try{
            parseCommand(raw)
        }catch (e: IllegalArgumentException) {
            return
        }

        // 아래 두 개는 한 쌍
        if (doNotProcessMessage(command, commands)) return
        if(isInvalidMessage(event)) {
            event.message.delete().queue()
            return
        }
        println("skip command")

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

//        val guild = event.guild
//        val musicManager = PlayerManager.INSTANCE.getMusicManager(guild)
//        val audioPlayer = musicManager.audioPlayer
//
//        if (audioPlayer.playingTrack == null) { // 없을 시 null임
//            channel.sendMessage("재생중인 노래가 없어요.").queue()
//            return
//        }

        // 다음 노래 재생
        PlayerManager.INSTANCE.next(channel as TextChannel)
    }
}