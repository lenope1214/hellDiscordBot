package kr.wearebaord.hellbot.commands

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate
import io.github.jdiscordbots.command_framework.command.Command
import io.github.jdiscordbots.command_framework.command.CommandEvent
import io.github.jdiscordbots.command_framework.command.ICommand
import kr.wearebaord.hellbot.music.GuildMusicManager
import kr.wearebaord.hellbot.music.PlayerManager
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException


@Command("stop", "s", "ㄴ")
class StopCommand : ICommand {
    private val log = LoggerFactory.getLogger(StopCommand::class.java)

    /**
     * Executes the command.
     * @param event A [CommandEvent] representing from the invoked command and allowing to respond to the command
     */
    override fun action(event: CommandEvent) {
        val channel = event.channel
        // self member
        val self = event.guild!!.selfMember
        val selfVoiceState = self!!.voiceState
        val member = event.member
        val memberVoiceState = member!!.voiceState

        if (!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("음성채널에 들어가주세요.").queue()
            return
        }

        // member의 channel과 bot의 channel이 다르면 return
        if (selfVoiceState!!.channel != memberVoiceState!!.channel) {
            channel.sendMessage("헬파티봇이 있는 채널이 아닙니다.").queue()
            return
        }


        val musicManage: GuildMusicManager = PlayerManager.INSTANCE.getMusicManager(event.guild!!)

        try{
            // stop track and clear
            // bot이 음악을 재생중이지 않으면 return
            if (!musicManage.isPlaying()) {
                channel.sendMessage("재생중인 음악이 없습니다.").queue()
                return
            }

            musicManage.stopMusic()
            channel.sendMessage("`${member.nickname}`에 의해 재생을 중지합니다.").queue()
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