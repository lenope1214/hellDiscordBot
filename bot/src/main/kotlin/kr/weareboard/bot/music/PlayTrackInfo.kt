package kr.wearebaord.hellbot.music

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import net.dv8tion.jda.api.entities.Member
import java.time.LocalDateTime

data class PlayTrackInfo(
    val track: AudioTrack,
    val addedBy: Member,
    val addedAt: LocalDateTime = LocalDateTime.now()
) {

    override fun toString(): String {
        return "PlayTrackInfo(track={" +
            """
                   title: ${track.info.title},
                   author: ${track.info.author},
                   length: ${track.info.length},
                   identifier: ${track.info.identifier},
                   uri: ${track.info.uri}
            """.trimIndent()
        "}" +
            ", addedBy='$addedBy', addedAt=$addedAt)"
    }
}
