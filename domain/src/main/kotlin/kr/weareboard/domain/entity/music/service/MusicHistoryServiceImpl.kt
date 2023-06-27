package kr.weareboard.domain.entity.music.service

import kr.weareboard.domain.entity.music.MusicHistoryEntity
import kr.weareboard.domain.entity.music.MusicHistoryRepository
import kr.weareboard.domain.entity.music.dto.MusicHistoryDto
import org.springframework.stereotype.Service

@Service
class MusicHistoryServiceImpl(
    private val musicHistoryRepository: MusicHistoryRepository,
) : MusicHistoryService{

    override fun addHistory(
        guildId: String,
        memberId: String,
        trackIdentifier: String,
        title: String,
        author: String,
        url: String,
        time: Long,
    ): MusicHistoryDto {
        return MusicHistoryDto(musicHistoryRepository.save(
            MusicHistoryEntity(
                memberId = memberId,
                guildId = guildId,
                trackIdentifier = trackIdentifier,
                title = title,
                author = author,
                url = url,
                time = time,
            )
        ))
    }
}
