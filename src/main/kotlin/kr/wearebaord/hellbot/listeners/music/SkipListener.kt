package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.PREFIX
import kr.wearebaord.hellbot.makeMessage
import kr.wearebaord.hellbot.utils.KoreanUtil
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory
import kr.wearebaord.hellbot.common.isInvalidMessage
import kr.wearebaord.hellbot.common.joinVoiceChannelBot
import kr.wearebaord.hellbot.common.parseCommand
import kr.wearebaord.hellbot.common.parseContent
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import java.net.URI
import java.net.URISyntaxException
import java.util.concurrent.TimeUnit

object SkipListener : ListenerAdapter() {
    val log = LoggerFactory.getLogger(SkipListener::class.java)

    private val commands: List<String> = listOf("sk", "skip", "나", "나ㅑㅔ", "넘기기", "다음", "next", "nt", "nxt")

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        val command = parseCommand(raw)
        if(isInvalidMessage(event)) return
        if (!commands.contains(command)) return
        println("skip command")

        val channel = event.channel
        val self = event.guild.selfMember
        val selfVoiceState = self.voiceState
        val member= event.member
        val memberVoiceState = member!!.voiceState

        if(!selfVoiceState!!.inAudioChannel()) {
            channel.sendMessage("'${member.effectiveName}'야 내가 음성채널에 없는데?").queue()
            return
        }

        if(!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("'${member.effectiveName}'야 너가 음성채널에 없는데?").queue()
            return
        }

        if(!selfVoiceState!!.channel!!.id.equals(memberVoiceState!!.channel!!.id)) {
            channel.sendMessage("'${member.effectiveName}'야 너랑 같은 음성채널에 있지 않은데?").queue()
            return
        }

        val guild = event.guild
        val musicManager = PlayerManager.INSTANCE.getMusicManager(guild)
        val audioPlayer = musicManager.audioPlayer
    
        if(audioPlayer.playingTrack == null) { // 없을 시 null임
            channel.sendMessage("재생중인 노래가 없어요.").queue()
            return
        }

        // 다음 노래 재생
        musicManager.scheduler.nextTrack()
        channel.sendMessage("다음 노래를 재생합니다.").queue()
    }
}