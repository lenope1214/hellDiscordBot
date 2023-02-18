package kr.wearebaord.hellbot

import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats.COMMON_PCM_S16_BE
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.SourceDataLine


class PlayListener : ListenerAdapter() {

    private val log = LoggerFactory.getLogger(PlayListener::class.java)

    override fun onReady(event: ReadyEvent) {
        log.info("on ready player listener")
        // add slash command
        event.jda.upsertCommand(
            Commands.slash("ym", "유튜브 링크를 재생합니다.")
                .addOption(OptionType.STRING, "url", "음악의 url을 입력해주세요.", true)
        )
            .queue()
        event.jda.upsertCommand(
            Commands.slash("demo", "데모")
        )
            .queue()
    }


    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        // member와 user의 차이
        // member는 서버에 가입한 유저
        // user는 디스코드에 가입한 유저
        when (event.name) {
            "demo" -> {
                val manager: AudioPlayerManager = DefaultAudioPlayerManager()
                AudioSourceManagers.registerRemoteSources(manager)
                manager.configuration.outputFormat = StandardAudioDataFormats.COMMON_PCM_S16_BE
                val player = manager.createPlayer()
                manager.loadItem("ytsearch: epic soundtracks", FunctionalResultHandler(null, { playlist: AudioPlaylist ->
                    player.playTrack(
                        playlist.tracks[0]
                    )
                }, null, null))
                val format = manager.configuration.outputFormat
                val stream = AudioPlayerInputStream.createStream(player, format, 10000L, false)
                val info = DataLine.Info(SourceDataLine::class.java, stream.format)
                val line = AudioSystem.getLine(info) as SourceDataLine
                line.open(stream.format)
                line.start()
                val buffer = ByteArray(StandardAudioDataFormats.COMMON_PCM_S16_BE.maximumChunkSize())
                var chunkSize: Int
                while (stream.read(buffer).also { chunkSize = it } >= 0) {
                    line.write(buffer, 0, chunkSize)
                }
            }
            "ym" -> {
                log.info("ym command by ${event.member}")
                val channel = event.channel
                // self member
                val self: Member = event.guild!!.selfMember
                val selfVoiceState = self!!.voiceState
                var url = event.getOption("url")!!.asString

                log.info("self: ${self}")
                log.info("member : ${event.member}")
                log.info("url : ${url}")


                val member = event.member
                val memberVoiceState = member!!.voiceState

                if (!memberVoiceState!!.inAudioChannel()) {
                    channel.sendMessage("음성채널에 들어가주세요.").queue()
                    return
                }

                if (!selfVoiceState!!.inAudioChannel()) {
                    joinVoiceChannelBot(event.channel, event.member!!, event.guild!!)
                }

                if (!isUrl(url)) {
                    url = "ytsearch: $url"
                }

                PlayerManager.INSTANCE
                    .loadAndPlay(channel as TextChannel, "https://www.youtube.com/watch?v=JiF3pbvR5G0")
//                    .loadAndPlay(channel as TextChannel, url)
            }
        }
    }

    private fun isUrl(url: String): Boolean {
        try {
            URI(url)
            return true
        } catch (e: URISyntaxException) {
            return false
        }
    }
}