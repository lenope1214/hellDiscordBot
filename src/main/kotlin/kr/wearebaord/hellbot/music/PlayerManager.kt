package kr.wearebaord.hellbot.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kr.wearebaord.hellbot.TEXT_CHANNEL_NAME
import kr.wearebaord.hellbot.common.leaveBot
import kr.wearebaord.hellbot.common.sendEmbed
import kr.wearebaord.hellbot.common.sendYoutubeEmbed
import kr.wearebaord.hellbot.exception.MusicTitleIsNullException
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.slf4j.LoggerFactory

class PlayerManager {
    private val log = LoggerFactory.getLogger(PlayerManager::class.java)
    private val musicManagers: HashMap<Long, GuildMusicManager> = HashMap()
    private val audioPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    var trackHash: HashMap<Long, MutableList<PlayTrackInfo>> = HashMap()

    init {
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager)
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager)
    }

    private fun getMusicManager(guild: Guild): GuildMusicManager {
        return this.musicManagers.computeIfAbsent(guild.idLong) {
            val guildMusicManagers = GuildMusicManager(this.audioPlayerManager)
            guild.audioManager.sendingHandler = guildMusicManagers.sendHandler
            guildMusicManagers
        }
    }

    fun loadAndPlay(channel: TextChannel, trackUrl: String, addedBy: Member): Unit {
        log.info("loadAndPlay: $trackUrl")
        val musicManager = this.getMusicManager(channel.guild).scheduler
        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                log.info("trackLoaded: ${track.info.title} (${track.info.uri})")
                try {
                    if (track.info.title == null) {
                        throw MusicTitleIsNullException()
                    }
                    track.userData = channel

                    val isAddedQueue = musicManager.addQueue(track)
                    if (isAddedQueue) {
                        plus(channel, track, addedBy)
                    } else {
                        channel.sendEmbed("노래 정보를 불러오는데에 실패했습니다. 다시 시도해주세요.")
                    }
                } catch (e: Exception) {
                    log.error("trackLoaded error: ${e.message}")
                    e.printStackTrace()
                }

            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val tracks = playlist.tracks
                trackLoaded(tracks[0])
            }

            override fun noMatches() {
                log.info("재생할 영상(노래)을 찾을 수 없습니다.: $trackUrl")
                channel.sendMessage("No matches found for $trackUrl").queue()
            }

            /**
             * Called when loading an item failed with an exception.
             * @param e The exception that was thrown
             */
            override fun loadFailed(e: FriendlyException) {
                e.printStackTrace()
                log.error("loadFailed: ${e.message}")
                channel.sendMessage("재생 불가능한 노래입니다.").queue()
            }
        })
    }

    fun updateMessage(channel: TextChannel) {
        // 기존 버튼을 수정?

    }

    fun sendMessage(channel: TextChannel) {
        if(trackHash[channel.guild.idLong]?.size == 0) return
        // track의 첫 번째 정보를 가져와 embed를 만든다
        val firstTrack = trackHash[channel.guild.idLong]!!.first()!!.track!!

        val guild = channel.guild
        val musicManager = getMusicManager(guild).scheduler
        val pause = musicManager.isPause()
        val repeat = musicManager.isRepeat()

        trackHash.forEach { log.info("trackHash - key: ${it.key}, value: ${it.value}") }
        channel.sendYoutubeEmbed(
            url = firstTrack.info.uri,
            title = firstTrack.info.title,
            author = firstTrack.info.author,
            duration = firstTrack.duration,
            youtubeIdentity = firstTrack.identifier,
            playTrackInfoList = trackHash[channel.guild.idLong] ?: listOf(),
            isPause = pause,
            isRepeat = repeat,
        )
    }

    fun plus(channel: TextChannel, track: AudioTrack, addedBy : Member) {
        val guild = channel.guild

        // 없으면 새로운 리스트를 만들어 사용할 수 있게 한다.
        if (trackHash[guild.idLong] == null) {
            trackHash[guild.idLong] = mutableListOf()
        }

        trackHash[guild.idLong]!!.add(PlayTrackInfo(
            track = track,
            addedBy = addedBy,
        ))
        log.info("[노래 추가 됨] ${addedBy}에 의해 추가 된 노래 정보 - ${track.info.title} (${track.info.uri})")
        sendMessage(channel)
    }

    /**
     * 만약 다음 곡을 재생했다면 true, 종료됐다면 false
     */
    fun next(channel: TextChannel): Boolean {

        val guild = channel.guild
        var tracks: MutableList<AudioTrack> = trackHash[guild.idLong]?.map { it.track }?.toMutableList() ?: return false
        log.info("trackHash - tracks.size : ${tracks?.size}")

        val musicManager = getMusicManager(guild).scheduler
        return if (!musicManager.isRepeat() && (tracks == null || tracks.size == 1)) {
            stop(channel)
            false
        } else {
            musicManager.nextTrack()

            // 반복재생 중이라면 맨 앞 곡을 맨 뒤에 추가
            if (musicManager.isRepeat()) {
                log.info("======= 반복 중이므로 맨 앞 곡을 맨 뒤에 추가 =======")
                tracks?.let {
                    log.info("track info : ${it[0].info.title}")
                    if (it.isNotEmpty()) {
                        val firstTrack = it[0]
                        it.add(firstTrack)
                        // set last track
                        musicManager.updateLastTrack(firstTrack)
                    }
                }
                log.info("==================================================")
            }

            // 큐의 맨 처음 트랙정보 제거
            trackHash[guild.idLong] = trackHash[guild.idLong]!!.drop(1).toMutableList()
            sendMessage(channel)
            true
        }
    }

    fun jumpTo(channel: TextChannel, index: Int) {
        val guild = channel.guild
        val tracks = trackHash[guild.idLong]?.map { it.track }?.toMutableList()
        val musicManager = getMusicManager(guild).scheduler
        musicManager.jumpTrack(index)
        // 1개 남았을 때도 스킵되면 없으므로 종료되어야 함.
        // 점프할 인덱스가 전체 개수보다 많으면 종료되어야 함.
        if (tracks == null || tracks.size == 1 || tracks.size <= index) {
            stop(channel)
        } else {
            // index만큼 제거 -> jump와 같음
            trackHash[guild.idLong] = trackHash[guild.idLong]!!.drop(index).toMutableList()
            println("tracks.size : ${trackHash[guild.idLong]?.size}")
            sendMessage(channel)
        }
    }

    fun stop(
        channel: TextChannel,
        stopBy: String = "",
    ) {
        log.info("resetTrack")
        val guild = channel.guild
        trackHash[guild.idLong] = mutableListOf()
        val musicManager = getMusicManager(guild).scheduler
        musicManager.updateLastTrack(null)
        channel.sendEmbed(
            title = "재생이 종료되었습니다.",
            description = "${
                if (stopBy.isNotBlank()) {
                    "${stopBy}에 의해 종료되었습니다."
                } else ""
            }\n재생목록을 다시 추가해주세요.",
            author = TEXT_CHANNEL_NAME,
        )
        Thread {
            Thread.sleep(60 * 1000 ) // 60 초 뒤에 나가게 함
            // 이때 만약 다른 사람이 노래를 추가했다면 나가지 않음
            // 종료되고 아무것도 추가 안 했을때 size를 확인해봐야 함
            if(trackHash[guild.idLong]?.size ?: 0 > 0) {
                log.info("다른 사람이 노래를 추가했으므로 나가지 않음")
                return@Thread
            }
            leftChannel(guild)
        }.start()
    }

    fun resume(textChannel: TextChannel) {
        val guild = textChannel.guild
        val musicManager = getMusicManager(guild)
        musicManager.scheduler.player.isPaused = false
        musicManager.scheduler.doNotPause()
//        sendMessage(textChannel)
    }

    fun pause(textChannel: TextChannel) {
        val guild = textChannel.guild
        val musicManager = getMusicManager(guild)
        musicManager.scheduler.player.isPaused = true
        musicManager.scheduler.doPause()
        // 기존 pauseButton을 playButton으로 바꾸는 방식으로 변경
//        sendMessage(textChannel)
    }

    fun prevTrack(textChannel: TextChannel) {
        val guild = textChannel.guild
        val musicManager = getMusicManager(guild)
        val isPrevTrack = musicManager.scheduler.prevTrack()
        if (!isPrevTrack) {
            sendMessage(textChannel)
            return
        }
        textChannel.sendEmbed(
            title = "이전 곡으로 돌아갑니다.",
            description = "이전 곡으로 돌아갑니다.",
            author = TEXT_CHANNEL_NAME,
        )
    }

    fun repeat(textChannel: TextChannel) {
        log.info("playerManager - repeat")
        val guild = textChannel.guild
        val musicManager = getMusicManager(guild)
        val scheduler = musicManager.scheduler
        scheduler.let {
            if (it.isRepeat()) it.doNotRepeat()
            else it.doRepeat()
        }
    }

    fun leftChannel(guild: Guild, channel: TextChannel? = null) {
        leaveBot(guild, channel)
    }

    companion object {
        // PlayerManager to SingleTon

        var INSTANCE: PlayerManager = PlayerManager()
            get(){
                if(field == null) field = PlayerManager()
                return field
            }
    }
}