package kr.weareboard.domain.entity.music.service

import kr.weareboard.domain.entity.music.dto.MusicHistoryDto

interface MusicHistoryService {
    fun addHistory(
        guildId: String,
        memberId: String,
        trackIdentifier: String,
        title: String,
        author: String,
        url: String,
        time: Long
    ): MusicHistoryDto
}
