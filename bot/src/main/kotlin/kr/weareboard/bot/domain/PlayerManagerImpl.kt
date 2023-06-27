package kr.weareboard.bot.domain

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kr.weareboard.bot.exception.MusicTitleIsNullException
import kr.weareboard.bot.music.GuildMusicManager
import kr.weareboard.bot.music.PlayTrackInfo
import kr.weareboard.bot.service.interfaces.BotService
import kr.weareboard.bot.service.interfaces.TextChannelService
import kr.weareboard.domain.entity.music.service.MusicHistoryService
import kr.weareboard.main.TEXT_CHANNEL_NAME
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PlayerManagerImpl(
    private val guildMusicManager: GuildMusicManager,
    private val botService: BotService,
    private val textChannelService: TextChannelService,
    private val musicHistoryService: MusicHistoryService
) : PlayerManager {
    private val log = LoggerFactory.getLogger(PlayerManagerImpl::class.java)
    private val musicManagers: HashMap<Long, GuildMusicManager> = HashMap()
    private val audioPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private var channelHash: HashMap<Long, ChannelInfo> = HashMap()

    fun getChannelHash(idLong: Long): ChannelInfo {
        var channelInfo = channelHash[idLong]
        if (channelInfo == null) {
            channelInfo = ChannelInfo()
        }
        return channelInfo
    }

    init {
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager)
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager)
    }

    override fun getMusicManager(guild: Guild): GuildMusicManager {
        return this.musicManagers.computeIfAbsent(guild.idLong) {
            guildMusicManager.init(this.audioPlayerManager)
            guild.audioManager.sendingHandler = guildMusicManager.sendHandler
            guildMusicManager
        }
    }

    override fun loadAndPlay(
        guild: Guild,
        channel: TextChannel,
        trackUrl: String,
        addedBy: Member,
        isYoutubeSearch: Boolean
    ) {
        log.info("loadAndPlay: $trackUrl")
        val musicManager = this.getMusicManager(channel.guild).scheduler
        this.audioPlayerManager.loadItemOrdered(
            musicManager,
            trackUrl,
            object : AudioLoadResultHandler {
                override fun trackLoaded(track: AudioTrack) {
                    log.info("trackLoaded: ${track.info.title} (${track.info.uri})")
                    try {
                        if (track.info.title == null) {
                            throw MusicTitleIsNullException()
                        }

                        val isAddedQueue = musicManager.addQueue(track)
                        if (isAddedQueue) {
                            plus(channel, listOf(track), addedBy)
                        } else {
                            textChannelService.sendEmbed(
                                channel = channel,
                                title = "노래 정보를 불러오는데에 실패했습니다. 다시 시도해주세요."
                            )
                        }
                    } catch (e: Exception) {
                        log.error("trackLoaded error: ${e.message}")
                        e.printStackTrace()
                    }
                }

                override fun playlistLoaded(playlist: AudioPlaylist) {
                    // 유튜브 검색 일 시 하나만 추가
                    log.info("============================> 유튜브 검색인가요? : $isYoutubeSearch")
                    if (isYoutubeSearch) {
                        trackLoaded(playlist.tracks[0])
                        return
                    }

                    val tracks = playlist.tracks
                    log.info("playlistLoaded: ${playlist.name} with ${tracks.size} tracks")
                    plus(channel, tracks, addedBy)
                    playlist.tracks.forEach {
                        musicManager.addQueue(it)
                    }
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
            }
        )
    }

    fun updateLastEmbedMessage(channel: TextChannel) {
        // 기존 embed message 수정?
    }

    override fun sendMessage(channel: TextChannel) {
        if (channelHash[channel.guild.idLong]!!.tracks.size == 0) return
        // track의 첫 번째 정보를 가져와 embed를 만든다
        val firstTrack = channelHash[channel.guild.idLong]!!.tracks.first().track

        val guild = channel.guild
        val musicManager = getMusicManager(guild).scheduler
        val pause = musicManager.isPause()
        val repeat = musicManager.isRepeat()

        channelHash.forEach { log.info("trackHash - key: ${it.key}, value: ${it.value}") }

        textChannelService.sendYoutubeEmbed(
            channel = channel,
            url = firstTrack.info.uri,
            title = firstTrack.info.title,
            author = firstTrack.info.author,
            duration = firstTrack.duration,
            youtubeIdentity = firstTrack.identifier,
            playTrackInfoList = channelHash[channel.guild.idLong]!!.tracks,
            isPause = pause,
            isRepeat = repeat
        )
    }

    override fun plus(
        channel: TextChannel,
        tracks: List<AudioTrack>,
        addedBy: Member
    ) {
        val guild = channel.guild

        // 없으면 새로운 리스트를 만들어 사용할 수 있게 한다.
        if (channelHash[guild.idLong] == null) {
            channelHash[guild.idLong] = ChannelInfo()
        }

        tracks.forEach { track ->
            track.userData = channel
            channelHash[guild.idLong]!!.tracks.add(
                PlayTrackInfo(
                    track = track,
                    addedBy = addedBy
                )
            )

            musicHistoryService.addHistory(
                guildId = guild.id,
                memberId = addedBy.id,
                trackIdentifier = track.identifier,
                title = track.info.title,
                author = track.info.author,
                url = track.info.uri,
                time = track.info.length
            )
        }

        log.info(
            """
            [ 현재 플레이 리스트 ]
            ${channelHash[guild.idLong]!!.tracks.map { it.track.info.title }}
            """.trimIndent()
        )
        sendMessage(channel)
    }

    override fun next(channel: TextChannel, nextedBy: Member?): Boolean {
        val guild = channel.guild
        var tracks: MutableList<AudioTrack> =
            channelHash[guild.idLong]!!.tracks
                .map { it.track }
                .toMutableList()
        log.info("trackHash - tracks.size : ${tracks.size}")

        val musicManager = getMusicManager(guild).scheduler
        return if (!musicManager.isRepeat() && (tracks.size == 1)) {
            stop(channel, nextedBy)
            false
        } else {
            // 반복재생 중이라면 맨 앞 곡을 맨 뒤에 추가
            if (musicManager.isRepeat()) {
                log.info("======= 반복 중이므로 맨 앞 곡을 맨 뒤에 추가 (트랙 수 : ${tracks.size}=======")
                if (tracks.isNotEmpty()) {
                    log.info("track info : ${tracks[0].info.title}")
                    val firstTrack = tracks[0]
                    // 반복이므로 기존 정보를 그대로 넣어야 함
                    val addedBy = channelHash[guild.idLong]!!.tracks[0].addedBy
                    plus(channel, listOf(firstTrack), addedBy)
                    log.info(
                        """
                        [ 현재 플레이 리스트(${tracks.size}) ]
                        ${tracks.map { it.info.title }}
                        """.trimIndent()
                    )
                    // set last track
                    musicManager.updateLastTrack(firstTrack)
                }
            }
            log.info("==================================================")
            // queue의 맨 앞 곡을 재생, 이 함수가 먼저 나오면 플레이리스트에 정상적으로 추가되지 않는다
            musicManager.nextTrack()

            // 큐의 맨 처음 트랙정보 제거
            channelHash[guild.idLong]!!.tracks = channelHash[guild.idLong]!!.tracks.drop(1).toMutableList()
            sendMessage(channel)
            true
        }
    }

    override fun jumpTo(
        channel: TextChannel,
        index: Int,
        jumpedBy: Member?
    ) {
        val guild = channel.guild
        val tracks = channelHash[guild.idLong]!!.tracks.map { it.track }
        val musicManager = getMusicManager(guild).scheduler
        musicManager.jumpTrack(index)
        // 1개 남았을 때도 스킵되면 없으므로 종료되어야 함.
        // 점프할 인덱스가 전체 개수보다 많으면 종료되어야 함.
        if (tracks.size == 1 || tracks.size <= index) {
            stop(channel, jumpedBy)
        } else {
            // index만큼 제거 -> jump와 같음
            channelHash[guild.idLong]!!.tracks = channelHash[guild.idLong]!!.tracks.drop(index).toMutableList()
            println("tracks.size : ${channelHash[guild.idLong]!!.tracks.size}")
            sendMessage(channel)
        }
    }

    override fun stop(
        channel: TextChannel,
        stopedBy: Member?
    ) {
        log.info("resetTrack")
        val guild = channel.guild
        val channelInfo = channelHash[guild.idLong]!!
        val musicManager = getMusicManager(guild).scheduler

        musicManager.reset()
        channelInfo.reset()

        var stopedByMessage = ""
        // stopedBy?.effectiveName가 null이면 "" 아니면 stopedBy?.effectiveName에 의해를 추가
        log.info("stopedBy?.nickname : ${stopedBy?.nickname}")
        log.info("stopedBy?.effectiveName : ${stopedBy?.effectiveName}")
        if (stopedBy?.nickname != null) {
            stopedByMessage = "${stopedBy.nickname}에 의해 "
        }

        textChannelService.sendEmbed(
            channel = channel,
            title = "재생이 종료되었습니다.",
            description = "$stopedByMessage 재생이 종료되었습니다.\n노래 제목을 검색해보세요!",
            author = TEXT_CHANNEL_NAME
        )

        Thread {
            Thread.sleep(300 * 1000) // 300 초 뒤에 나가게 함
            // 이때 만약 다른 사람이 노래를 추가했다면 나가지 않음
            // 종료되고 아무것도 추가 안 했을때 size를 확인해봐야 함
            if (channelInfo.tracks.size > 0) {
                log.info("다른 사람이 노래를 추가했으므로 나가지 않음")
                return@Thread
            }
            leftChannel(guild, channel)
        }.start()

        Thread {
            Thread.sleep(5 * 1000) // 5초 뒤에 기본 메세지를 보냄
            textChannelService.sendDefaultMessage(channel)
        }.start()
    }

    override fun resume(
        textChannel: TextChannel,
        resumedBy: Member?
    ) {
        val guild = textChannel.guild
        val musicManager = getMusicManager(guild)
        musicManager.scheduler.player.isPaused = false
        musicManager.scheduler.doNotPause()
//        sendMessage(textChannel)
    }

    override fun pause(
        textChannel: TextChannel,
        pausedBy: Member?
    ) {
        val guild = textChannel.guild
        val musicManager = getMusicManager(guild)
        musicManager.scheduler.player.isPaused = true
        musicManager.scheduler.doPause()
        // 기존 pauseButton을 playButton으로 바꾸는 방식으로 변경
//        sendMessage(textChannel)
    }

    override fun prevTrack(
        textChannel: TextChannel,
        prevTrackBy: Member?
    ) {
        val guild = textChannel.guild
        val musicManager = getMusicManager(guild)
        val isPrevTrack = musicManager.scheduler.prevTrack()
        if (!isPrevTrack) {
            sendMessage(textChannel)
            return
        }
        textChannelService.sendEmbed(
            channel = textChannel,
            title = "이전 곡으로 돌아갑니다.",
            description = "이전 곡으로 돌아갑니다.",
            author = TEXT_CHANNEL_NAME
        )
    }

    override fun repeat(
        textChannel: TextChannel,
        repeatBy: Member?
    ) {
        val guild = textChannel.guild
        val musicManager = getMusicManager(guild)
        val scheduler = musicManager.scheduler
        scheduler.let {
            it.turnRepeat()
            log.info("현재 repeat 상태 : ${it.isRepeat()}")
        }
    }

    override fun leftChannel(guild: Guild, channel: TextChannel) {
        botService.leaveBot(guild, channel)
    }
}
