package kr.wearebaord.hellbot.common

fun String.isYoutubeUrl(): Boolean {
    return if (Regex("^(https?://)?(www\\.)?youtube\\.com(/watch\\?v=.{11}|/embed/.{11}|/v/.{11}|/.{0,})").matches(this)) {
        println("올바른 YouTube URL입니다.")

        // 만약 this에 shorts가 포함되어 있다면 return false
        if (this.contains("shorts")) {
            println("shorts가 포함되어 있습니다.")
            return false
        }

        true
    } else {
        println("잘못된 URL입니다.")
        false
    }
}