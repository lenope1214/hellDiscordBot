package kr.wearebaord.hellbot.commands

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate
import io.github.jdiscordbots.command_framework.command.Command
import io.github.jdiscordbots.command_framework.command.CommandEvent
import io.github.jdiscordbots.command_framework.command.ICommand
import kr.wearebaord.hellbot.JDA
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.requests.RestAction
import org.slf4j.LoggerFactory


@Command("button")
class DemoButtonCommand : ICommand {

    private val log = LoggerFactory.getLogger(DemoButtonCommand::class.java)

    override fun action(event: CommandEvent) {
        try {
            val messageEmbed = EmbedBuilder()
                .setTitle("title - 데모버튼")
                .setDescription("description - 데모버튼입니다.")
                .build()

            // 버튼을 추가
//            val embed = EmbedBuilder()
//                .setColor(0x00ff00)
//                .setTitle("음..타이틀")
//                .setImage("https://namu.wiki/w/%EB%A1%9C%EC%8A%A4%ED%8A%B8%EC%95%84%ED%81%AC/%EA%B5%B0%EB%8B%A8%EC%9E%A5%20%EB%A0%88%EC%9D%B4%EB%93%9C/%EC%BF%A0%ED%81%AC%EC%84%B8%EC%9D%B4%ED%8A%BC")
//                .setDescription("설명란")

            val channel: MessageChannel = event.channel
            val time = System.currentTimeMillis()
            val action: RestAction<Message> = channel.sendMessage("Calculating Response Time...")
            val message: Message = action.complete()
            message.editMessage("Response Time: " + (System.currentTimeMillis() - time) + "ms").queue()
            // End with queue() to not block the callback thread!

            event.replyWithActionRows(
                messageEmbed,
                ActionRow.of(
                    Button.primary( //
                        "hello", "Click Me"
                    ),  // Button with only a label
                    Button.success(
                        "emoji", Emoji.fromFormatted("<:minn:245267426227388416>")
                    ),
                ),

            )
                .complete()
            message.editMessage("Response Time: " + (System.currentTimeMillis() - time) + "ms").queue()
//                .queue({
//                log.info("success: ${it.embeds[0].title}")
//                it.embeds.forEach{
//                    log.info("embeds: ${it.title}")
//                }
//            }) { log.error("error: ${it.message}") }
        } catch (e: Exception) {
            e.printStackTrace()
            log.error("error: ${e.message}")
        } finally {
            // delete command message
            event.message.delete().queue()
        }

    }

    override fun onButtonClick(event: ButtonInteractionEvent) {
        log.info("event.componentId: ${event.componentId}")
        if (event.componentId == "hello") {
            event.reply("Hello :)").queue()
        }
    }

    override fun help(): String {
        return "데모버튼입니다."
    }

    override fun getExpectedArguments(): MutableList<ArgumentTemplate> {
        return mutableListOf()
    }
}