package kr.wearebaord.hellbot

import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class UserLogger(user: User) : ListenerAdapter() {
    private val userId: Long

    init {
        userId = user.idLong
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val author = event.author
        val message = event.message
        if (author.idLong == userId) {
            // Print the message of the user
            println(author.asTag + ": " + message.contentDisplay)
        }
    }

    override fun onGuildJoin(event: GuildJoinEvent) {
        val api = event.jda
        val user = api.getUserById(userId) // Acquire a reference to the User instance through the id
        user!!.openPrivateChannel().queue { channel: PrivateChannel ->
            // Send a private message to the user
            channel.sendMessageFormat("I have joined a new guild: **%s**", event.guild.name).queue()
        }
    }
}