package kr.wearebaord.hellbot.commands

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate
import io.github.jdiscordbots.command_framework.command.Command
import io.github.jdiscordbots.command_framework.command.CommandEvent
import io.github.jdiscordbots.command_framework.command.ICommand
import io.github.jdiscordbots.command_framework.command.text.MessageArgument
import kr.wearebaord.hellbot.joinVoiceChannelBot
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException


@Command("play", "p", "ㅔ")
@Command
class PlayCommand : ICommand {
    private val log = LoggerFactory.getLogger(PlayCommand::class.java)

    override fun action(event: CommandEvent) {
        val channel = event.channel
        // self member
        val self = event.guild!!.selfMember
        val selfVoiceState = self!!.voiceState

        log.info("event.message.contentRaw : ${event.message.contentRaw}")
        log.info("event.message.contentDisplay : ${event.message.contentDisplay}")
        log.info("event.message.contentStripped : ${event.message.contentStripped}")

        if (event.args.isEmpty()) {
            log.info("play command args is empty")
            channel.sendMessage("사용법: !!p title or link").queue()
            return
        }

        val member = event.member
        val memberVoiceState = member!!.voiceState
        log.info("member : $member")
        log.info("memberVoiceState: $memberVoiceState")

        if (!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("음성채널에 들어가주세요.").queue()
            return
        }

        if (!selfVoiceState!!.inAudioChannel()) {
            joinVoiceChannelBot(event.channel, event.member!!, event.guild!!)
        }

//        val url = event.args
        // loging args
        val args: List<MessageArgument> = event.args as List<MessageArgument>
        var url = args.joinToString(" ")
        log.info("url: $url")
        if (!isUrl(url)) {
            log.info("url is not url, ytsearch")
            url = "ytsearch:$url"
        }

        try{
            PlayerManager.INSTANCE
//            .loadAndPlay(channel as TextChannel, "https://www.youtube.com/watch?v=dmSUBdk4SY4")
                .loadAndPlay(channel as TextChannel, url)
        }catch (e: Exception) {
            e.printStackTrace()
            log.error("error: ${e.message}")
        }finally {
            // delete command message
            event.message.delete().queue()
        }

    }

    override fun help(): String {
        return "유튜브 링크를 재생합니다."

    }

    /**
     * gets a list of all parameters the command expects.
     * The expected parameters should not change.
     * The order of arguments is preserved.
     * @return A [List] containing all argument the command expects
     */
    override fun getExpectedArguments(): MutableList<ArgumentTemplate> {
        return mutableListOf(
//          ArgumentTemplate("url", "유튜브 링크를 입력해주세요.", true, null)
        )
    }

    override fun allowExecute(event: CommandEvent): Boolean {
        return super.allowExecute(event)
    }


    private fun isUrl(url: String): Boolean {
        // check if url is valid
        try{
            // is url
            URI(url)
            // check start with https://www.youtube.com/watch?v=
            if (!url.startsWith("https://www.youtube.com/watch?v=")) {
                throw URISyntaxException(url, "url is not youtube url")
            }
                return true
        }catch (e: URISyntaxException){
            log.error("Uri is not valid! ${e.message}")
            return false
        }
    }
}