package kr.weareboard.bot.service.interfaces

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

interface BotService {
    fun play(track: AudioTrack): Boolean

    fun joinVoiceChannelIfNotJoined(channel: TextChannel, member: Member, guild: Guild): Boolean

    fun isAlreadyConnectedChannel(guild: Guild): Boolean

    fun isMemberEnteredChannel(
        guild: Guild,
        channel: TextChannel
    ): Boolean
    fun leaveBot(guild: Guild, channel: TextChannel, member: Member?): Boolean
}
