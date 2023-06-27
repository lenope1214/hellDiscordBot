package kr.weareboard.domain.entity.music

import org.springframework.data.jpa.repository.JpaRepository

interface MusicHistoryRepository: JpaRepository<MusicHistoryEntity, String> {
    fun findByTitle(title: String): MusicHistoryEntity?
    fun countByTrackIdentifier(trackIdentifier: String): Long
}
