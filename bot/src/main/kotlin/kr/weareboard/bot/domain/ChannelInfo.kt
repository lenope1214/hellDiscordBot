package kr.wearebaord.hellbot.domain

import kr.wearebaord.hellbot.music.PlayTrackInfo
import org.slf4j.LoggerFactory

data class ChannelInfo(
    var tracks: MutableList<PlayTrackInfo> = mutableListOf()
) {

    private val log = LoggerFactory.getLogger(ChannelInfo::class.java)
    var embedMessageId: Long = 0L

    fun updateEmbedId(embedMessageId: Long) {
        this.embedMessageId = embedMessageId
    }

    override fun toString(): String {
        return "ChannelInfo(embedMessageId=$embedMessageId, tracks=${tracks.joinToString { it.toString() }})"
    }

    fun reset() {
        tracks = mutableListOf()
        embedMessageId = 0L
    }
}
