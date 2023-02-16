package kr.wearebaord.hellbot.configs

import io.github.cdimascio.dotenv.Dotenv
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class Config {

    companion object{
        private val requiredEnvironments: List<String> = listOf(
            "TOKEN",
            "PREFIX",
        )
        private val log: Logger= LoggerFactory.getLogger(Config::class.java)
        private val dotenv: Dotenv = Dotenv.load();
        fun getEnvByKey(key: String) = dotenv[key.uppercase()]

        init {
            dotenv.entries().forEach { println("${it.key} : ${it.value}") }
            // 만약 requiredEnvironments에 해당하는 환경변수가 dotenv에 존재하지 않다면 log.error를 뱉고 프로그램을 종료한다.
            if (dotenv.entries().none { requiredEnvironments.contains(it.key) }) {
                log.error("필수 환경변수가 세팅되지 않았습니다. 필수 환경변수: ${requiredEnvironments.joinToString(", ")}")
                exitProcess(1)
            }
        }
    }
}