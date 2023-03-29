package kr.weareboard.main

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

@Component
class BotRunner(
    private val bot: BotApplication
) : ApplicationRunner {

    @Value("\${spring.profiles.active}")
    private val profile: String? = null

    override fun run(args: ApplicationArguments) {
        bot.run(profile ?: "PROD")
    }
}
