package kr.wearebaord.hellbot.music

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

class PlayerManager {

    private val musicManagers: HashMap<Long, GuildMusicManager> = HashMap()
    private val audioPlayerManager: AudioPlayerManager = DefaultAudioPlayerManager()

    init {
        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager)
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager)
    }

    private fun getMusicManager(guild: Guild): GuildMusicManager {
        return this.musicManagers.computeIfAbsent(guild.idLong) {
            val guildMusicManagers = GuildMusicManager(this.audioPlayerManager)
            guild.audioManager.sendingHandler = guildMusicManagers.getSendHandler()
            guildMusicManagers
        }
    }

    fun loadAndPlay(channel: TextChannel, trackUrl: String): Unit{
        val musicManager = this.getMusicManager(channel.guild).scheduler
        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                try{
                    musicManager.queue(track)
                    channel.sendMessage("Adding to queue: `")
                        .addContent(track.info.title)
                        .addContent("` by `")
                        .addContent(track.info.author)
                        .queue()
                }catch (e: Exception){
                    e.printStackTrace()
                }

            }

            override fun playlistLoaded(playlist: com.sedmelluq.discord.lavaplayer.track.AudioPlaylist) {
                val tracks = playlist.tracks

                channel.sendMessage("Adding to queue: `")
                    .addContent(tracks[0].info.title)
                    .addContent("` by `")
                    .addContent(tracks[0].info.author)
                    .addContent("` and ")
                    .addContent((tracks.size - 1).toString())
                    .addContent(" more tracks")
                    .queue()
            }

            override fun noMatches() {
                channel.sendMessage("No matches found for $trackUrl").queue()
            }

            /**
             * Called when loading an item failed with an exception.
             * @param exception The exception that was thrown
             */
            override fun loadFailed(exception: FriendlyException?) {
                TODO("Not yet implemented")
            }
        })
    }

    companion object{
        // PlayerManager to SingleTon
        val INSTANCE: PlayerManager = PlayerManager()
    }
}