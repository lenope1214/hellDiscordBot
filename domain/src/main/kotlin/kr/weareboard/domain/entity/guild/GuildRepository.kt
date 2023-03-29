package kr.weareboard.domain.entity.guild

import org.springframework.data.jpa.repository.JpaRepository

interface GuildRepository: JpaRepository<GuildEntity, String> {
}
