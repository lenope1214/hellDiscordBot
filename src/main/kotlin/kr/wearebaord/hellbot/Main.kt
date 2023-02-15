package kr.wearebaord.hellbot

import kr.wearebaord.hellbot.utils.KoreanUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Integer.max
import java.util.concurrent.ForkJoinPool


val log = LoggerFactory.getLogger("Main") as Logger

fun getThreadCount(): Int = max(2, ForkJoinPool.getCommonPoolParallelism())
class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val name = "박상권"

            val name1 = KoreanUtil.getCompleteWord(name, "을", "를")
            val name2 = KoreanUtil.getCompleteWord(name, "이", "가")
            val name3 = KoreanUtil.getCompleteWord(name, "은", "는")

            println("name1 = ${name1}")
            println("name2 = ${name2}")
            println("name3 = ${name3}")
        }
    }
}

