package kr.wearebaord.hellbot.listeners.music

import kr.wearebaord.hellbot.BOT_VERSION
import kr.wearebaord.hellbot.TEXT_CHANNEL_NAME
import kr.wearebaord.hellbot.common.*
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory
import kr.wearebaord.hellbot.music.PlayerManager
import kr.wearebaord.hellbot.music.enums.EmojiValue
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericSelectMenuInteractionEvent

object PlayCommand: CommandInterface {
    val log = LoggerFactory.getLogger(PlayCommand::class.java)

    val commands: List<String> = listOf("p", "play", "ㅔ", "ㅔㅣ묘", "재생", "노래")


    override fun onAction(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        try{
            isValidContentRaw(raw, commands)
        }catch (e:IllegalArgumentException){
            return
        }
        val content = parseContent(raw)
        log.info("play command by ${event.member!!.effectiveName}")

        val channel = event.channel
        val member = event.member
        val memberVoiceState = member!!.voiceState

        if (!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("음성채널에 들어가주세요.").queue()
            return
        }

        play(event, content)
    }


    private fun play(event: MessageReceivedEvent, url: String) {
        val channel = event.channel
        // bot infomation
        val self = event.guild!!.selfMember
        val selfVoiceState = self!!.voiceState
        var url = url

        log.info("url: $url")
        if (!url.isYoutubeUrl()) {
            log.info("url is not url, ytsearch")
            url = "ytsearch:$url"
        }

        try {
            if (!selfVoiceState!!.inAudioChannel()) {
                joinVoiceChannelBot(event.channel, event.member!!, event.guild!!)
            }
            PlayerManager.INSTANCE
                .loadAndPlay(channel as TextChannel, url)
        } catch (e: Exception) {
            e.printStackTrace()
            log.error("error: ${e.message}")
        } finally {
            // delete command message
            //  event.message.delete().queue()
        }
    }
}