package kr.wearebaord.hellbot.commands

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate
import io.github.jdiscordbots.command_framework.command.Command
import io.github.jdiscordbots.command_framework.command.CommandEvent
import io.github.jdiscordbots.command_framework.command.ICommand
import kr.wearebaord.hellbot.funcs.TextChannelFuncs
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageHistory
import net.dv8tion.jda.api.entities.TextChannel
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URISyntaxException


@Command("clear", "청소")
class ClearCommand : ICommand {
    private val log = LoggerFactory.getLogger(ClearCommand::class.java)

    /**
     * Executes the command.
     * @param event A [CommandEvent] representing from the invoked command and allowing to respond to the command
     */
    override fun action(event: CommandEvent) {
        val channel = event.channel as TextChannel
        TextChannelFuncs.deleteMessage(channel, event)
    }

    override fun help(): String {
        return "헬파티봇 채팅 채널의 최근 채팅 429개를 삭제합니다"

    }

    /**
     * gets a list of all parameters the command expects.
     * The expected parameters should not change.
     * The order of arguments is preserved.
     * @return A [List] containing all argument the command expects
     */
    override fun getExpectedArguments(): MutableList<ArgumentTemplate> {
        return mutableListOf()
    }

    override fun allowExecute(event: CommandEvent): Boolean {
        return super.allowExecute(event)
    }
}