package kr.weareboard.bot.service

import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.MessageCreateBuilder
import dev.minn.jda.ktx.messages.MessageEditBuilder
import dev.minn.jda.ktx.messages.editMessage
import kr.weareboard.bot.common.convertMsToMmSs
import kr.weareboard.bot.domain.enums.ComponentTypes
import kr.weareboard.bot.domain.enums.EmojiValue
import kr.weareboard.bot.music.PlayTrackInfo
import kr.weareboard.bot.music.status.getRepeatEmoji
import kr.weareboard.bot.music.status.getRepeatText
import kr.weareboard.bot.service.interfaces.TextChannelService
import kr.weareboard.domain.entity.guild.GuildEntity
import kr.weareboard.domain.entity.guild.GuildRepository
import kr.weareboard.main.SHOW_BUTTONS
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TextChannelServiceImpl(
    private val guildRepository: GuildRepository
) : TextChannelService {

    private val log = LoggerFactory.getLogger(this::class.java)

    override fun deleteAllMessages(channel: MessageChannel) {
        try {
            val deleteTime = java.time.OffsetDateTime.now().minusSeconds(1)
            channel.iterableHistory.takeAsync(10).thenAccept {
                it.forEach { message ->
                    if (message.timeCreated.isBefore(deleteTime)) {
                        log.info("채널의 메세지 삭제 : ${message.contentDisplay}")
                        message.delete().queue()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            log.error("채널의 메세지 삭제 실패 이유 : ${e.message}")
        }
    }

    override fun sendYoutubeEmbed(
        channel: TextChannel,
        url: String,
        title: String,
        description: String,
        author: String,
        duration: Long,
        youtubeIdentity: String,
        playTrackInfoList: List<PlayTrackInfo>,
        isPause: Boolean,
        isRepeat: Boolean
    ) {
        if (playTrackInfoList.isEmpty()) return
        // 트랙 정보 리스트 출력
        playTrackInfoList.forEach {
            log.info("PlayTrackInfo : $it")
        }

        val tracks = playTrackInfoList.map { it.track }
        var fields = mutableListOf<Field>()
        val addedBy = playTrackInfoList.first().addedBy
        // 등록자 정보 출력
        var footerText = "${addedBy.nickname}"
        if(addedBy.roles.size > 0){
            footerText += "(${addedBy.roles.joinToString(" | ") { it.name }})"
        }
        val footerIconUrl = addedBy.effectiveAvatar.url

        fields.add(
            Field(
                "노래 길이",
                duration.convertMsToMmSs(),
                true
            )
        )

        val trackNames = tracks.map { it.info.title }.toMutableList()
        log.info("trackNames : $trackNames")

        val playButton = button(
            id = if (isPause) "playButton" else "pauseButton",
            style = ButtonStyle.PRIMARY,
            label = if (isPause) "재생" else "일시정지",
            emoji = if (isPause) EmojiValue.PLAY.fromUnicode() else EmojiValue.PAUSE.fromUnicode()
        )

        val stopButton = button(
            id = "stopButton",
            style = ButtonStyle.DANGER,
            label = "정지",
            emoji = EmojiValue.EXIT.fromUnicode()
        )

        val skipButton = if (trackNames.size > 1) {
            button(
                id = "skipButton",
                style = ButtonStyle.SECONDARY,
                label = "다음곡"
            )
        } else {
            null
        }

        val repeatButton = button(
            id = "repeatButton",
            label = getRepeatText(isRepeat),
            emoji = getRepeatEmoji(isRepeat)
        )

        // 첫 곡 제목 오른쪽에 트랙 수 추가
        val firstTrackName = trackNames.first()
        val firstTrackNameWithTrackCount = "$firstTrackName (총 ${trackNames.size}곡)"
        trackNames[0] = firstTrackNameWithTrackCount
        val menu = StringSelectMenu(
            customId = "trackBox",
            placeholder = "StringSelectMenu",
            options = trackNames.mapIndexed { index, trackName ->
                log.info("trackName : $trackName")
                log.info("index : $index")
                SelectOption(
                    trackName,
                    index.toString(),
                    default = index == 0
                )
            }
        )

        var actionRowsMap: Map<ComponentTypes, List<ActionComponent>> = mapOf(
            ComponentTypes.STRING_MENU to listOf(menu)
        )

        if (SHOW_BUTTONS) {
            var buttons = ComponentTypes.BUTTON to mutableListOf(
                playButton,
                stopButton,
                repeatButton
            )
            if (skipButton != null) {
                buttons.second.add(skipButton)
            }
            actionRowsMap = actionRowsMap.plus(
                buttons
            )
        }

        sendEmbed(
            channel = channel,
            url = url,
            title = title,
            description = description,
            author = author,
            thumbnail = youtubeIdentity.isNotEmpty().let { "https://i.ytimg.com/vi/$youtubeIdentity/hqdefault.jpg" },
            fields = fields,
            actionRowsMap = actionRowsMap,
            footerText = footerText,
            footerIconUrl = footerIconUrl
        )
    }

    override fun sendEmbed(
        channel: TextChannel,
        url: String,
        title: String,
        description: String,
        author: String,
        thumbnail: String?,
        fields: List<Field>,
        actionRowsMap: Map<ComponentTypes, List<ItemComponent>>,
        footerText: String?,
        footerIconUrl: String?
    ) {
        // 메세지 임베드 값 생성
        val embed: MessageEmbed = EmbedBuilder(
            authorName = author,
            title = title,
            description = description,
            thumbnail = thumbnail,
            color = 0xFF7B96,
            footerText = footerText,
            footerIcon = footerIconUrl,
            fields = fields
        ).build()

        val lastMessageId = guildRepository.findById(channel.id).get().lastMessageId
        channel.editMessage(
            id = lastMessageId,
            embeds = listOf(
                embed
            ),
            components = actionRowsMap.map { (type, components) ->
                ActionRow.of(
                    components.map { component ->
                        when (type) {
                            ComponentTypes.BUTTON -> component as Button
                            ComponentTypes.STRING_MENU -> component as StringSelectMenu
                        }
                    }
                )
            },
            replace = true
        ).queue()
    }

    override fun sendFirstMessage(
        channel: TextChannel,
        title: String,
        description: String,
    ) {
        // 메세지 임베드 값 생성
        val embed: MessageEmbed = EmbedBuilder(
            title = title,
            description = description,
            color = 0xFF7B96,
        ).build()

        // 메세지 보내기
        val message = MessageCreateBuilder(
            embeds = listOf(
                embed
            ),
        ).build()

        channel.sendMessage(message).queue {
            guildRepository.save(
                GuildEntity(
                    id = channel.id,
                    name = channel.guild.name,
                    lastMessageId = it.id,
                )
            )
        }
    }

    override fun updateActionRows(
        actionRowsMap: Map<ComponentTypes, List<ItemComponent>>,
        sendMessageEmbeds: MessageEditAction
    ) {
        if (actionRowsMap.isNotEmpty()) {
            var components: List<ItemComponent> = mutableListOf()
            actionRowsMap.forEach { (_, value) ->
                log.info("updateActionRows : $value")
                components = components.plus(value)
            }
            sendMessageEmbeds.setActionRow(components)
        }
    }
}
