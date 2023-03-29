package kr.weareboard.bot.exception

class MusicTitleIsNullException(message: String?) : Exception(message) {
    constructor() : this("Music title is null")
}
