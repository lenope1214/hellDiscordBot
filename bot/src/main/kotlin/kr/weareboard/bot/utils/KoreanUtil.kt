package kr.wearebaord.hellbot.utils

object KoreanUtil {
    fun getCompleteWord(
        name: String,
        firstValue: String,
        secondValue: String,
        onlySelectedValue: Boolean = true
    ): String {
        val lastName = name[name.length - 1]

        // 한글의 제일 처음과 끝의 범위밖일 경우는 오류
        if (lastName.code < 0xAC00 || lastName.code > 0xD7A3) {
            return name
        }
        val selectedValue = if ((lastName.code - 0xAC00) % 28 > 0) firstValue else secondValue
        return if (onlySelectedValue) { selectedValue } else { name + selectedValue }
    }
}
