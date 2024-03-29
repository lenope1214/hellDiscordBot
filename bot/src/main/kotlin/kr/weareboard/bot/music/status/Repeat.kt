package kr.weareboard.bot.music.status

import kr.weareboard.bot.domain.enums.EmojiValue
import net.dv8tion.jda.api.entities.emoji.Emoji

fun getRepeatText(isRepeat: Boolean): String =
    if (isRepeat) "반복해제" else "반복하기"

fun getRepeatEmoji(isRepeat: Boolean): Emoji =
    if (isRepeat) EmojiValue.SINGLE.fromUnicode() else EmojiValue.INFINITY.fromUnicode()
