package kr.wearebaord.hellbot.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import kr.wearebaord.hellbot.VOLUME
import kr.wearebaord.hellbot.music.overrides.AudioPlayerOverride
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.slf4j.LoggerFactory
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue


class TrackScheduler(
    val player: AudioPlayer,
) : AudioEventAdapter() {
    private var pause: Boolean = false
    private var repeat: Boolean = false
    private var lastTrack: AudioTrack? = null
    val queue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()

    private val log = LoggerFactory.getLogger(TrackScheduler::class.java)

    init {
        player.volume = VOLUME!!

        // Thread 하나 생성해서
        // music에 사용되는 매개변수를 1분 단위로 로깅
//        Thread {
//            while (true) {
//                Thread.sleep(3000)
//                log.info("player: ${player.toString()}, pause: $pause, repeat: $repeat, lastTrack: $lastTrack, queue: $queue")
//            }
//        }.start()
    }

    fun isPause(): Boolean {
        return pause
    }

    fun doPause() {
        pause = true
    }

    fun doNotPause() {
        pause = false
    }

    fun isRepeat(): Boolean {
        return repeat
    }

    fun doRepeat() {
        repeat = true
    }

    fun doNotRepeat() {
        repeat = false
    }

    fun queue(track: AudioTrack) {
        // startTrack이 true일 때는 현재 재생하는 노래가 없을때 바로 재생시켰음을 의미

        // false일 때는 재생시키지 않았음을 의미.

        val startTrack = player.startTrack(track, true)
        log.info("queue - startTrack: $startTrack")
        if (startTrack) {
            // 만약 첫 곡(플레이 중이 아닌 상태)이라면 마지막 곡 정보를 업데이트
            lastTrack = track
        }
        if (!startTrack) {
            // 현재 플레이 중인 노래가 있다면 queue에 추가.
            queue.offer(track)
        }


    }

    fun nextTrack() {
        // noInterrupt() is a method on AudioPlayer which is used to make sure the current track finishes playing
        // 만약 다음 노래가 없다면 종료
        log.info("nextTrack - queue size = ${queue.size}, isRepeat: ${isRepeat()}, lastTrack: $lastTrack")

        if (isRepeat() && lastTrack != null) {
            // 만약 repeat가 true면 다시 큐에 추가
            queue.add(lastTrack)
        } else if (queue.isEmpty()) {
            player.stopTrack()
        }

        player.startTrack(queue.poll()?.makeClone(), false)
    }

    fun jumpTrack(index: Int, repeat: Boolean = false) {
        log.info("jumpTrack - index = $index")
        log.info("jumpTrack - queue size = ${queue.size}")

        // 인덱스가 유효한 범위 내에 있는지 확인
        if (index > 0 && index <= queue.size) {
            // 만약 repeat가 true면 그 사이의 값을 복사해야 함
            if (repeat) {
                // 0부터 index-2까지 값을 queue에 복사
                val tempQueue: BlockingQueue<AudioTrack> = LinkedBlockingQueue()
                val track = queue.elementAt(index - 1)
                tempQueue.add(track)
                for (i in 0 until index - 1) {
                    tempQueue.add(queue.elementAt(i).makeClone())
                }
            } else {
                val track = queue.elementAt(index - 1)
                queue.remove(track)
                // 다음 트랙으로 이동
                player.startTrack(track.makeClone(), false)
            }

        }
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
        // logging endReason
        log.info("track: ${track}, onTrackEnd: $endReason")
        if (endReason == AudioTrackEndReason.REPLACED) {
            log.info("Audio is REPLACED")
            return
        } else if (endReason == AudioTrackEndReason.STOPPED) {
            log.info("Audio is STOPPED")
            return
        } else if (endReason == AudioTrackEndReason.FINISHED ||
            endReason == AudioTrackEndReason.LOAD_FAILED
        ) {
            if (track != null) {
                val channel = track.userData as TextChannel
                PlayerManager.INSTANCE.next(channel)
            }
            return
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
        nextTrack()
    }

    fun prevTrack(): Boolean {
        log.info("prevTrack - queue size = ${queue.size}")
        if (lastTrack == null) {
            player.stopTrack()
            return false
        }
        val newQueue = LinkedBlockingQueue<AudioTrack>()
        newQueue.offer(lastTrack)
        newQueue.addAll(queue)
        return player.startTrack(newQueue.first().makeClone(), false)
    }
}
