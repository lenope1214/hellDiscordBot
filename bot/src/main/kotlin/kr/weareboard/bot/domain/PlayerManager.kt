package kr.weareboard.bot.domain

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import kr.weareboard.bot.music.GuildMusicManager
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

interface PlayerManager {
    fun getMusicManager(guild: Guild): GuildMusicManager
    fun loadAndPlay(
        guild: Guild,
        channel: TextChannel,
        trackUrl: String,
        addedBy: Member,
        isYoutubeSearch: Boolean,
    )

    fun sendMessage(channel: TextChannel)

    fun plus(
        channel: TextChannel,
        tracks: List<AudioTrack>,
        addedBy: Member,
    )

    /**
     * 만약 다음 곡을 재생했다면 true, 종료됐다면 false
     */
    fun next(
        channel: TextChannel,
        nextedBy: Member? = null,
    ): Boolean

    fun jumpTo(
        channel: TextChannel,
        index: Int,
        jumpedBy: Member? = null,
    )

    fun stop(
        channel: TextChannel,
        stopBy: Member? = null,
    )

    fun resume(
        textChannel: TextChannel,
        resumedBy: Member? = null,
    )

    fun pause(
        textChannel: TextChannel,
        pausedBy: Member? = null,
    )

    fun prevTrack(
        textChannel: TextChannel,
        prevBy: Member? = null,
    )

    fun repeat(
        textChannel: TextChannel,
        repeatBy: Member? = null,
    )

    fun leftChannel(
        guild: Guild,
        channel: TextChannel,
        member: Member? = null,
    )
}
