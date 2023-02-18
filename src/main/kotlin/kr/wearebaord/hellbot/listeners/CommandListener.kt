package kr.wearebaord.hellbot.listeners

import kr.wearebaord.hellbot.OWNER_ID
import kr.wearebaord.hellbot.PREFIX
import kr.wearebaord.hellbot.botTextChannel
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.slf4j.LoggerFactory

fun joinVoiceChannelBot(channel: MessageChannel, member: Member, guild: Guild) {
    val selfVoiceState = member!!.voiceState
    println("selfVoiceState = ${selfVoiceState}")
    if (selfVoiceState?.inAudioChannel() != true) {
        channel.sendMessage("음성채널에 들어가주세요.").queue()
        return
    }

    if (!member.hasPermission(Permission.VOICE_CONNECT)) {
        channel.sendMessage("음성채널에 연결할 권한이 없습니다.").queue()
        return
    }

    if (!member.hasPermission(Permission.VOICE_SPEAK)) {
        channel.sendMessage("음성채널에서 말할 권한이 없습니다.").queue()
        return
    }

    // 이미 들어와 있으면 알림 후 종료
    if (guild.selfMember.voiceState!!.inAudioChannel()) {
        channel.sendMessage("이미 음성채널에 연결되어 있습니다.").queue()
        return
    }

    val audioManager = guild.audioManager
    val voiceChannel = selfVoiceState.channel

    audioManager.openAudioConnection(voiceChannel)
    channel.sendMessageFormat("음성채널에 연결되었습니다. (%s)", voiceChannel!!.name).queue()
}

fun leaveBot(channel: MessageChannel, guild: Guild) {
    // 봇이 음성채널에 있다면 나가게 한다
    val audioManager = guild.audioManager
    if (audioManager.isConnected) {
        audioManager.closeAudioConnection()
        channel.sendMessage("음성채널에서 나갔습니다.").queue()
    }
}

fun isInvalidMessage(event: MessageReceivedEvent): Boolean {
    val raw: String = event.message.contentRaw
    val channel = event.channel

    // raw의 대소문자에 상관없이 prefix로 시작하는지 확인한다.
    if (!raw.startsWith(PREFIX, ignoreCase = true)) return true

    // 대상이 봇이 아니고 채널이 Config.getEnvByKey("text_channel_name")과 다르다면 알림을 주고 종료
    if (!event.author.isBot && channel.name != botTextChannel) {
        channel.sendMessage("채팅 채널 이름이 `$botTextChannel`인 채널에서 요청해야합니다.").queue()
        return true
    }

    return false
}

class CommandListener : ListenerAdapter() {
    val log = LoggerFactory.getLogger(CommandListener::class.java)

    override fun onReady(event: ReadyEvent) {
        log.info("Logged in as ${event.jda.selfUser.name}")
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val raw: String = event.message.contentRaw
        val channel = event.channel

        if (isInvalidMessage(event)) return

        when (raw.lowercase()) {
            PREFIX + "help" -> {
                event.channel.sendMessage("도움말").queue()
            }
            PREFIX + "shutdown" -> {
                if (!(event.author.id == OWNER_ID || event.member?.hasPermission(Permission.ADMINISTRATOR) == true)) {
                    event.message.delete().queue()
                    event.channel.sendMessage("권한이 없습니다.").queue()
                }

                log.info("Shutdown command received from ${event.author.name}")
                event.jda.shutdown()
            }
            PREFIX + "join" -> {
                log.info("join bot by username : ${event.author.name}")
                joinVoiceChannelBot(event.channel, event.member!!, event.guild)
            }
            PREFIX + "leave" -> {
                log.info("leave bot by username : ${event.author.name}")
                leaveBot(event.channel, event.guild)
            }
        }

//        // prefix + "join"이라면 음성 채널에 참여
//        if (raw.equals(PREFIX + "join", ignoreCase = true)) {
//
//        }

    }
}