package kr.weareboard.hellbot.listener.music

import kr.wearebaord.hellbot.listeners.music.PlayListener
import java.net.URI
import java.net.URISyntaxException

fun main() {
//    val url = "https://www.youtube.com/watch?v=1"
//    println(isUrl(url))
//
//    val url2 = "https://youtube.com/shorts/ihPtIR2gfvM?feature=share"
//    println(isUrl(url2))

    val url3 = "https://www.youtube.com/watch?v=12345678901"
    println(isUrl2(url3))

    val url4 = "https://youtube.com/shorts/ihPtIR2gfvM?feature=share"
    println(isUrl2(url4))

}

private fun isUrl2(url: String): Boolean =
    if (Regex("^(https?://)?(www\\.)?youtube\\.com(/watch\\?v=.{11}|/embed/.{11}|/v/.{11}|/.{0,})").matches(url)) {
        println("올바른 YouTube URL입니다.")
        true
    } else {
        println("잘못된 URL입니다.")
        false
    }

private fun isUrl(url: String): Boolean {
    // check if url is valid
    try {
        // is url
        URI(url)
        // check start with https://www.youtube.com/watch?v=
//        if (!Regex("^(https?://)?(www\\.)?youtube\\.com").matches(url)) {
//            throw URISyntaxException(url, "url is not youtube url")
//        }

        if (Regex("^(https?://)?(www\\.)?youtube\\.com(/watch\\?v=.{11}|/embed/.{11}|/v/.{11}|/.{0,})\n").matches(url)) {
            println("올바른 YouTube URL입니다.")
        } else {
            throw URISyntaxException(url, "잘못된 URL입니다.")
        }

        return true
    } catch (e: URISyntaxException) {
        PlayListener.log.error("Uri is not valid! ${e.message}")
        return false
    }
}