package kr.wearebaord.hellbot.music.enums

import net.dv8tion.jda.api.entities.emoji.Emoji

enum class EmojiValue(val value: String) {
    PLAY("▶"),
    PAUSE("⏸"),
    EXIT("✖"),
    INFINITY("♾"),
    SINGLE("🔂")
    ;
    fun fromUnicode(): Emoji {
        return Emoji.fromUnicode(this.value)
    }
}
