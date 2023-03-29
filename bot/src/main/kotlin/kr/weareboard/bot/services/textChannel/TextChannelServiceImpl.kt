package kr.weareboard.bot.services.textChannel

import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.button
import dev.minn.jda.ktx.messages.EmbedBuilder
import dev.minn.jda.ktx.messages.InlineEmbed
import kr.wearebaord.hellbot.common.convertMsToMmSs
import kr.wearebaord.hellbot.domain.enums.ComponentTypes
import kr.wearebaord.hellbot.domain.enums.EmojiValue
import kr.wearebaord.hellbot.music.PlayTrackInfo
import kr.wearebaord.hellbot.music.status.getRepeatEmoji
import kr.wearebaord.hellbot.music.status.getRepeatText
import kr.weareboard.main.SHOW_BUTTONS
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class TextChannelServiceImpl {

    private val log = LoggerFactory.getLogger(this::class.java)

    fun deleteAllMessages(channel: MessageChannel) {
        try {
            val deleteTime = java.time.OffsetDateTime.now().minusSeconds(1)
            channel.iterableHistory.takeAsync(100).thenAccept {
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

    fun sendYoutubeEmbed(
        url: String,
        title: String,
        description: String = "",
        author: String,
        duration: Long = 0,
        youtubeIdentity: String = "",
        playTrackInfoList: List<PlayTrackInfo>,
        isPause: Boolean = false,
        isRepeat: Boolean = false
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
        val footerText = "${addedBy.nickname}\n(${addedBy.roles.joinToString(" | ") { it.name }})"
        val footerIconUrl = addedBy.effectiveAvatar.url

        fields.add(
            Field(
                "노래 길이",
                duration.convertMsToMmSs(),
                true
            )
        )

        val trackNames = tracks.map { it.info.title }
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

    fun sendEmbed(
        channel: TextChannel,
        url: String = "",
        title: String = "",
        description: String = "이 노래를 재생합니다.",
        author: String = "",
        thumbnail: String? = null,
        fields: List<Field> = listOf(),
        actionRowsMap: Map<ComponentTypes, List<ItemComponent>> = mapOf(),
        footerText: String? = null,
        footerIconUrl: String? = null
    ): String {
        // 메세지 임베드 값 생성
        val messageEmbed = EmbedBuilder(
            authorName = author,
            title = title,
            description = description,
            thumbnail = thumbnail,
            color = 0xFF7B96,
            footerText = footerText,
            footerIcon = footerIconUrl,
            fields = fields,
        )

        addActionRows(actionRowsMap, messageEmbed)

        var messageId = ""
        // 기존에 보낸 embed가 없다면 채널의 기존 메세지 삭제 후
        this.deleteAllMessages(channel).let {
            log.info("기존 메세지 삭제 후 새로운 메세지 생성 [${channel.guild.name}] : $title")
            // 새로운 메세지 생성
            messageEmbed.build().let { embed ->
                channel.sendMessageEmbeds(embed).queue {
                    messageId = it.id
                }
            }
        }

        log.info("새로운 메세지 생성 완료 message id : ${messageId}")
        return messageId
    }

     fun addActionRows(
        actionRowsMap: Map<ComponentTypes, List<ItemComponent>>,
        sendMessageEmbeds: InlineEmbed,
    ) {
        if (actionRowsMap.isNotEmpty()) {
            actionRowsMap.forEach { (_, value) ->
                log.info("addActionRows : $value")
                sendMessageEmbeds
                    .addActionRow(value)
            }
        }
    }

     fun updateActionRows(
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
