package kr.weareboard.domain.entity

import jakarta.persistence.*
import kr.weareboard.werewolf.domain.entity.BaseEntity
import org.hibernate.annotations.Where

@Where(clause = "deleted_at IS NULL")
@Entity
@Table(name = "tb_guild")
class GuildEntity(
    @Column(name = "name", length = 50)
    var name: String = "",

    @Column(name = "usable")
    var usable: Boolean = true,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    val id: Long? = null,
) : BaseEntity()
