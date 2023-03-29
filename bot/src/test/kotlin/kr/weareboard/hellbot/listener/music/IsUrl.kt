package kr.weareboard.hellbot.listener.music

fun String.parseCommand(): String {
    val prefix = "$!"
    val prefixLength = prefix.length
    println("!this.startsWith(prefix) = ${!this.startsWith(prefix)}")
    println("this.isEmpty() = ${this.isEmpty()}")
    println("this.length < prefixLength = ${this.length < prefixLength}")
    if (!this.startsWith(prefix) || (this.isEmpty() || this.length < prefixLength)) return ""
    return this.substring(prefixLength).split(" ")[0]
}

fun main() {
    val url = "https://www.youtube.com/watch?v=1"
    val url2 = "https://youtube.com/shorts/ihPtIR2gfvM?feature=share"
    val url3 = "https://www.youtube.com/watch?v=12345678901"
    val url4 = "https://youtu.be/emk4Yoredik"
    val url5 = "https://youtu.be/NMA_isZYsYQ"

    val una = "우 나 핫걸~"
    val parseCommand = una.parseCommand()
    println("parseCommand = ${parseCommand}")
    when(parseCommand){
        in listOf("sk", "skip", "나", "나ㅑㅔ", "넘기기", "다음", "next", "nt", "nxt") -> {
            println("우나핫걸")
        }
    }

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
