package kr.wearebaord.hellbot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager

class GuildMusicManager(
    manager: AudioPlayerManager,
) {
    val audioPlayer: AudioPlayer
    val scheduler: TrackScheduler
    val sendHandler: AudioPlayerSendHandler

    init {
        this.audioPlayer = manager.createPlayer()
        this.scheduler = TrackScheduler(this.audioPlayer)
        this.audioPlayer.addListener(this.scheduler)
        this.sendHandler = AudioPlayerSendHandler(this.audioPlayer)
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