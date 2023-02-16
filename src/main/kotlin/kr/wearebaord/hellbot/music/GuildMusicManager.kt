package kr.wearebaord.hellbot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager

class GuildMusicManager(
    manager: AudioPlayerManager,
) {
    val audioPlayer: AudioPlayer
    val scheduler: TrackScheduler
    private val sendHandler: AudioPlayerSendHandler

    init {
        this.audioPlayer = manager.createPlayer()
        this.scheduler = TrackScheduler(this.audioPlayer)
        this.audioPlayer.addListener(this.scheduler)
        this.sendHandler = AudioPlayerSendHandler(this.audioPlayer)
    }

    fun getSendHandler(): AudioPlayerSendHandler {
        return this.sendHandler
    }


}