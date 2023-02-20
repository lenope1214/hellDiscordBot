package kr.wearebaord.hellbot.common

import kr.wearebaord.hellbot.PREFIX
import kr.wearebaord.hellbot.TEXT_CHANNEL_NAME
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent


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
    if (!event.author.isBot && channel.name != TEXT_CHANNEL_NAME) {
        channel.sendMessage("채팅 채널 이름이 `$TEXT_CHANNEL_NAME`인 채널에서 요청해야합니다.").queue()
        return true
    }

    return false
}
