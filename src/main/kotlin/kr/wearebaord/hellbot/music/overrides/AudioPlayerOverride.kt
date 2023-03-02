package kr.wearebaord.hellbot.music.overrides

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayer
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager

abstract class AudioPlayerOverride(manager: DefaultAudioPlayerManager) : DefaultAudioPlayer(manager) {
    override fun toString(): String {
        return "AudioPlayer[playingTrack=${this.playingTrack}, volume=${this.volume}, paused=${this.isPaused}]"
    }
}