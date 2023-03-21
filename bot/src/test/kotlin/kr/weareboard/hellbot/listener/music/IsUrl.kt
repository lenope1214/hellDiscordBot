package kr.weareboard.hellbot.listener.music

fun main() {
    val url = "https://www.youtube.com/watch?v=1"
    val url2 = "https://youtube.com/shorts/ihPtIR2gfvM?feature=share"
    val url3 = "https://www.youtube.com/watch?v=12345678901"
    val url4 = "https://youtu.be/emk4Yoredik"
    val url5 = "https://youtu.be/NMA_isZYsYQ"

    listOf(
        url,
        url2,
        url3,
        url4,
        url5
    ).forEach {
        println(isUrl3(it))
    }
}

private fun isUrl3(url: String): Boolean {
    val pattern = "^https?://(?:\\w+\\.)?(?:(?:\\w+\\.)+\\w+/)?(?:\\S+/)*(?:\\S+)?(?:\\?.*)?$"
    return if (Regex(pattern).matches(url)) {
        println("올바른 http URL입니다.")
        true
    } else {
        println("잘못된 http URL입니다.")
        false
    }
}
