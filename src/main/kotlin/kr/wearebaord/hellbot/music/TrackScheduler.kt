package kr.wearebaord.hellbot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

class TrackScheduler(
    private val player: AudioPlayer
) : AudioEventAdapter() {
    private val queue: BlockingQueue<AudioTrack> =  LinkedBlockingQueue()

    fun queue(track: AudioTrack) {
        // 한글로 주석 작성
        // 만약 플레이어가 현재 재생중이지 않다면 바로 재생, 큐에 추가
        if (!player.startTrack(track, true)) {
            queue.offer(track)
        }
    }

    fun nextTrack() {
        // noInterrupt() is a method on AudioPlayer which is used to make sure the current track finishes playing
        this.player.startTrack(queue.poll(), false)
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        val channel = track.userData as TextChannel
        channel.sendMessageFormat("Now playing: %s by %s", track.info.title, track.info.author).queue()
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        super.onTrackEnd(player, track, endReason)
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        super.onTrackStuck(player, track, thresholdMs)
    }
}
