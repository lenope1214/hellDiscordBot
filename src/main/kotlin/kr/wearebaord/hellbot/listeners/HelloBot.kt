package kr.wearebaord.hellbot.listeners

import io.github.jdiscordbots.command_framework.command.Command
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.buttons.Button

object HelloBot : ListenerAdapter() {

    val log = org.slf4j.LoggerFactory.getLogger(HelloBot::class.java)

    override fun onGuildReady(event: GuildReadyEvent) {
        log.info("GuildReadyEvent: ${event.guild.name}")

        // add slash command
//        event.guild.upsertCommand(
//            Commands.slash("hello", "데모버튼"),
//        ).queue()
//        event.guild.upsertCommand(
//            Commands.slash("info", "데모버튼2"),
//        ).queue()

        event.jda.updateCommands().addCommands(
            Commands.slash("hello", "데모버튼"),
            Commands.slash("info", "데모버튼2"),
        ).queue()

    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        if (event.guild == null) {
            event.reply("개인 채팅에서 사용은 불가능합니다.").setEphemeral(true).queue()
            return
        }

        log.info("event: ${event.name}")
        if (event.name == "hello") {
            event.reply("Click the button to say hello").setEphemeral(true).queue()
            event.reply("reply 2").setEphemeral(true).queue()
            event.replyEmbeds(
                net.dv8tion.jda.api.EmbedBuilder()
                    .setTitle("title - 데모버튼")
                    .setDescription("description - 데모버튼입니다.")
                    .build()
            ).setEphemeral(true)
                .addActionRow(
                    Button.primary("hello", "Click Me"),  // Button with only a label
                    Button.success("emoji", Emoji.fromFormatted("<:minn:245267426227388416>"))
                ) // Button with only an emoji
                .queue({
                    log.info("it: ${it}")
                }, {
                    log.error("error: ${it.message}")
                })
        } else if (event.name == "info") {
            event.reply("Click the buttons for more info")
                .addActionRow( // link buttons don't send events, they just open a link in the browser when clicked
                    Button.link("https://github.com/DV8FromTheWorld/JDA", "GitHub")
                        .withEmoji(Emoji.fromFormatted("<:github:849286315580719104>")),  // Link Button with label and emoji
                    Button.link("https://ci.dv8tion.net/job/JDA/javadoc/", "Javadocs")
                ) // Link Button with only a label
                .queue()
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        when (event.componentId) {
            "hello" -> event.reply("Hello :)").queue() // send a message in the channel
            "emoji" -> event.editMessage("That button didn't say click me").queue() // update the message
        }
    }
}