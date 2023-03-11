package kr.wearebaord.hellbot.common

fun String.isHttpUrl(): Boolean {
    val pattern = "^https?://(?:\\w+\\.)?(?:(?:\\w+\\.)+\\w+/)?(?:\\S+/)*(?:\\S+)?(?:\\?.*)?$"
        //"^(https?://)?(www\\.)?youtube\\.com(/watch\\?v=.{11}|/embed/.{11}|/v/.{11}|/.{0,})"
    return if(Regex(pattern).matches(this)){
        println("올바른 HTTP URL입니다.")
        true
    } else {
        println("잘못된 URL입니다.")
        false
    }
}
