package kr.wearebaord.hellbot.common

fun Long.convertMsToMmSs(): String {
    val seconds = (this / 1000) % 60
    val minutes = (this / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
}