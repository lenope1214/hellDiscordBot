package kr.weareboard.bot.service.interfaces

import kr.weareboard.bot.domain.enums.ComponentTypes
import kr.weareboard.bot.music.PlayTrackInfo
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.requests.restaction.MessageEditAction

interface TextChannelService {

    fun deleteAllMessages(channel: MessageChannel)

    fun sendYoutubeEmbed(
        channel: TextChannel,
        url: String,
        title: String,
        description: String = "",
        author: String,
        duration: Long = 0,
        youtubeIdentity: String = "",
        playTrackInfoList: List<PlayTrackInfo>,
        isPause: Boolean = false,
        isRepeat: Boolean = false
    )

    fun sendEmbed(
        channel: TextChannel,
        url: String = "",
        title: String = "",
        description: String = "",
        author: String = "",
        thumbnail: String? = null,
        fields: List<MessageEmbed.Field> = listOf(),
        actionRowsMap: Map<ComponentTypes, List<ItemComponent>> = mapOf(),
        footerText: String? = null,
        footerIconUrl: String? = null
    )

    fun sendEmbedWithDefaultMessage(
        channel: TextChannel,
        url: String = "",
        title: String = "",
        description: String = "",
        author: String = "",
        thumbnail: String? = null,
        fields: List<MessageEmbed.Field> = listOf(),
        actionRowsMap: Map<ComponentTypes, List<ItemComponent>> = mapOf(),
        footerText: String? = null,
        footerIconUrl: String? = null
    )

    fun sendPleaseEnterVoiceChannel(channel: TextChannel)

    fun sendDefaultMessage(channel: TextChannel)
    fun sendFirstMessage(channel: TextChannel)

    fun updateActionRows(
        actionRowsMap: Map<ComponentTypes, List<ItemComponent>>,
        sendMessageEmbeds: MessageEditAction
    )
}
