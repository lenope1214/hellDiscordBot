package kr.wearebaord.hellbot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame
import net.dv8tion.jda.api.audio.AudioSendHandler
import java.nio.ByteBuffer

class AudioPlayerSendHandler(
    private val audioPlayer: AudioPlayer,
) : AudioSendHandler {

    private val buffer: ByteBuffer = ByteBuffer.allocate(1024)
    private val frame: MutableAudioFrame = MutableAudioFrame()

    init {
        frame.setBuffer(buffer)
    }

    // audio 제공 가능여부 반환
    override fun canProvide(): Boolean = this.audioPlayer.provide(this.frame)

    override fun provide20MsAudio(): ByteBuffer {
        return this.buffer.flip()
    }

    override fun isOpus(): Boolean {
        return true
    }
}