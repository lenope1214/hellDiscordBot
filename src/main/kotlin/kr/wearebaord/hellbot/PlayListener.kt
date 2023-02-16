package kr.wearebaord.hellbot

import dev.minn.jda.ktx.generics.getChannel
import kr.wearebaord.hellbot.music.PlayerManager
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceVideoEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class PlayListener: ListenerAdapter() {

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        val channel = event.channel
        // self member
        val self: Member = event.guild!!.selfMember
        val selfVoiceState = self!!.voiceState


        if (!selfVoiceState!!.inAudioChannel()) {
            channel.sendMessage("봇이 음성채널에 있어야 합니다.").queue()
            //TODO 스스로 음성채널에 들어가기
            return
        }

        val member = event.member
        val memberVoiceState = member!!.voiceState

        if (!memberVoiceState!!.inAudioChannel()) {
            channel.sendMessage("음성채널에 들어가주세요.").queue()
            return
        }

        if(memberVoiceState.channel != selfVoiceState.channel) {
            channel.sendMessage("봇과 같은 음성채널에 있어야 합니다.").queue()
            return
        }

        PlayerManager.INSTANCE
            .loadandPlay(channel as TextChannel, event.getOption("url").toString())
    }
}