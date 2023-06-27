package kr.weareboard.domain.entity.music.dto

import jakarta.persistence.Column
import jakarta.persistence.Id
import kr.weareboard.domain.entity.music.MusicHistoryEntity

data class MusicHistoryDto(
    val id: Long,
    var memberId: String,
    var guildId: String,
    var trackIdentifier: String,
    var title: String,
    var author: String,
    var url: String,
    var time: Long,
) {
    constructor(musicHistoryEntity: MusicHistoryEntity) : this(
        id = musicHistoryEntity.id,
        memberId = musicHistoryEntity.memberId,
        guildId = musicHistoryEntity.guildId,
        trackIdentifier = musicHistoryEntity.trackIdentifier,
        title = musicHistoryEntity.title,
        author = musicHistoryEntity.author,
        url = musicHistoryEntity.url,
        time = musicHistoryEntity.time,
    )
}
