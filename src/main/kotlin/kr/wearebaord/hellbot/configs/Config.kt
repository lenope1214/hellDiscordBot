package kr.wearebaord.hellbot.configs

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import kr.wearebaord.hellbot.PROFILE
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

class Config {

    companion object {
        private const val envDirectory = "."
        private val requiredEnvironments: List<String> = listOf(
            "TOKEN",
            "BOT_ID",
            "PREFIX",
            "TEXT_CHANNEL_ID",
            "TEXT_CHANNEL_NAME",
        )
        private val log: Logger = LoggerFactory.getLogger(Config::class.java)
        private val commonEnv: Dotenv = dotenv{
            directory = envDirectory
            filename = "common.env"
        }

        private lateinit var profileEnv: Dotenv

        private val devEnv: Dotenv = dotenv{
            directory = envDirectory
            filename = "dev.env"
        }

        private val prodEnv: Dotenv = dotenv{
            directory = envDirectory
            filename = "prod.env"
        }

        fun getEnvByKey(key: String): String?{
            val key = key.uppercase(locale = java.util.Locale.getDefault())
            commonEnv.get(key)?.let { return it }
            profileEnv.get(key)?.let { return it }
            return null
        }

        init {
//            dotenv.entries().forEach { println("${it.key} : ${it.value}") }
            // 만약 requiredEnvironments에 해당하는 환경변수가 dotenv에 존재하지 않다면 log.error를 뱉고 프로그램을 종료한다.
            when(PROFILE!!){
                EnvTypes.DEV -> {
                    if (devEnv.entries().none { requiredEnvironments.contains(it.key) }) {
                        log.error("필수 환경변수가 세팅되지 않았습니다. 필수 환경변수: ${requiredEnvironments.joinToString(", ")}")
                        exitProcess(1)
                    }
                    profileEnv = devEnv
                }
                EnvTypes.PROD -> {
                    if (prodEnv.entries().none { requiredEnvironments.contains(it.key) }) {
                        log.error("필수 환경변수가 세팅되지 않았습니다. 필수 환경변수: ${requiredEnvironments.joinToString(", ")}")
                        exitProcess(1)
                    }
                    profileEnv = prodEnv
                }
            }
        }
    }
}