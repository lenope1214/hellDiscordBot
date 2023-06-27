package kr.weareboard.domain.entity.music

import jakarta.persistence.*
import kr.weareboard.domain.entity.BaseEntity
import org.hibernate.annotations.Comment
import org.hibernate.annotations.Where

private val log = org.slf4j.LoggerFactory.getLogger(MusicHistoryEntity::class.java)

@Where(clause = "deleted_at IS NULL")
@Entity
@Table(
    name = "tb_music_history",
    indexes = [
        Index(name = "idx_music_history_trackIdentifier_title_url", columnList = "trackIdentifier,title,url")
    ]
)
class MusicHistoryEntity(
    @Column(name = "member_id", length = 50, updatable = false)
    @Comment("유저 고유 식별자")
    var memberId: String,

    @Column(name = "guild_id", length = 50, updatable = false)
    @Comment("서버 고유 식별자")
    var guildId: String,

    @Column(name = "trackIdentifier", length = 100, updatable = false)
    @Comment("트랙 고유 식별자(동일한 음원이면 동일한 값임)")
    var trackIdentifier: String,

    @Column(name = "title", length = 100, updatable = false)
    @Comment("음원 제목")
    var title: String,

    @Column(name = "author", length = 100, updatable = false)
    @Comment("음원 작곡가(유튜브라면 채널명)")
    var author: String,

    @Column(name = "url", length = 100, updatable = false)
    @Comment("음원 URL")
    var url: String,

    @Column(name = "time", updatable = false)
    @Comment("음원 길이, 단위 : ms")
    var time: Long,

    @Id
    @Column(name = "id", updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
) : BaseEntity()
