package kr.wearebaord.hellbot.tutorial.music

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.remote.RemoteNode
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.tools.PlayerLibrary
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import com.sedmelluq.discord.lavaplayer.tools.io.MessageOutput
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.DecodedTrackHolder
import com.sedmelluq.discord.lavaplayer.track.TrackMarker
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler.MarkerState
import kr.wearebaord.hellbot.tutorial.BotApplicationManager
import kr.wearebaord.hellbot.tutorial.BotGuildContext
import kr.wearebaord.hellbot.tutorial.MessageDispatcher
import kr.wearebaord.hellbot.tutorial.controller.BotCommandHandler
import kr.wearebaord.hellbot.tutorial.controller.BotController
import kr.wearebaord.hellbot.tutorial.controller.BotControllerFactory
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.managers.AudioManager
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

class MusicController(manager: BotApplicationManager, state: BotGuildContext?, guild: Guild) : BotController {
    private val log = LoggerFactory.getLogger(MusicController::class.java)
    private val manager: AudioPlayerManager?
    private val player: AudioPlayer
    private val outputChannel: AtomicReference<TextChannel>
    private val scheduler: MusicScheduler
    private val messageDispatcher: MessageDispatcher
    private val guild: Guild
    private val equalizer: EqualizerFactory

    init {
        this.manager = manager.playerManager
        this.guild = guild
        equalizer = EqualizerFactory()
        player = manager.playerManager.createPlayer()
        guild.audioManager.sendingHandler = AudioPlayerSendHandler(player)
        outputChannel = AtomicReference()
        messageDispatcher = GlobalDispatcher()
        scheduler = MusicScheduler(player, messageDispatcher, manager.executorService)
        player.addListener(scheduler)
    }

    @BotCommandHandler("add")
    private fun add(message: Message, identifier: String) {
        log.info("add command")
        addTrack(message, identifier, false)
    }

    @BotCommandHandler
    private fun now(message: Message, identifier: String) {
        addTrack(message, identifier, true)
    }

    @BotCommandHandler
    private fun hex(message: Message, pageCount: Int) {
        manager!!.source(YoutubeAudioSourceManager::class.java).setPlaylistPageCount(pageCount)
    }

    @BotCommandHandler
    @Throws(IOException::class)
    private fun serialize(message: Message) {
        val baos = ByteArrayOutputStream()
        val outputStream = MessageOutput(baos)
        for (track in scheduler.drainQueue()) {
            manager!!.encodeTrack(outputStream, track)
        }
        outputStream.finish()
        message.channel.sendMessage(Base64.getEncoder().encodeToString(baos.toByteArray())).queue()
    }

    @BotCommandHandler
    @Throws(IOException::class)
    private fun deserialize(message: Message, content: String) {
        outputChannel.set(message.channel as TextChannel)
        connectToFirstVoiceChannel(guild.audioManager)
        val bytes = Base64.getDecoder().decode(content)
        val inputStream = MessageInput(ByteArrayInputStream(bytes))
        var holder: DecodedTrackHolder
        while (manager!!.decodeTrack(inputStream).also { holder = it } != null) {
            if (holder.decodedTrack != null) {
                scheduler.addToQueue(holder.decodedTrack)
            }
        }
    }

    @BotCommandHandler
    private fun eqsetup(message: Message) {
        manager!!.configuration.isFilterHotSwapEnabled = true
        player.setFrameBufferDuration(500)
    }

    @BotCommandHandler
    private fun eqstart(message: Message) {
        player.setFilterFactory(equalizer)
    }

    @BotCommandHandler
    private fun eqstop(message: Message) {
        player.setFilterFactory(null)
    }

    @BotCommandHandler
    private fun eqband(message: Message, band: Int, value: Float) {
        equalizer.setGain(band, value)
    }

    @BotCommandHandler
    private fun eqhighbass(message: Message, diff: Float) {
        for (i in BASS_BOOST.indices) {
            equalizer.setGain(i, BASS_BOOST[i] + diff)
        }
    }

    @BotCommandHandler
    private fun eqlowbass(message: Message, diff: Float) {
        for (i in BASS_BOOST.indices) {
            equalizer.setGain(i, -BASS_BOOST[i] + diff)
        }
    }

    @BotCommandHandler
    private fun volume(message: Message, volume: Int) {
        player.volume = volume
    }

    @BotCommandHandler
    private fun nodes(message: Message, addressList: String) {
        manager!!.useRemoteNodes(*addressList.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
    }

    @BotCommandHandler
    private fun local(message: Message) {
        manager!!.useRemoteNodes()
    }

    @BotCommandHandler
    private fun skip(message: Message) {
        scheduler.skip()
    }

    @BotCommandHandler
    private fun forward(message: Message, duration: Int) {
        forPlayingTrack(object : TrackOperation {
            override fun execute(track: AudioTrack?) {
                track!!.position = track.position + duration
            }
        })
    }

    @BotCommandHandler
    private fun back(message: Message, duration: Int) {
        forPlayingTrack(object : TrackOperation {
            override fun execute(track: AudioTrack?) {
                track!!.position = Math.max(0, track.position - duration)
            }
        })
    }

    @BotCommandHandler
    private fun pause(message: Message) {
        player.isPaused = true
    }

    @BotCommandHandler
    private fun resume(message: Message) {
        player.isPaused = false
    }

    @BotCommandHandler
    private fun duration(message: Message) {
        forPlayingTrack(object : TrackOperation {
            override fun execute(track: AudioTrack?) {
                message.channel.sendMessage("Duration is " + track!!.duration).queue()
            }
        })
    }

    @BotCommandHandler
    private fun seek(message: Message, position: Long) {
        forPlayingTrack(object : TrackOperation {
            override fun execute(track: AudioTrack?) {
                track!!.position = position
            }
        })
    }

    @BotCommandHandler
    private fun pos(message: Message) {
        forPlayingTrack(object : TrackOperation {
            override fun execute(track: AudioTrack?) {
                message.channel.sendMessage("Position is " + track!!.position).queue()
            }
        })
    }

    @BotCommandHandler
    private fun marker(message: Message, position: Long, text: String) {
        forPlayingTrack(object : TrackOperation {
            override fun execute(track: AudioTrack?) {
                track!!.setMarker(TrackMarker(position) { state: MarkerState ->
                    message.channel.sendMessage(
                        "Trigger [" + text + "] cause [" + state.name + "]"
                    ).queue()
                })
            }
        })
    }

    @BotCommandHandler
    private fun unmark(message: Message) {
        forPlayingTrack(object : TrackOperation {
            override fun execute(track: AudioTrack?) {
                track!!.setMarker(null)
            }
        })
    }

    @BotCommandHandler
    private fun version(message: Message) {
        message.channel.sendMessage(PlayerLibrary.VERSION).queue()
    }

    @BotCommandHandler
    private fun nodeinfo(message: Message) {
        for (node in manager!!.remoteNodeRegistry.nodes) {
            val report = buildReportForNode(node)
            message.channel.sendMessage(report).queue()
        }
    }

    @BotCommandHandler
    private fun provider(message: Message) {
        forPlayingTrack(object : TrackOperation {
            override fun execute(track: AudioTrack?) {
                val node = manager!!.remoteNodeRegistry.getNodeUsedForTrack(track)
                if (node != null) {
                    message.channel.sendMessage("Node " + node.address).queue()
                } else {
                    message.channel.sendMessage("Not played by a remote node.").queue()
                }
            }
        })
    }

    @BotCommandHandler
    private fun leave(message: Message) {
        guild.audioManager.closeAudioConnection()
    }

    private fun buildReportForNode(node: RemoteNode): String {
        val builder = StringBuilder()
        builder.append("--- ").append(node.address).append(" ---\n")
        builder.append("Connection state: ").append(node.connectionState).append("\n")
        val statistics = node.lastStatistics
        builder.append("Node global statistics: \n").append(if (statistics == null) "unavailable" else "")
        if (statistics != null) {
            builder.append("   playing tracks: ").append(statistics.playingTrackCount).append("\n")
            builder.append("   total tracks: ").append(statistics.totalTrackCount).append("\n")
            builder.append("   system CPU usage: ").append(statistics.systemCpuUsage).append("\n")
            builder.append("   process CPU usage: ").append(statistics.processCpuUsage).append("\n")
        }
        builder.append("Minimum tick interval: ").append(node.tickMinimumInterval).append("\n")
        builder.append("Tick history capacity: ").append(node.tickHistoryCapacity).append("\n")
        val ticks = node.getLastTicks(false)
        builder.append("Number of ticks in history: ").append(ticks.size).append("\n")
        if (ticks.size > 0) {
            val tail = Math.min(ticks.size, 3)
            builder.append("Last ").append(tail).append(" ticks:\n")
            for (i in ticks.size - tail until ticks.size) {
                val tick = ticks[i]
                builder.append("   [duration ").append(tick.endTime - tick.startTime).append("]\n")
                builder.append("   start time: ").append(tick.startTime).append("\n")
                builder.append("   end time: ").append(tick.endTime).append("\n")
                builder.append("   response code: ").append(tick.responseCode).append("\n")
                builder.append("   request size: ").append(tick.requestSize).append("\n")
                builder.append("   response size: ").append(tick.responseSize).append("\n")
            }
        }
        val tracks = node.playingTracks
        builder.append("Number of playing tracks: ").append(tracks.size).append("\n")
        if (tracks.size > 0) {
            val head = Math.min(tracks.size, 3)
            builder.append("First ").append(head).append(" tracks:\n")
            for (i in 0 until head) {
                val track = tracks[i]
                builder.append("   [identifier ").append(track.info.identifier).append("]\n")
                builder.append("   name: ").append(track.info.author).append(" - ").append(track.info.title)
                    .append("\n")
                builder.append("   progress: ").append(track.position).append(" / ").append(track.duration).append("\n")
            }
        }
        builder.append("Balancer penalties: ").append(tracks.size).append("\n")
        for ((key, value) in node.balancerPenaltyDetails) {
            builder.append("   ").append(key).append(": ").append(value).append("\n")
        }
        return builder.toString()
    }

    private fun addTrack(message: Message, identifier: String, now: Boolean) {
        outputChannel.set(message.channel as TextChannel)
        manager!!.loadItemOrdered(this, identifier, object : AudioLoadResultHandler {
            override fun trackLoaded(track: AudioTrack) {
                connectToFirstVoiceChannel(guild.audioManager)
                message.channel.sendMessage("Starting now: " + track.info.title + " (length " + track.duration + ")")
                    .queue()
                if (now) {
                    scheduler.playNow(track, true)
                } else {
                    scheduler.addToQueue(track)
                }
            }

            override fun playlistLoaded(playlist: AudioPlaylist) {
                val tracks = playlist.tracks
                message.channel.sendMessage("Loaded playlist: " + playlist.name + " (" + tracks.size + ")").queue()
                connectToFirstVoiceChannel(guild.audioManager)
                var selected = playlist.selectedTrack
                if (selected != null) {
                    message.channel.sendMessage("Selected track from playlist: " + selected.info.title).queue()
                } else {
                    selected = tracks[0]
                    message.channel.sendMessage("Added first track from playlist: " + selected.info.title).queue()
                }
                if (now) {
                    scheduler.playNow(selected, true)
                } else {
                    scheduler.addToQueue(selected)
                }
                for (i in 0 until Math.min(10, playlist.tracks.size)) {
                    if (tracks[i] !== selected) {
                        scheduler.addToQueue(tracks[i])
                    }
                }
            }

            override fun noMatches() {
                message.channel.sendMessage("Nothing found for $identifier").queue()
            }

            override fun loadFailed(throwable: FriendlyException) {
                message.channel.sendMessage("Failed with message: " + throwable.message + " (" + throwable.javaClass.simpleName + ")")
                    .queue()
            }
        })
    }

    private fun forPlayingTrack(operation: TrackOperation) {
        val track = player.playingTrack
        if (track != null) {
            operation.execute(track)
        }
    }

    private interface TrackOperation {
        fun execute(track: AudioTrack?)
    }

    private inner class GlobalDispatcher : MessageDispatcher {
        override fun sendMessage(message: String?, success: Consumer<Message?>, failure: Consumer<Throwable?>?) {
            val channel = outputChannel.get()
            channel?.sendMessage(message!!)?.queue(success, failure)
        }

        override fun sendMessage(message: String?) {
            val channel = outputChannel.get()
            channel?.sendMessage(message!!)?.queue()
        }
    }

    private inner class FixedDispatcher private constructor(private val channel: TextChannel) : MessageDispatcher {
        override fun sendMessage(message: String?, success: Consumer<Message?>, failure: Consumer<Throwable?>?) {
            channel.sendMessage(message!!).queue(success, failure)
        }

        override fun sendMessage(message: String?) {
            channel.sendMessage(message!!).queue()
        }
    }

    class Factory : BotControllerFactory<MusicController> {
        override val controllerClass: Class<MusicController>
            get() = MusicController::class.java

        override fun create(manager: BotApplicationManager?, state: BotGuildContext?, guild: Guild?): MusicController {
            return MusicController(manager!!, state, guild!!)
        }
    }

    companion object {
        private val BASS_BOOST = floatArrayOf(
            0.2f, 0.15f, 0.1f, 0.05f, 0.0f, -0.05f, -0.1f, -0.1f, -0.1f, -0.1f, -0.1f,
            -0.1f, -0.1f, -0.1f, -0.1f
        )

        private fun connectToFirstVoiceChannel(audioManager: AudioManager) {
            if (!audioManager.isConnected) {
                for (voiceChannel in audioManager.guild.voiceChannels) {
                    if ("헬봇테스트" == voiceChannel.name) {
                        audioManager.openAudioConnection(voiceChannel)
                        return
                    }
                }
                for (voiceChannel in audioManager.guild.voiceChannels) {
                    audioManager.openAudioConnection(voiceChannel)
                    return
                }
            }
        }
    }
}