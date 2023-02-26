package kr.wearebaord.hellbot.listeners

import kr.wearebaord.hellbot.BOT_ID
import kr.wearebaord.hellbot.JDA
import kr.wearebaord.hellbot.TEXT_CHANNEL_NAME
import kr.wearebaord.hellbot.makeMessage
import kr.wearebaord.hellbot.music.PlayerManager
import kr.wearebaord.hellbot.utils.KoreanUtil
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.events.thread.member.ThreadMemberLeaveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.slf4j.LoggerFactory

object DefaultListener : ListenerAdapter() {
    val log = LoggerFactory.getLogger(DefaultListener::class.java)

    override fun onReady(event: ReadyEvent) {
        log.info("Logged in as ${event.jda.selfUser.name}")

        // Sets the global command list to the provided commands (removing all others)

        // TODO addCommands는 일일 제한이 있어서 테스트 시에 조심해야 함
        event.jda.updateCommands().addCommands(
            Commands.slash("ping", "Calculate ping of the bot"),
            Commands.slash("놀리기", "대상을 놀립니다.")
                .addOption(
                    OptionType.STRING,
                    "nickname",
                    "놀릴 대상의 닉네임(이름)을 입력해주세요",
                    true
                ),
            Commands.slash("사실부탁임", "사실부탁임 출력")
        ).queue()
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        // 멤버가 나갔을 때 취할 행동
        // 해당 음성 채널에 모두가 나갔다면 메세지를 남기고 leave
//        log.info("==============멤버가 들어오거나 나갔음===============")
        // 어떠한 멤버가 들어왔거나 나갔을 때,
        val channelLeft = event.channelLeft // 나갔다면 null이 아님,
//        val channelJoined = event.channelJoined // 들어왔다면 null이 아님
        val member = event.member

        // 나갔다면
        if (channelLeft != null) {
//            log.info("${member}가 ${channelLeft?.name}음성 채널에서 나감")
            // 현재 서버에 남아있는 멤버 수
            // member.defaultChannel?.members?.size ?: 0

            val leftChannel = channelLeft.asVoiceChannel()
            val leftMemberSize = leftChannel.members.size
//            log.info("leftChannel: $leftChannel")
//            log.info("leftMemberSize: $leftMemberSize")
            if (leftMemberSize == 1) {
                val leftMember = leftChannel.members[0]
                log.info("leftMember: $leftMember")
                // 만약 헬파티봇이 혼자 남아있다면 내쫓는다.
                if(leftMember.id == BOT_ID){
                    val textChannel = leftChannel.guild.textChannels.find {
                        it.name == TEXT_CHANNEL_NAME
                    }
                    PlayerManager.INSTANCE.leftChannel(
                        guild = leftChannel.guild,
                        channel = textChannel,
                    )
                }
//                member.defaultChannel?.sendMessage("${member}님이 나가셨습니다.")?.queue()
//                member.defaultChannel?.leave()?.queue()
            }
        }
    }

    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        // make sure we handle the right command
        // 만약 개인채팅이면 "개인 채팅에서 사용은 불가능합니다." 메세지와 함께 종료
        if (event.guild == null) {
            event.reply("개인 채팅에서 사용은 불가능합니다.").setEphemeral(true).queue()
            return
        }
        when (event.name) {
            "ping" -> {
                val time = System.currentTimeMillis()
                event.reply("Pong!").setEphemeral(false) // reply or acknowledge
                    .flatMap { event.hook.editOriginalFormat("Pong: %d ms", System.currentTimeMillis() - time) }
                    .queue()
            }

            "놀리기" -> {
                if (!event.member?.hasPermission(Permission.MESSAGE_SEND)!!) {
                    event.reply("You cannot Send Message ;)").setEphemeral(true).queue()
                    return
                }
                val nickname = event.getOption("nickname", OptionMapping::getAsString)
                if (nickname.isNullOrBlank()) {
                    event.reply("닉네임을 입력해주세요").setEphemeral(true).queue()
                }

                // get mention info
//                val mention = event.getOption("nickname", OptionMapping::getAsMentionable)
//                val mentionName = mention?.asMention?.replace("@", "")?.replace("!", "")

                val str = """
                    부럽다

                    부러워

                    앞에 걷는 저 사람이 부러워

                    뒷모습밖에 보이지 않지만 부러워

                    나도 되고 싶어

                    나도 `${nickname}`${KoreanUtil.getCompleteWord(nickname!!, "이", "가")} 되고 싶어

                    너였구나

                    앞에서 걷던 그 사람이 너였어

                    너만 보면 내 세상이 무너져

                    그리고

                    지금도 무너지고 있다
                """.trimIndent() // 앞뒤공백제거

                makeMessage(event, str)
            }

            "사실부탁임" -> {
                makeMessage(
                    event, """
                    사실 부탁임...

                    근데 어쩔 수 없음 ㅠㅠ

                    내가 뭘 할 수 있는 게 없음 ㅜㅜㅜㅜ

                    `${event.user.name}`${
                        KoreanUtil.getCompleteWord(
                            event.user.name!!,
                            "은",
                            "는",
                        )
                    } 좆밥 새끼임 ㅠ

                    존나 화내거나 욕하는 거 말고 할 수 있는 게 없음 ㅠㅠ
                """.trimIndent()
                )
            }
        }
    }
}