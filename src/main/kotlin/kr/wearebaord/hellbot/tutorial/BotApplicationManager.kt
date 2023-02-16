package kr.wearebaord.hellbot.tutorial

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager
import com.sedmelluq.lava.common.tools.DaemonThreadFactory
import kr.wearebaord.hellbot.tutorial.controller.BotCommandMappingHandler
import kr.wearebaord.hellbot.tutorial.controller.BotControllerManager
import kr.wearebaord.hellbot.tutorial.music.MusicController
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class BotApplicationManager : ListenerAdapter() {
    private val guildContexts: MutableMap<Long, BotGuildContext?>
    private val controllerManager: BotControllerManager
    val playerManager: AudioPlayerManager
    val executorService: ScheduledExecutorService

    init {
        guildContexts = HashMap()
        controllerManager = BotControllerManager()
        controllerManager.registerController(MusicController.Factory())
        playerManager = DefaultAudioPlayerManager()
        //playerManager.useRemoteNodes("localhost:8080");
        playerManager.configuration.resamplingQuality = AudioConfiguration.ResamplingQuality.LOW
        playerManager.registerSourceManager(YoutubeAudioSourceManager())
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault())
        playerManager.registerSourceManager(BandcampAudioSourceManager())
        playerManager.registerSourceManager(VimeoAudioSourceManager())
        playerManager.registerSourceManager(TwitchStreamAudioSourceManager())
        playerManager.registerSourceManager(BeamAudioSourceManager())
        playerManager.registerSourceManager(HttpAudioSourceManager())
        playerManager.registerSourceManager(LocalAudioSourceManager())
        executorService = Executors.newScheduledThreadPool(1, DaemonThreadFactory("bot"))
    }

    private fun createGuildState(guildId: Long, guild: Guild): BotGuildContext {
        val context = BotGuildContext(guildId)
        for (controller in controllerManager.createControllers(this, context, guild)) {
            context.controllers[controller!!.javaClass] = controller
        }
        return context
    }

    @Synchronized
    private fun getContext(guild: Guild): BotGuildContext? {
        val guildId = guild.id.toLong()
        var context = guildContexts[guildId]
        if (context == null) {
            context = createGuildState(guildId, guild)
            guildContexts[guildId] = context
        }
        return context
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val member = event.member
        log.info("Received message from ${member?.user?.name}: ${event.message.contentDisplay}")
        if (!event.isFromType(ChannelType.TEXT) || member == null || member.user.isBot) {
            return
        }
        val guildContext = getContext(event.guild)
        controllerManager.dispatchMessage(
            guildContext!!.controllers,
            "!/",
            event.message,
            object : BotCommandMappingHandler {
                override fun commandNotFound(message: Message?, name: String?) {}
                override fun commandWrongParameterCount(
                    message: Message?,
                    name: String?,
                    usage: String?,
                    given: Int,
                    required: Int
                ) {
                    event.channel.sendMessage("Wrong argument count for command").queue()
                }

                override fun commandWrongParameterType(
                    message: Message?,
                    name: String?,
                    usage: String?,
                    index: Int,
                    value: String?,
                    expectedType: Class<*>?
                ) {
                    event.channel.sendMessage("Wrong argument type for command").queue()
                }

                override fun commandRestricted(message: Message?, name: String?) {
                    event.channel.sendMessage("Command not permitted").queue()
                }

                override fun commandException(message: Message, name: String?, throwable: Throwable?) {
                    event.channel.sendMessage("Command threw an exception").queue()
                    log.error("Command with content {} threw an exception.", message.contentDisplay, throwable)
                }
            })
    }

    override fun onGuildLeave(event: GuildLeaveEvent) {
        // do stuff
    }

    companion object {
        private val log = LoggerFactory.getLogger(BotApplicationManager::class.java)
    }
}