package kr.weareboard.bot.exception

class InvalidTextChannel : Exception() {
    override val message: String?
        get() = "텍스트 채널이 잘못되었습니다."
}
