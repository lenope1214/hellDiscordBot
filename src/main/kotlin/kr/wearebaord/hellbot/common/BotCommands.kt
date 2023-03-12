package kr.wearebaord.hellbot.common

import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.button
import kr.wearebaord.hellbot.PREFIX
import kr.wearebaord.hellbot.SHOW_BUTTONS
import kr.wearebaord.hellbot.TEXT_CHANNEL_NAME
import kr.wearebaord.hellbot.exception.InvalidTextChannel
import kr.wearebaord.hellbot.music.PlayTrackInfo
import kr.wearebaord.hellbot.music.entity.PlayerManager
import kr.wearebaord.hellbot.music.enums.ComponentTypes
import kr.wearebaord.hellbot.music.enums.EmojiValue
import kr.wearebaord.hellbot.music.status.getRepeatEmoji
import kr.wearebaord.hellbot.music.status.getRepeatText
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.requests.restaction.MessageEditAction
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.OffsetDateTime


class BotCommands

private val log = LoggerFactory.getLogger(BotCommands::class.java)


fun joinVoiceChannelBot(channel: MessageChannel, member: Member, guild: Guild): Boolean {
    val selfVoiceState = member!!.voiceState
    println("selfVoiceState = ${selfVoiceState}")

    // 요청자가 음성 채널에 들어가있는가?
    if (!isMemberEnteredChannel(selfVoiceState, channel)) return false

    // 봇이 이미 음성채널에 들어가있는가?
    if (isAlreadyConnectedChannel(guild)) return true

    // 요청자가 음성채널에 연결할 권한이 있는가?
    // if (!isAbleToConnectVoiceChannel(member, channel)) return false

    // 요청자가 음성채널에서 말할 권한이 있는가?
    // if (!isAbleToSpeakVoice(member, channel)) return false

    val audioManager = guild.audioManager

    // selfVoiceState는 CacheFlag에 VOICE_STATE가 포함되어 있어야 한다
    val voiceChannel = selfVoiceState!!.channel

    // 봇이 음성 채널에 들어가도록 함
    audioManager.openAudioConnection(voiceChannel)
    return true
}

private fun isAlreadyConnectedChannel(guild: Guild): Boolean {
    log.info("guild.selfMember = ${guild.selfMember}")
    if (guild.selfMember.voiceState!!.inAudioChannel()) {
        return true
    }
    return false
}

private fun isAbleToSpeakVoice(
    member: Member,
    channel: MessageChannel
): Boolean {
    if (!member.hasPermission(Permission.VOICE_SPEAK)) {
        channel.sendMessage("음성채널에서 말할 권한이 없습니다.").queue()
        return false
    }
    return true
}

private fun isAbleToConnectVoiceChannel(
    member: Member,
    channel: MessageChannel
): Boolean {
    if (!member.hasPermission(Permission.VOICE_CONNECT)) {
        channel.sendMessage("음성채널에 연결할 권한이 없습니다.").queue()
        return false
    }
    return true
}

fun isMemberEnteredChannel(
    selfVoiceState: GuildVoiceState?,
    channel: MessageChannel
): Boolean {
    if (selfVoiceState?.inAudioChannel() != true) {
        channel.sendMessage("음성채널에 들어가주세요.").queue()
        channel.deleteAllMessages()
        return false
    }
    return true
}

fun leaveBot(guild: Guild, channel: TextChannel?) {
    // 봇이 음성채널에 있다면 나가게 한다
    val audioManager = guild.audioManager
    if (audioManager.isConnected) {
        audioManager.closeAudioConnection()
    }
    channel?.sendEmbed(
        title = "봇이 음성채널에서 나갔습니다.",
        description = "봇이 음성채널에서 나갔습니다."
    )
}

// 올바른 PREFIX로 시작하는지 확인하는 함수
fun String.isCorrectPrefix(): Boolean {
    return this.startsWith(PREFIX, ignoreCase = true)
}


fun doNotProcessMessage(command: String, commands: List<String>): Boolean {
    if (!(commands.contains(command))) {
        throw IllegalArgumentException("잘못된 명령어입니다.")
    }
    return true
}

fun isValidTextChannel(channel: MessageChannel) {
    if (channel.name != TEXT_CHANNEL_NAME) {
        throw InvalidTextChannel()
    }
}

fun isValidContentRaw(raw: String, commands: List<String>): String {

    val command = raw.let {
        if (it.isEmpty()) {
            throw IllegalArgumentException("raw is empty")
        }
        if (!it.isCorrectPrefix()) {
            throw IllegalArgumentException("raw is not correct prefix")
        }

        val prefix = raw.split(" ")[0]
        val checkCommand = prefix.substring(PREFIX.length)
        if (!commands.contains(checkCommand)) {
            throw IllegalArgumentException("raw is not correct message")
        }
        checkCommand
    }
    return command
}

fun String.parseCommand(): String {
    if (this.isEmpty() || this.length < PREFIX.length) return ""
    return this.substring(PREFIX.length).split(" ")[0]
}

fun parseContent(raw: String): String {
    if (raw.isEmpty()) return ""

    // split 후 1번 인덱스부터 끝까지
    val substring = raw.substring(PREFIX.length)
    val split = substring.split(" ")
    val subList = split.subList(1, split.size)
    return subList.joinToString(" ")
}

fun MessageChannel.deleteAllMessages() {
    val channel = this as TextChannel
    channel.deleteAllMessages()
}

fun TextChannel.deleteAllMessages() {
    try {
        val deleteTime = OffsetDateTime.now().minusSeconds(1)
        val iterableMessage = this.iterableHistory.takeIf { it != null } ?: return
        iterableMessage
            .takeAsync(5) // Collect 5 messages
            .thenApply {
                it.forEach { m ->
                    // 삭제요청한 시간보다 5초 이전에 만들어진 데이터만 삭제
                    if (m.timeCreated.isBefore(deleteTime)) {
                        // 공지가 포함되면 삭제하지 않는다.
                        if (m.contentRaw.contains("[공지]").not()) m.delete().queue()
                    }
                }
            }
    } catch (e: Exception) {
        e.printStackTrace()
        log.error("채널의 메세지 삭제 실패 이유 : ${e.message}")
    }
}

fun TextChannel.sendYoutubeEmbed(
    url: String,
    title: String,
    description: String = "",
    author: String,
    duration: Long = 0,
    youtubeIdentity: String = "",
    playTrackInfoList: List<PlayTrackInfo>,
    isPause: Boolean = false,
    isRepeat: Boolean = false,
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
    val footerText = "${addedBy.nickname}(\n${addedBy.roles.joinToString(" | ") { it.name }})"
    val footerIconUrl = addedBy.effectiveAvatar.url

    fields.add(
        Field(
            "노래 길이",
            duration.convertMsToMmSs(),
            true,
        )
    )


    val trackNames = tracks.map { it.info.title }
    log.info("trackNames : $trackNames")

    val playButton = button(
        id = if (isPause) "playButton" else "pauseButton",
        style = ButtonStyle.PRIMARY,
        label = if (isPause) "재생" else "일시정지",
        emoji = if (isPause) EmojiValue.PLAY.fromUnicode() else EmojiValue.PAUSE.fromUnicode(),
    )

    val stopButton = button(
        id = "stopButton",
        style = ButtonStyle.DANGER,
        label = "정지",
        emoji = EmojiValue.EXIT.fromUnicode(),
    )

    val skipButton = if (trackNames.size > 1) {
        button(
            id = "skipButton",
            style = ButtonStyle.SECONDARY,
            label = "다음곡",
        )
    } else null

    val repeatButton = button(
        id = "repeatButton",
        label = getRepeatText(isRepeat),
        emoji = getRepeatEmoji(isRepeat),
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
        ComponentTypes.STRING_MENU to listOf(menu),
    )

    if (SHOW_BUTTONS) {
        var buttons = ComponentTypes.BUTTON to mutableListOf(
            playButton,
            stopButton,
            repeatButton,
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
        thumbnail = youtubeIdentity.isNotEmpty().let { "https://i.ytimg.com/vi/${youtubeIdentity}/hqdefault.jpg" },
        fields = fields,
        actionRowsMap = actionRowsMap,
        footerText = footerText,
        footerIconUrl = footerIconUrl,
    )
}

fun TextChannel.sendEmbed(
    url: String = "",
    title: String = "",
    description: String = "이 노래를 재생합니다.",
    author: String = "",
    thumbnail: String? = null,
    fields: List<Field> = listOf(),
    actionRowsMap: Map<ComponentTypes, List<ItemComponent>> = mapOf(),
    footerText: String? = null,
    footerIconUrl: String? = null,
) {
    // 메세지 임베드 값 생성
    val builder = EmbedBuilder()

    fields.forEach {
        builder.addField(it)
    }

    val messageEmbed = builder.setAuthor("HellBot")
        .setTitle(title, url)
        .setDescription(description)
        .setAuthor(author)
        .setThumbnail(thumbnail)
        .setColor(Color(0xFF7B96))
        .setFooter(footerText, footerIconUrl)
        .build()

    val sendMessageEmbeds = this
        .sendMessageEmbeds(messageEmbed)


    addActionRows(actionRowsMap, sendMessageEmbeds)



    // 만약, 채널에 기존에 보낸 embed가 존재한다면 수정하는 방식으로 한다.
    if(isUpdate){
        log.info("기존 메세지 수정")
        // edit last embed
        val messageChannel: MessageChannel = this
        val lastMessage = messageChannel.latestMessageId.let {
            messageChannel.edit
        }
        val lastEmbeds = lastMessage.embeds
        log.info("lastEmbeds : $lastEmbeds")
        lastMessage.editMessageEmbeds(messageEmbed).queue()
    }

    // 기존에 보낸 embed가 없다면 채널의 기존 메세지 삭제 후
    this.deleteAllMessages().let{
        log.info("기존 메세지 삭제 후 새로운 메세지 생성")
        // 새로운 메세지 생성
        sendMessageEmbeds
            .queue()

    }
}

private fun addActionRows(
    actionRowsMap: Map<ComponentTypes, List<ItemComponent>>,
    sendMessageEmbeds: MessageCreateAction
) {
    if (actionRowsMap.isNotEmpty()) {
        actionRowsMap.forEach { (_, value) ->
            log.info("addActionRows : $value")
            sendMessageEmbeds
                .addActionRow(value)
        }
    }
}

private fun updateActionRows(
    actionRowsMap: Map<ComponentTypes, List<ItemComponent>>,
    sendMessageEmbeds: MessageEditAction,
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
