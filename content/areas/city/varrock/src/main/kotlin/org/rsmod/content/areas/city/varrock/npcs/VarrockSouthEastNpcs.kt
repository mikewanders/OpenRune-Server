package org.rsmod.content.areas.city.varrock.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onAiTimer
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.content.quest.manager.QuestRequirementMode
import org.rsmod.content.quest.manager.QuestRequirements
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private var Player.abyssMiniquest by intVarp("varp.abyssal_miniquest")

private enum class MageTopic(val option: String) {
    Incantation("About that incantation your initiate gave me..."),
    Abyss("Can you tell me more about the Abyss?"),
    Group("Can you tell me more about your group?"),
}

private enum class GaryTopic(val option: String) {
    WhoAreYou("Who are you?"),
    Cat("Is the cat supposed to help you get runes too?"),
    Magic("If you're a wizard, are you doing anything magical?"),
    Bye("Bye, Gary."),
}

class VarrockSouthEastNpcs @Inject constructor(private val random: GameRandom) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.pmod_town_crier_varrock") { startDialogue(it.npc) { townCrier() } }
        onAiTimer("npc.pmod_town_crier_varrock") {
            val (anim, shout) = random.pick(CRIER_SHOUTS)
            npc.anim(anim)
            npc.say(shout)
        }

        onOpNpc1("npc.dimintheis") { startDialogue(it.npc) { dimintheis() } }
        onOpNpc1("npc.guidors_wife") { startDialogue(it.npc) { julie() } }
        onOpNpc1("npc.bioguard1") {
            startDialogue(it.npc) {
                chatNpc(
                    neutral,
                    "Please don't disturb me, I've got to keep any eye out for suspicious " +
                        "individuals.",
                )
            }
        }

        onOpNpc1("npc.rcu_zammy_mage1_edgeb") { startDialogue(it.npc) { mageOfZamorak() } }
        onPlayerLogin { player.syncAbyssMiniquest() }

        onOpNpc1("npc.25th_host") { startDialogue(it.npc) { wizardGary() } }
    }

    private suspend fun Dialogue.townCrier() {
        chatNpc(happy, "Hello citizen!")
        if (choice2("What do you do around here?", true, "See you later.", false)) {
            chatPlayer(quiz, "What do you do around here?")
            chatNpc(
                happy,
                "I'm a Town Crier. It's my job to let people know of any recent news. After all, " +
                    "there's all sorts of things happening around here.",
            )
            chatPlayer(happy, "I see. See you later.")
        } else {
            chatPlayer(happy, "See you later.")
        }
        chatNpc(happy, "Until next time.")
    }

    private suspend fun Dialogue.dimintheis() {
        if (!choice2("Have you got any quests for me?", true, "Other", false)) {
            return
        }
        chatPlayer(
            neutral,
            "Do you have any quests for me? I really want one. I really want to help out!",
        )
        mesbox(
            "You murmur under your breath something about how great the rewards were last time " +
                "you did a quest for Dimintheis."
        )
        chatNpc(
            happy,
            "My family consider what you did for us a great service. We couldn't possibly " +
                "trouble you further.",
        )
        chatPlayer(
            quiz,
            "There must be SOMETHING you need help with. I am a great adventurer after all.",
        )
        chatNpc(neutral, "There really isn't anything, I am very content with life right now.")
        val muttered =
            choice2(
                "I guess money does buy happiness.",
                true,
                "Oh come on, however menial, I want to help!",
                false,
            )
        if (!muttered) {
            chatPlayer(neutral, "Oh come on, however menial, I want to help!")
            return
        }
        chatPlayer(shifty, "I guess money really does buy happiness.")
        chatNpc(angry, "What was that? I didn't quite catch that.")
        chatPlayer(shifty, "Nothing...")
    }

    private suspend fun Dialogue.julie() {
        chatPlayer(neutral, "Hello.")
        chatNpc(
            sad,
            "Oh hello, I can't chat now. I have to keep an eye on my husband. He's very ill!",
        )
        chatPlayer(sad, "I'm sorry to hear that!")
    }

    private suspend fun Dialogue.mageOfZamorak() {
        chatNpc(neutral, "Ah, you again. What do you want?")
        var last: MageTopic? = null
        while (true) {
            val topics = MageTopic.entries.filter { it != last }
            val leave =
                when (last) {
                    null -> "Nothing. I'm just looking around."
                    MageTopic.Incantation -> "I'd better be off."
                    else -> "No thanks."
                }
            val options = topics.map { it.option } + leave
            val picked =
                when (options.size) {
                    3 -> choice3(options[0], 0, options[1], 1, options[2], 2)
                    else -> choice4(options[0], 0, options[1], 1, options[2], 2, options[3], 3)
                }
            val topic = topics.getOrNull(picked)
            if (topic == null) {
                chatPlayer(neutral, leave)
                return
            }
            when (topic) {
                MageTopic.Incantation -> mageIncantation()
                MageTopic.Abyss -> mageAbyss()
                MageTopic.Group -> mageGroup()
            }
            last = topic
        }
    }

    private suspend fun Dialogue.mageAbyss() {
        chatPlayer(quiz, MageTopic.Abyss.option)
        chatNpc(
            neutral,
            "It is a hard place to describe. We often refer to it as another plane, but that " +
                "isn't quite accurate. If anything, it is more like a plane that sits between " +
                "all other planes.",
        )
        chatPlayer(confused, "Right... And what does it have to do with runecrafting?")
        chatNpc(
            neutral,
            "In truth, nothing at all. However, it has everything to do with teleportation. You " +
                "see, time and space work differently in the Abyss.",
        )
        chatNpc(
            neutral,
            "For example, you can travel to the Abyss, walk a few steps and then exit, only to " +
                "find yourself somewhere completely different from where you entered. You might " +
                "even find yourself on a different plane.",
        )
        chatNpc(
            neutral,
            "You can also enter the Abyss and spend days there, but on your return, only " +
                "moments have passed.",
        )
        chatNpc(
            neutral,
            "This is why the Abyss is so useful. It can be used as an effective hub for " +
                "teleportation. In fact, all teleportation that we know of uses the Abyss, you " +
                "just don't realise it.",
        )
        chatPlayer(quiz, "Wait... So when I teleport, I'm actually travelling through the Abyss?")
        chatNpc(
            neutral,
            "In some ways, I suppose you are. Whenever you use a teleport spell, that spell is " +
                "actually travelling through the Abyss and using it to link two places together.",
        )
        chatPlayer(quiz, "So did you discover the Abyss?")
        chatNpc(
            neutral,
            "No. Knowledge of the Abyss has existed for a long time, hence it being used for " +
                "teleportation. However, we believe we may be the first to have properly gained " +
                "access in centuries.",
        )
        chatPlayer(quiz, "How did you manage it?")
        chatNpc(
            neutral,
            "By complete accident. One of our initiates was performing a routine teleportation " +
                "experiment when something went wrong. Instead of ending up at their " +
                "destination, they found themselves in the Abyss.",
        )
        chatNpc(quiz, "Now, did you want something else?")
    }

    private suspend fun Dialogue.mageGroup() {
        chatPlayer(quiz, MageTopic.Group.option)
        chatNpc(
            neutral,
            "We are a group of mages in service to Zamorak. Our group is called the Zamorak " +
                "Magical Institute, or Z.M.I. for short.",
        )
        chatNpc(
            angry,
            "Few actually know of us. Saradominist groups like the Order of Wizards hold sway " +
                "over these lands, so we are forced to work in the shadows. However, make no " +
                "mistake, our power far exceeds theirs.",
        )
        chatPlayer(quiz, "You don't seem to like the Order of Wizards very much.")
        chatNpc(
            angry,
            "And why would we? Did you know that the Order of Wizards wasn't always a " +
                "Saradominist group? Once they allowed mages of all faiths to study with them, " +
                "but they became greedy.",
        )
        chatPlayer(quiz, "What happened?")
        chatNpc(
            angry,
            "They started to desire more control. They banned Zamorakian wizards from certain " +
                "areas of study, claiming them to be too dangerous. In reality, they just wanted " +
                "that knowledge for themselves.",
        )
        chatNpc(
            angry,
            "It all went wrong for them, of course. A Zamorakian wizard made a great " +
                "breakthrough in teleportation magic, but the Saradominists stole his research.",
        )
        chatNpc(
            angry,
            "They used it to perform a ritual, but their lack of understanding caused it to go " +
                "wrong. The entire Wizards' Tower burnt down. Many were killed, and years of " +
                "research was destroyed.",
        )
        chatNpc(
            angry,
            "Naturally, they blamed the Zamorakians. Claimed we intentionally burnt the tower " +
                "down. After the tower was rebuilt, we were banished from the Order of Wizards " +
                "and forced underground.",
        )
        chatPlayer(quiz, "And that's how the Z.M.I. was formed?")
        chatNpc(neutral, "Exactly.")
        chatNpc(quiz, "Now, did you want something else?")
    }

    private suspend fun Dialogue.mageIncantation() {
        chatPlayer(quiz, MageTopic.Incantation.option)
        chatNpc(
            angry,
            "Ah yes, the one to trace that amulet. Don't think I didn't notice that you modified " +
                "the transmission spell I put on it. Next time you need help, we might not be so " +
                "willing.",
        )
        chatPlayer(
            confused,
            "Well maybe you should have asked first! Besides, you wouldn't have got anything " +
                "useful out of it. In tracing that amulet, we accidentally managed to open an " +
                "uncontrollable rift to the Abyss.",
        )
        chatNpc(quiz, "And does this 'we' include the Order of Wizards?")
        chatPlayer(confused, "Maybe...")
        chatNpc(
            angry,
            "I knew it! It's just like them to play with magic they don't understand. First they " +
                "burnt down the Wizards' Tower, now they're opening uncontrollable abyssal rifts!",
        )
        chatNpc(angry, "Perhaps the fools should leave the dangerous magic to us next time!")
        chatPlayer(
            quiz,
            "Oh, that reminds me. That incantation didn't appear to be written on parchment. " +
                "What was it?",
        )
        chatNpc(quiz, "Hmm... probably the flesh of an abyssal creature. Why do you ask?")
        chatPlayer(happy, "Oh, I just thought it might be human flesh. I'm relieved it wasn't!")
        chatNpc(
            angry,
            "You've been spending too much time with your friends in the Order of Wizards! I " +
                "know that we Zamorakians have a bad reputation, but do you really think we'd be " +
                "so barbaric?",
        )
    }

    /**
     * The Mage of Zamorak only shows Talk-to once Enter the Abyss has begun, which the client
     * reads from the real miniquest varp, so a quest-assuming world has to set it.
     */
    private fun Player.syncAbyssMiniquest() {
        if (QuestRequirements.activePolicy().mode != QuestRequirementMode.AssumeCompleted) {
            return
        }
        if (abyssMiniquest == 0) {
            abyssMiniquest = ABYSS_MINIQUEST_COMPLETE
        }
    }

    private suspend fun Dialogue.wizardGary() {
        chatNpc(neutral, "Hello. What brings you here?")
        var topics = listOf(GaryTopic.WhoAreYou, GaryTopic.Magic, GaryTopic.Bye)
        while (true) {
            val topic =
                when (topics.size) {
                    2 -> choice2(topics[0].option, topics[0], topics[1].option, topics[1])
                    else ->
                        choice3(
                            topics[0].option,
                            topics[0],
                            topics[1].option,
                            topics[1],
                            topics[2].option,
                            topics[2],
                        )
                }
            topics =
                when (topic) {
                    GaryTopic.WhoAreYou -> {
                        chatPlayer(shifty, topic.option)
                        chatNpc(
                            happy,
                            "I'm Gary, the wizard. I've not actually been qualified for very " +
                                "long, so I'm living here near the rune shop so I can keep " +
                                "practicing the magical arts.",
                        )
                        chatNpc(
                            confused,
                            "I seem to be raising a cat, too, ever since some local lady " +
                                "persuaded me to buy one off her.",
                        )
                        listOf(GaryTopic.Cat, GaryTopic.Magic, GaryTopic.Bye)
                    }
                    GaryTopic.Cat -> {
                        chatPlayer(laugh, topic.option)
                        chatNpc(
                            angry,
                            "No! I know there are places where people think it's normal to swap " +
                                "cats for runes, but that will NOT happen to Sookie!",
                        )
                        listOf(GaryTopic.Magic, GaryTopic.Bye)
                    }
                    GaryTopic.Magic -> {
                        chatPlayer(quiz, topic.option)
                        chatNpc(
                            bored,
                            "Always! But I'm not looking for any assistance from passing " +
                                "adventurers, if that's what you were thinking. Maybe another time.",
                        )
                        listOf(GaryTopic.WhoAreYou, GaryTopic.Bye)
                    }
                    GaryTopic.Bye -> {
                        chatPlayer(happy, topic.option)
                        return
                    }
                }
        }
    }

    private companion object {
        const val ABYSS_MINIQUEST_COMPLETE = 4

        val CRIER_SHOUTS =
            listOf(
                "seq.town_crier_bell_ring" to "Check out our newsposts for all the latest info!",
                "seq.town_crier_bell_ring" to
                    "The Fractured Archive needs adventurers to protect it!",
                "seq.town_crier_scratch_head" to
                    "Head to the Guthixian Temple to enter The Fractured Archive!",
            )
    }
}
