package kr.weareboard.bot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import org.springframework.stereotype.Component


@Component
class GuildMusicManager(
    val scheduler: TrackScheduler,
) {
    lateinit var audioPlayer: AudioPlayer
    lateinit var sendHandler: AudioPlayerSendHandler

    fun init(manager: AudioPlayerManager){
        this.audioPlayer = manager.createPlayer()
        this.audioPlayer.addListener(this.scheduler)
        this.sendHandler = AudioPlayerSendHandler(this.audioPlayer)
        scheduler.init(this.audioPlayer)
    }

    fun clearQueue() {
        this.scheduler.queue.clear()
    }

    fun stopTrack() {
        this.scheduler.player.stopTrack()
    }

    fun stopMusic() {
        stopTrack()
        clearQueue()
    }

    fun isPlaying(): Boolean {
        return this.scheduler.player.playingTrack != null
    }
}
