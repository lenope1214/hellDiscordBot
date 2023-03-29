package kr.weareboard.bot.common

import kr.weareboard.main.PREFIX

fun String.isHttpUrl(): Boolean {
    val pattern = "^https?://(?:\\w+\\.)?(?:(?:\\w+\\.)+\\w+/)?(?:\\S+/)*(?:\\S+)?(?:\\?.*)?$"
    // "^(https?://)?(www\\.)?youtube\\.com(/watch\\?v=.{11}|/embed/.{11}|/v/.{11}|/.{0,})"
    return if (Regex(pattern).matches(this)) {
        println("올바른 HTTP URL입니다.")
        true
    } else {
        println("잘못된 URL입니다.")
        false
    }
}

// 올바른 PREFIX로 시작하는지 확인하는 함수
fun String.isCorrectPrefix(): Boolean {
    return this.startsWith(PREFIX, ignoreCase = true)
}

// PREFIX를 제외한 내용을 반환하는 함수
fun String.parseContent(raw: String): String {
    if (raw.isEmpty()) return ""

    // split 후 1번 인덱스부터 끝까지
    val substring = raw.substring(PREFIX.length)
    val split = substring.split(" ")
    val subList = split.subList(1, split.size)
    return subList.joinToString(" ")
}

fun String.parseCommand(): String {
    if (!this.startsWith(PREFIX) || this.isEmpty() || this.length < PREFIX.length) return ""
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
