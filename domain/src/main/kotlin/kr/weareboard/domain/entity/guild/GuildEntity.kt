package kr.weareboard.domain.entity.guild

import jakarta.persistence.*
import kr.weareboard.werewolf.domain.entity.BaseEntity
import org.hibernate.annotations.Where

private val log = org.slf4j.LoggerFactory.getLogger(GuildEntity::class.java)

@Where(clause = "deleted_at IS NULL")
@Entity
@Table(name = "tb_guild")
class GuildEntity(
    @Column(name = "name", length = 50)
    var name: String = "",

    @Column(name = "usable")
    var usable: Boolean = true,

    @Column(name = "lastMessageId", length = 20)
    var lastMessageId: String = "0",

    @Id
    @Column(name = "guild_id")
    val id: String,
) : BaseEntity() {
    fun updateLastMessageId(lastMessageId: String) {
        try {
            this.lastMessageId = lastMessageId
        } catch (e: Exception) {
            log.error("updateLastMessageId error : ${e.message}")
            this.lastMessageId = "0"
        }
    }

    override fun toString(): String {
        return "GuildEntity(name='$name', usable=$usable, lastMessageId='$lastMessageId', id=$id)"
    }
}
