package kr.wearebaord.hellbot.tutorial

import com.sedmelluq.discord.lavaplayer.format.AudioPlayerInputStream
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.FunctionalResultHandler
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import java.io.IOException
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.DataLine
import javax.sound.sampled.LineUnavailableException
import javax.sound.sampled.SourceDataLine

object LocalPlayerDemo {
    @Throws(LineUnavailableException::class, IOException::class)
    @JvmStatic
    fun main(args: Array<String>) {
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
}