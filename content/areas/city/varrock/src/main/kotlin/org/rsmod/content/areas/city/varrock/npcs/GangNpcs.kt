package org.rsmod.content.areas.city.varrock.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Shield of Arrav gang members, as they talk to a player who joined the Phoenix Gang. */
class GangNpcs : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.straven") { startDialogue(it.npc) { straven() } }
        onOpNpc1("npc.katrine") { startDialogue(it.npc) { katrine() } }
        onOpNpc1("npc.tramppg") { startDialogue(it.npc) { charlie() } }
    }

    private suspend fun Dialogue.straven() {
        chatNpc(neutral, "Greetings, fellow gang member.")
        val carried = access.inv.contains(PHOENIX_KEY)
        if (carried || access.bank.contains(PHOENIX_KEY)) {
            return
        }
        chatPlayer(sad, "I'm afraid I lost that key you gave me...")
        chatNpc(
            neutral,
            "You really need to be more careful. We don't want that key falling into the wrong " +
                "hands. Have this spare, but don't lose it this time!",
        )
        if (access.invAdd(access.inv, PHOENIX_KEY).failure) {
            return
        }
        objbox(PHOENIX_KEY, "Straven hands you a key.")
    }

    private suspend fun Dialogue.katrine() {
        chatNpc(
            angry,
            "You've got some guts coming here, Phoenix scum! Get lost, or I'll make sure you " +
                "haven't got those guts anymore!",
        )
    }

    private suspend fun Dialogue.charlie() {
        chatNpc(sad, "Spare some change, guv?")
        var askedName = false
        while (true) {
            val options = buildList {
                if (!askedName) add("Who are you?")
                addAll(CHARLIE_REPLIES)
            }
            when (options[menu(options) - 1]) {
                "Who are you?" -> {
                    askedName = true
                    chatPlayer(quiz, "Who are you?")
                    chatNpc(
                        neutral,
                        "Charles. Charles E. Trampin' at your service. Now, about that change " +
                            "you were going to give me...",
                    )
                }
                "I'm looking for the Black Arm Gang." -> return blackArmGang()
                "Okay, here you go." -> {
                    chatPlayer(happy, "Okay, here you go.")
                    chatPlayer(sad, "Oh wait... I haven't got enough, sorry.")
                    return chatNpc(sad, "Thanks anyway.")
                }
                "Sorry, I haven't got any." -> {
                    chatPlayer(sad, "Sorry, I haven't got any.")
                    return chatNpc(sad, "Thanks anyway.")
                }
                else -> {
                    chatPlayer(angry, "Go get a job!")
                    return chatNpc(angry, "You startin? I hope your nose falls off!")
                }
            }
        }
    }

    private suspend fun Dialogue.menu(options: List<String>): Int =
        when (options.size) {
            4 -> choice4(options[0], 1, options[1], 2, options[2], 3, options[3], 4)
            else -> choice5(options[0], 1, options[1], 2, options[2], 3, options[3], 4, options[4], 5)
        }

    private suspend fun Dialogue.blackArmGang() {
        chatPlayer(quiz, "I'm looking for the Black Arm Gang.")
        chatNpc(
            shifty,
            "Funny you should mention that... Rumour has it that they have their headquarters " +
                "right down this alleyway. Though if I were you, I'd be careful about messing " +
                "with them...",
        )
        chatPlayer(quiz, "Do you think they would let me join?")
        chatNpc(
            neutral,
            "No. You're a collaborator with the Phoenix Gang. There's no way they'll let you join.",
        )
        val askHow =
            choice2(
                "How did you know I was in the Phoenix Gang?",
                true,
                "Any ideas how I could get in there then?",
                false,
            )
        if (askHow) {
            chatPlayer(quiz, "How did you know I was in the Phoenix Gang?")
            chatNpc(
                neutral,
                "In my current profession, I spend a lot of time on the streets. You hear these " +
                    "things sometimes.",
            )
            chatPlayer(quiz, "I see. So, any ideas how I could get in there?")
        } else {
            chatPlayer(quiz, "Any ideas how I could get in there then?")
        }
        chatNpc(
            neutral,
            "Your best bet would probably be to find someone who isn't a member of the Phoenix " +
                "Gang and get them to infiltrate the ranks of the Black Arm Gang for you.",
        )
        chatPlayer(quiz, "Someone? Like who?")
        chatNpc(
            neutral,
            "There's plenty of other adventurers about besides yourself. I'm sure if you asked " +
                "one of them nicely they would help you.",
        )
        chatPlayer(neutral, "I see...")
    }

    private companion object {
        const val PHOENIX_KEY = "obj.phoenixkey2"

        val CHARLIE_REPLIES =
            listOf(
                "I'm looking for the Black Arm Gang.",
                "Okay, here you go.",
                "Sorry, I haven't got any.",
                "Go get a job!",
            )
    }
}
