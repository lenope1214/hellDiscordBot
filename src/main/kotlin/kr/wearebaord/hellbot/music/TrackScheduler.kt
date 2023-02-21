package kr.wearebaord.hellbot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.LoggerFactory
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


class TrackScheduler(
    val player: AudioPlayer
) : AudioEventAdapter() {
    val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()

    private val log = LoggerFactory.getLogger(TrackScheduler::class.java)

    fun queue(track: AudioTrack) {
        // 한글로 주석 작성
        // 만약 플레이어가 현재 재생중이지 않다면 바로 재생, 큐에 추가
        if (!player.startTrack(track, true)) {
            queue.offer(track)
        }


    }

    fun nextTrack() {
        // noInterrupt() is a method on AudioPlayer which is used to make sure the current track finishes playing
        // 만약 다음 노래가 없다면 종료
        if(queue.isEmpty()) {
            return
        }
        player.startTrack(queue.poll()?.makeClone(), false)
    }

    override fun onPlayerPause(player: AudioPlayer?) {
        // Player was paused
    }

    override fun onPlayerResume(player: AudioPlayer?) {
        // Player was resumed
    }

    override fun onTrackStart(player: AudioPlayer, track: AudioTrack) {
        // A track started playing


//        val duration = track.duration
//        val embed: MessageEmbed = EmbedBuilder()
//            .setTitle("Now Playing")
//            .setDescription(track.info.title)
//            .addField("Duration", "${duration / 1000 / 60}m ${duration / 1000 % 60}s", true)
//            .addField("Author", track.info.author, true)
//            .setThumbnail("https://i.ytimg.com/vi/${track.identifier}/hqdefault.jpg")
//            .build()
//        log.info("onTrackStart: $track")
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack?, endReason: AudioTrackEndReason) {
        if(!endReason.mayStartNext){
            nextTrack()
        }else{
            // stoptrack
            player.stopTrack()
        }


        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack?, exception: FriendlyException?) {
        // An already playing track threw an exception (track end event will still be received separately)
    }

    override fun onTrackStuck(player: AudioPlayer, track: AudioTrack, thresholdMs: Long) {
        super.onTrackStuck(player, track, thresholdMs)
        log.info("onTrackStuck: $thresholdMs")
    }
}
