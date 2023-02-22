package kr.wearebaord.hellbot.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kr.wearebaord.hellbot.TEXT_CHANNEL_NAME
import kr.wearebaord.hellbot.common.sendEmbed
import kr.wearebaord.hellbot.common.sendYoutubeEmbed
import kr.wearebaord.hellbot.exception.MusicTitleIsNullException
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import org.slf4j.LoggerFactory

class PlayerManager {
    private val log = LoggerFactory.getLogger(PlayerManager::class.java)
    private val musicManagers: HashMap<Long, GuildMusicManager> = HashMap()
    private val audioPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()
    private val trackHash: HashMap<Long, List<AudioTrack>> = HashMap()

    init {
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager)
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager)
    }

    fun getMusicManager(guild: Guild): GuildMusicManager {
        return this.musicManagers.computeIfAbsent(guild.idLong) {
            val guildMusicManagers = GuildMusicManager(this.audioPlayerManager)
            guild.audioManager.sendingHandler = guildMusicManagers.sendHandler
            guildMusicManagers
        }
    }

    fun loadAndPlay(channel: TextChannel, trackUrl: String): Unit {
        log.info("loadAndPlay: $trackUrl")
        val musicManager = this.getMusicManager(channel.guild).scheduler
        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                try {
                    if(track.info.title == null){
                        throw MusicTitleIsNullException()
                    }
                    track.userData = track.info.title

                    musicManager.queue(track)

                    plus(channel, track)
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
                channel.sendMessage("No matches found for $trackUrl").queue()
            }

            /**
             * Called when loading an item failed with an exception.
             * @param exception The exception that was thrown
             */
            override fun loadFailed(exception: FriendlyException?) {
                channel.sendMessage("Could not play: $exception").queue()
            }
        })
    }

    fun sendMessage(channel: TextChannel) {
        // track의 첫 번째 정보를 가져와 embed를 만든다
        val firstTrack = trackHash[channel.guild.idLong]?.let {
            if (it.isNotEmpty()) it[0] else null
        } ?: return

        channel.sendYoutubeEmbed(
            url = firstTrack.info.uri,
            title = firstTrack.info.title,
            author = firstTrack.info.author,
            duration = firstTrack.duration,
            youtubeIdentity = firstTrack.identifier,
            tracks = trackHash[channel.guild.idLong]!!,
        )
    }

    fun plus(channel: TextChannel, track: AudioTrack) {
        val guild = channel.guild
        val trackNames = trackHash[guild.idLong]
        if (trackNames == null) {
            trackHash[guild.idLong] = listOf(track)
        } else {
            trackHash[guild.idLong] = trackNames.plus(track)
        }
        sendMessage(channel)
        log.info("addTrackName - result trackNames: $trackNames")
    }

    fun next(channel: TextChannel) {

        val guild = channel.guild
        val tracks = trackHash[guild.idLong]
        log.info("trackHash - tracks.size : ${tracks?.size}")

        val musicManager = getMusicManager(guild).scheduler
        musicManager.nextTrack()
        if (tracks == null || tracks.size == 1) { // 1개 남았을 때도 스킵되면 없으므로 종료되어야 함.
            reset(channel)
        } else {
            trackHash[guild.idLong] = tracks.drop(1)
            println("tracks.size : ${trackHash[guild.idLong]?.size}")
            sendMessage(channel)
        }
    }

    fun jumpTo(channel: TextChannel, index: Int) {
        val guild = channel.guild
        val tracks = trackHash[guild.idLong]
        val musicManager = getMusicManager(guild).scheduler
        musicManager.jumpTrack(index)
        // 1개 남았을 때도 스킵되면 없으므로 종료되어야 함.
        // 점프할 인덱스가 전체 개수보다 많으면 종료되어야 함.
        if (tracks == null || tracks.size == 1 || tracks.size <= index) {
            reset(channel)
        } else {
            trackHash[guild.idLong] = tracks.drop(index)
            println("tracks.size : ${trackHash[guild.idLong]?.size}")
            sendMessage(channel)
        }
    }

    fun reset(channel: TextChannel) {
        log.info("resetTrackNames")
        val guild = channel.guild
        trackHash[guild.idLong] = listOf()
        val musicManager = getMusicManager(guild)
        musicManager.scheduler.player.stopTrack()
        musicManager.scheduler.queue.clear()
        channel.sendEmbed(
            title = "재생목록이 비었습니다.",
            description = "재생목록을 다시 추가해주세요.",
            author = TEXT_CHANNEL_NAME,
        )
    }

    companion object {
        // PlayerManager to SingleTon
        val INSTANCE: PlayerManager = PlayerManager()
    }
}