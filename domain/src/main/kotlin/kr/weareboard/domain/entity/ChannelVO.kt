package kr.weareboard.domain.entity

import jakarta.persistence.*
import kr.weareboard.domain.entity.BaseEntity
import org.hibernate.annotations.Where

@Where(clause = "deleted_at IS NULL")
@Table(name = "tb_job")
class ChannelVO(
    @Column(name = "name", length = 50)
    var name: String = "",

    @Column(name = "usable")
    var usable: Boolean = true,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    val id: Long? = null,
) : BaseEntity()
