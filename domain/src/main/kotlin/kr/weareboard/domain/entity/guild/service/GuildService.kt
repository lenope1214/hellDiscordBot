package kr.weareboard.domain.entity.guild.service

import kr.weareboard.domain.entity.guild.GuildEntity

interface GuildService {
    fun getOrPutByGuildId(guildId: String): GuildEntity
}
