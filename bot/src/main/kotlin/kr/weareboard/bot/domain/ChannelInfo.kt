package kr.weareboard.bot.domain

import kr.weareboard.bot.music.PlayTrackInfo
import org.slf4j.LoggerFactory

data class ChannelInfo(
    var tracks: MutableList<PlayTrackInfo> = mutableListOf()
) {
    private val log = LoggerFactory.getLogger(ChannelInfo::class.java)

    override fun toString(): String {
        return "ChannelInfo(tracks=${tracks.joinToString { it.toString() }})"
    }

    fun reset() {
        tracks = mutableListOf()
    }
}
