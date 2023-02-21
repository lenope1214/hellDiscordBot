package kr.wearebaord.hellbot.common

fun String.isYoutubeUrl(): Boolean {
    return if (Regex("^(https?://)?(www\\.)?youtube\\.com(/watch\\?v=.{11}|/embed/.{11}|/v/.{11}|/.{0,})").matches(this)) {
        println("올바른 YouTube URL입니다.")
        true
    } else {
        println("잘못된 URL입니다.")
        false
    }
}