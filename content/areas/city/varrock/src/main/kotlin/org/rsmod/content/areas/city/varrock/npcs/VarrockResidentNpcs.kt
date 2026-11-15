package org.rsmod.content.areas.city.varrock.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc4
import org.rsmod.api.script.onOpNpc5
import org.rsmod.content.areas.city.falador.npcs.MakeoverType
import org.rsmod.content.areas.city.falador.npcs.hairdresserMakeover
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private enum class EmmaTopic(val option: String) {
    Business("Does this aggressive business model always work?"),
    WhoAreYou("Who are you?"),
    Suzie("Who is that girl over there?"),
}

class VarrockResidentNpcs : PluginScript() {
    override fun ScriptContext.startup() {
        for (child in listOf("npc.philop", "npc.kanel")) {
            onOpNpc1(child) { startDialogue(it.npc) { busyChild() } }
        }
        val clients =
            listOf(
                "npc.con_contract_client_1",
                "npc.con_contract_client_2",
                "npc.con_contract_client_3",
            )
        for (client in clients) {
            onOpNpc1(client) { startDialogue(it.npc) { chatNpc(confused, BUSY) } }
        }

        onOpNpc1("npc.hairdresser_boutique") { startDialogue(it.npc) { emma() } }
        onOpNpc4("npc.hairdresser_boutique") { boutiqueMakeover(it.npc, MakeoverType.Hair) }
        onOpNpc5("npc.hairdresser_boutique") { boutiqueMakeover(it.npc, MakeoverType.FacialHair) }
        onOpNpc1("npc.tailor_boutique") { startDialogue(it.npc) { suzie() } }
    }

    private suspend fun Dialogue.busyChild() {
        chatPlayer(happy, "Hello youngster!")
        mesbox("The child is too busy playing to take any notice.")
    }

    private suspend fun ProtectedAccess.boutiqueMakeover(emma: Npc, type: MakeoverType) {
        startDialogue(emma) { hairdresserMakeover(type) }
    }

    private suspend fun Dialogue.emma() {
        chatNpc(
            happy,
            "Hello. Let's get to business. I got scissors. You got hair. Want to do this?",
        )
        val option =
            choice4(
                "I'd like a haircut please.",
                1,
                "I'd like a shave please.",
                2,
                "Actually, I'd like to ask you about something else.",
                3,
                "No thank you.",
                4,
            )
        when (option) {
            1 -> {
                chatPlayer(happy, "I'd like a haircut please.")
                chatNpc(
                    happy,
                    "Awesome! There are so many hair styles to pick from. What would you like?",
                )
                hairdresserMakeover(MakeoverType.Hair)
            }
            2 -> {
                chatPlayer(happy, "I'd like a shave please.")
                chatNpc(
                    happy,
                    "Sweet! There are so many facial hair styles to pick from. What would you " +
                        "like?",
                )
                hairdresserMakeover(MakeoverType.FacialHair)
            }
            3 -> {
                chatPlayer(neutral, "Actually, I'd like to ask you about something else.")
                emmaTopics(asked = null)
            }
            4 -> {
                chatPlayer(neutral, "No thank you.")
                chatNpc(neutral, "Suit yourself. Come back if you change your mind!")
            }
        }
    }

    private suspend fun Dialogue.emmaTopics(asked: EmmaTopic?) {
        val topics = EmmaTopic.entries.filter { it != asked }
        val topic =
            if (topics.size == 3) {
                choice4(
                    topics[0].option,
                    topics[0],
                    topics[1].option,
                    topics[1],
                    topics[2].option,
                    topics[2],
                    "Actually, nevermind.",
                    null,
                )
            } else {
                choice3(
                    topics[0].option,
                    topics[0],
                    topics[1].option,
                    topics[1],
                    "Actually, nevermind.",
                    null,
                )
            }
        when (topic) {
            null -> {
                chatPlayer(neutral, "Actually, nevermind.")
                chatNpc(neutral, "Suit yourself. Come back if you'd like a trim!")
                return
            }
            EmmaTopic.Business -> emmaBusiness()
            EmmaTopic.WhoAreYou -> emmaWhoAreYou()
            EmmaTopic.Suzie -> emmaSuzie()
        }
        emmaTopics(asked = topic)
    }

    private suspend fun Dialogue.emmaBusiness() {
        chatPlayer(quiz, EmmaTopic.Business.option)
        chatNpc(
            happy,
            "9 times out of 10. Though I wouldn't call it aggressive... I just like cutting " +
                "straight to the point. ",
        )
        chatNpc(quiz, "Speaking of straight to the point. What can I help you with?")
    }

    private suspend fun Dialogue.emmaWhoAreYou() {
        chatPlayer(quiz, EmmaTopic.WhoAreYou.option)
        chatNpc(
            happy,
            "My name is Emma. I LOVE hair! There are so many cool things you can do with it.",
        )
        chatPlayer(quiz, "I suppose there are. Have you always been a hairdresser?")
        chatNpc(
            happy,
            "No, I used to be a sheep shearer. However, the farmer wasn't too happy when I dyed " +
                "his favourite sheep pink and gave it a side-cut.",
        )
        chatNpc(quiz, "Anyway, enough chit-chat. What can I help you with?")
    }

    private suspend fun Dialogue.emmaSuzie() {
        chatPlayer(quiz, EmmaTopic.Suzie.option)
        chatNpc(
            happy,
            "That's Suzie, my sister. She's a bit shy around strangers, but a lovely person! I " +
                "just wish more people would see that.",
        )
        chatPlayer(quiz, "Doesn't she talk to anyone?")
        chatNpc(neutral, "Only to me. She wants to, it's just... difficult.")
        chatNpc(
            neutral,
            "She likes being around the shop. My hope is that when more customers come in, " +
                "she'll gradually get used to strangers and open up a bit more.",
        )
        chatPlayer(neutral, "It's sweet of you to help her like that.")
        chatNpc(quiz, "Thank you. Now... What can I do for you?")
    }

    private suspend fun Dialogue.suzie() {
        chatPlayer(neutral, "Hello!")
        chatNpc(silent, "...")
        chatPlayer(neutral, "Is everything okay?")
        chatNpc(neutral, "*mumble mumble*")
        chatPlayer(neutral, "I didn't catch that.")
        chatNpc(silent, "...")
        chatPlayer(neutral, "Right... Well, have a nice day.")
        chatNpc(neutral, "*mumble*")
    }

    private companion object {
        const val BUSY = "Please excuse me, I'm rather busy."
    }
}
