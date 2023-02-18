package kr.wearebaord.hellbot.commands

import io.github.jdiscordbots.command_framework.command.ArgumentTemplate
import io.github.jdiscordbots.command_framework.command.Command
import io.github.jdiscordbots.command_framework.command.CommandEvent
import io.github.jdiscordbots.command_framework.command.ICommand
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.slf4j.LoggerFactory


//@Command("button")
class DemoButtonCommand : ListenerAdapter(), ICommand {

    private val log = LoggerFactory.getLogger(DemoButtonCommand::class.java)

    override fun onGuildReady(event: GuildReadyEvent) {
        log.info("onGuildReady")
        event.guild.upsertCommand("hello", "데모버튼").queue()
        event.guild.upsertCommand("info", "데모버튼").queue()
    }

    override fun action(event: CommandEvent) {
        try {
            val messageEmbed = EmbedBuilder()
                .setTitle("title - 데모버튼")
                .setDescription("description - 데모버튼입니다.")
                .build()

            // 버튼을 추가
            event.replyWithActionRows(
                messageEmbed,
                ActionRow.of(
                    Button.primary("hello", "Click Me"),  // Button with only a label
                    Button.success(
                        "emoji", Emoji.fromFormatted("<:minn:245267426227388416>")
                    ),
                )
            ).queue()
        } catch (e: Exception) {
            e.printStackTrace()
            log.error("error: ${e.message}")
        } finally {
            // delete command message
            event.message.delete().queue()
        }

    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.name == "hello") {
            val messageEmbed = EmbedBuilder()
                .setTitle("title - 데모버튼")
                .setDescription("description - 데모버튼입니다.")
                .build()

            // 버튼을 추가
            event.reply("Click the button to say hello")
                .addActionRow(
                    Button.primary("hello", "Click Me"), // Button with only a label
                    Button.success("emoji", Emoji.fromFormatted("<:minn:245267426227388416>"))) // Button with only an emoji
                .queue();
//            event.reply("Click the button to say hello")
//                .addActionRow(
//                    Button.primary("hello", "Click Me"), // Button with only a label
//                    Button.success("emoji", Emoji.fromMarkdown("<:minn:245267426227388416>"))) // Button with only an emoji
//                .queue();
        } else if (event.name == "info") {
            event.reply("Click the buttons for more info")
                .addActionRow( // link buttons don't send events, they just open a link in the browser when clicked
                    Button.link("https://github.com/DV8FromTheWorld/JDA", "GitHub")
                        .withEmoji(Emoji.fromFormatted("<:github:849286315580719104>")), // Link Button with label and emoji
                    Button.link("https://ci.dv8tion.net/job/JDA/javadoc/", "Javadocs")) // Link Button with only a label
                .queue();
        }
    }

    override fun onButtonClick(event: ButtonInteractionEvent) {
        log.info("event.componentId: ${event.componentId}")
        if (event.componentId == "hello") {
            event.reply("Hello :)").queue()
        }
    }

    override fun help(): String {
        return "데모버튼 출력"
    }

    /**
     * gets a list of all parameters the command expects.
     * The expected parameters should not change.
     * The order of arguments is preserved.
     * @return A [List] containing all argument the command expects
     */
    override fun getExpectedArguments(): MutableList<ArgumentTemplate> {
        return mutableListOf(
//          ArgumentTemplate("url", "유튜브 링크를 입력해주세요.", true, null)
        )
    }

    override fun allowExecute(event: CommandEvent): Boolean {
        return super.allowExecute(event)
    }
}