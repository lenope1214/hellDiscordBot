package kr.weareboard.domain.entity.guild.service

import kr.weareboard.domain.entity.guild.GuildEntity
import kr.weareboard.domain.entity.guild.GuildRepository
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull

@Service
class GuildServiceImpl(
    private val guildRepository: GuildRepository,
) : GuildService{

    override fun getOrPutByGuildId(guildId: String): GuildEntity {
        guildRepository.findById(guildId).let {
            if (it.isPresent) {
                return it.get()
            }
            guildRepository.save(GuildEntity(id = guildId)).let { guildEntity ->
                return guildEntity
            }
        }
    }
}
