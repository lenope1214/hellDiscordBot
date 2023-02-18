//package kr.wearebaord.hellbot.commands
//
//import io.github.jdiscordbots.command_framework.command.ArgumentTemplate
//import io.github.jdiscordbots.command_framework.command.Command
//import io.github.jdiscordbots.command_framework.command.CommandEvent
//import io.github.jdiscordbots.command_framework.command.ICommand
//import net.dv8tion.jda.api.Permission
//import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
//
//@Command("example", "examplealias") // Step 4
//class Example : ICommand {
//    override fun action(event: CommandEvent) {
//        event.channel.sendMessage("Example command reply").queue()
//    }
//
//    /**
//     * This method is executed on every button click where the button id equals the name of the command.
//     *
//     * @param event A [ButtonInteractionEvent] representing the clicked button.
//     */
//    override fun onButtonClick(event: ButtonInteractionEvent) {
//        super.onButtonClick(event)
//    }
//
//    /**
//     * Return whether command can be executed or not
//     *
//     * @param event A [CommandEvent] representing the invoked command and allowing to respond to the command
//     * @return `true` (default) if command can be executed, otherwise `false`
//     */
//    override fun allowExecute(event: CommandEvent): Boolean {
//        return super.allowExecute(event)
//    }
//
//    /**
//     * Gets the permissions required for executing a command.
//     * Everyone with at least one of those permissons can use the command.
//     * Server administrators can overwrite this.
//     * This overwrites [ICommand.isAvailableToEveryone]
//     *
//     * @return a [Set] containing the required permissions or `null` if everyone should be able to use the command
//     */
//    override fun getRequiredPermissions(): Set<Permission>? {
//        return super.getRequiredPermissions()
//    }
//
//    /**
//     * checks weather this command can be used by anyone without special permissions.
//     *
//     * @return `true` if anyone can use the command by default
//     */
//    override fun isAvailableToEveryone(): Boolean {
//        return super.isAvailableToEveryone()
//    }
//
//    override fun help(): String {
//        return "Example command"
//    }
//
//    /**
//     * gets a list of all parameters the command expects.
//     * The expected parameters should not change.
//     * The order of arguments is preserved.
//     *
//     * @return A [List] containing all argument the command expects
//     */
//    override fun getExpectedArguments(): List<ArgumentTemplate> {
//        return listOf()
//    }
//}