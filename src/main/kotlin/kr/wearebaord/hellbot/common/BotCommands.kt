package kr.wearebaord.hellbot.common

import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import dev.minn.jda.ktx.interactions.components.SelectOption
import dev.minn.jda.ktx.interactions.components.StringSelectMenu
import dev.minn.jda.ktx.interactions.components.button
import kr.wearebaord.hellbot.PREFIX
import kr.wearebaord.hellbot.TEXT_CHANNEL_NAME
import kr.wearebaord.hellbot.music.PlayerManager
import kr.wearebaord.hellbot.music.enums.ComponentTypes
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageEmbed.Field
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import org.slf4j.LoggerFactory
import java.awt.Color
import java.util.concurrent.TimeUnit


class BotCommands

private val log = LoggerFactory.getLogger(BotCommands::class.java)


fun joinVoiceChannelBot(channel: MessageChannel, member: Member, guild: Guild) {
    val selfVoiceState = member!!.voiceState
    println("selfVoiceState = ${selfVoiceState}")
    if (selfVoiceState?.inAudioChannel() != true) {
        channel.sendMessage("음성채널에 들어가주세요.").queue()
        return
    }

    if (!member.hasPermission(Permission.VOICE_CONNECT)) {
        channel.sendMessage("음성채널에 연결할 권한이 없습니다.").queue()
        return
    }

    if (!member.hasPermission(Permission.VOICE_SPEAK)) {
        channel.sendMessage("음성채널에서 말할 권한이 없습니다.").queue()
        return
    }

    // 이미 들어와 있으면 알림 후 종료
    if (guild.selfMember.voiceState!!.inAudioChannel()) {
//        channel.sendMessage("이미 음성채널에 연결되어 있습니다.").queue()
        return
    }

    val audioManager = guild.audioManager
    val voiceChannel = selfVoiceState.channel

    audioManager.openAudioConnection(voiceChannel)
//    channel.sendMessageFormat("음성채널에 연결되었습니다. (%s)", voiceChannel!!.name).queue()
}

fun leaveBot(channel: MessageChannel, guild: Guild) {
    // 봇이 음성채널에 있다면 나가게 한다
    val audioManager = guild.audioManager
    if (audioManager.isConnected) {
        audioManager.closeAudioConnection()
        channel.sendMessage("음성채널에서 나갔습니다.").queue()
    }
}

fun isInvalidMessage(event: MessageReceivedEvent): Boolean {
    val raw: String = event.message.contentRaw
    val channel = event.channel

    // raw의 대소문자에 상관없이 prefix로 시작하는지 확인한다.
    if (!raw.startsWith(PREFIX, ignoreCase = true)) return true

    // 대상이 봇이 아니고 채널이 Config.getEnvByKey("text_channel_name")과 다르다면 알림을 주고 종료
    if (!event.author.isBot && channel.name != TEXT_CHANNEL_NAME) {
        channel.sendMessage("채팅 채널 이름이 `$TEXT_CHANNEL_NAME`인 채널에서 요청해야합니다.")
            .queue{
                it.delete().queueAfter(5, TimeUnit.SECONDS)
            }
        return true
    }

    return false
}

fun parseCommand(raw: String): String {
    if (raw.isEmpty()) return ""

    val substring = raw.substring(PREFIX.length)
    val split = substring.split(" ")
    return split[0]
}

fun parseContent(raw: String): String {
    if (raw.isEmpty()) return ""

    // split 후 1번 인덱스부터 끝까지
    val substring = raw.substring(PREFIX.length)
    val split = substring.split(" ")
    val subList = split.subList(1, split.size)
    return subList.joinToString(" ")
}

private fun TextChannel.deleteAllMessages() {
    try {
        val messages = this.iterableHistory
            .takeAsync(1000) // Collect 1000 messages
            .thenApply {
                it.toList()
            }.get()
        // message를 100개 단위로 나눠서 삭제
        log.info("deleteAllMessages - messages.size = ${messages.size}")
        if (messages.isEmpty()) return
        if (messages.size == 1) {
            val channelMessages = this.iterableHistory
                .takeAsync(1) // Collect 1000 messages
                .thenApply {
                    it.toList()
                }
            if(channelMessages.get().isEmpty()) return
            channelMessages.get()[0]
                .delete().queue()
        } else {
            messages.chunked(100).forEach {
                this.deleteMessages(it).queue()
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
    description: String = "이 노래를 재생합니다.",
    author: String,
    duration: Long = 0,
    youtubeIdentity: String = "",
    tracks: List<AudioTrack> = listOf(),
    isPause: Boolean = false,
    isRepeat: Boolean = false,
) {
    var fields = mutableListOf<Field>()

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
    )

    val stopButton = button(
        id = "stopButton",
        style = ButtonStyle.DANGER,
        label = "정지",
    )

    val skipButton = button(
        id = "skipButton",
        style = ButtonStyle.SECONDARY,
        label = "다음곡",
    )

    val repeatButton = button(
        id = "repeatButton",
        label = if(isRepeat) "반복해제" else "반복하기",
        emoji = Emoji.fromUnicode(if(isRepeat) "🔁" else "🔂"),
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


    log.info("youtubeIdentity : $youtubeIdentity")
    val actionRowsMap = mapOf(
        ComponentTypes.BUTTON to listOf(
            playButton,
            stopButton,
            skipButton,
            repeatButton,
        ),
        ComponentTypes.STRING_MENU to listOf(menu)
    )
    sendEmbed(
        title = "$title",
        description = description,
        author = "YT 채널 :$author",
        thumbnail = youtubeIdentity.isNotEmpty().let {  "https://i.ytimg.com/vi/${youtubeIdentity}/hqdefault.jpg"},
        fields = fields,
        actionRowsMap = actionRowsMap,
    )
}

fun TextChannel.sendEmbed(
    title: String,
    description: String = "이 노래를 재생합니다.",
    author: String,
    thumbnail: String?=null,
    fields: List<Field> = listOf(),
    actionRowsMap: Map<ComponentTypes,List<ItemComponent>> = mapOf(),
) {
    // 1. 채널의 기존 메세지 삭제
    this.deleteAllMessages()

    // 2. 새로운 메세지 생성
    val builder = EmbedBuilder()

    fields.forEach {
        builder.addField(it)
    }

    log.info("thumbnail : $thumbnail")
    val messageEmbed = builder.setAuthor("HellBot")
        .setTitle(title)
        .setDescription(description)
        .setAuthor(author)
        .setThumbnail(thumbnail)
        .setColor(Color(0xFF7B96))
        .build()

    val sendMessageEmbeds = this
        .sendMessageEmbeds(messageEmbed)


    if(actionRowsMap.isNotEmpty()) {
        actionRowsMap.forEach { (key, value) ->
            sendMessageEmbeds
                .addActionRow(value)
        }
    }

    sendMessageEmbeds
        .queue()
}