//package kr.wearebaord.hellbot.commands
//
//import io.github.jdiscordbots.command_framework.command.ArgumentTemplate
//import io.github.jdiscordbots.command_framework.command.Command
//import io.github.jdiscordbots.command_framework.command.CommandEvent
//import io.github.jdiscordbots.command_framework.command.ICommand
//import kr.wearebaord.hellbot.JDA
//import net.dv8tion.jda.api.EmbedBuilder
//import net.dv8tion.jda.api.entities.Message
//import net.dv8tion.jda.api.entities.MessageChannel
//import net.dv8tion.jda.api.entities.emoji.Emoji
//import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
//import net.dv8tion.jda.api.interactions.components.ActionRow
//import net.dv8tion.jda.api.interactions.components.buttons.Button
//import net.dv8tion.jda.api.requests.RestAction
//import org.slf4j.LoggerFactory
//import java.util.concurrent.TimeUnit
//
//
//@Command("button")
//class DemoButtonCommand : ICommand {
//
//    private val log = LoggerFactory.getLogger(DemoButtonCommand::class.java)
//
//    override fun action(event: CommandEvent) {
//        try {
//            val messageEmbed = EmbedBuilder()
//                .setTitle("title - 데모버튼")
//                .setDescription("description - 데모버튼입니다.")
//                .build()
//
//            // 버튼을 추가
//            event
//                .replyWithActionRows(
//                messageEmbed,
//                ActionRow.of(
//                    Button.primary( //
//                        "hello", "Click Me"
//                    ),  // Button with only a label
//                    Button.success(
//                        "emoji", Emoji.fromFormatted("<:minn:245267426227388416>")
//                    ),
//                ),
//            )
//                .queueAfter(3, TimeUnit.SECONDS)
////                .queue{
////                    it
////                }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            log.error("error: ${e.message}")
//        } finally {
//            // delete command message
//            event.message.delete().queue()
//        }
//
//    }
//
//    override fun onButtonClick(event: ButtonInteractionEvent) {
//        log.info("event.componentId: ${event.componentId}")
//        if (event.componentId == "hello") {
//            event.reply("Hello :)").queue()
//        }
//    }
//
//    override fun help(): String {
//        return "데모버튼입니다."
//    }
//
//    override fun getExpectedArguments(): MutableList<ArgumentTemplate> {
//        return mutableListOf()
//    }
//}