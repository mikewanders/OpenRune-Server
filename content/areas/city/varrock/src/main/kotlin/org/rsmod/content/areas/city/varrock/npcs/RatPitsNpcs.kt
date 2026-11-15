package org.rsmod.content.areas.city.varrock.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.script.onOpNpc1
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class RatPitsNpcs : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.vc_hooknosed_jack") { startDialogue(it.npc) { hooknosedJack() } }
        onOpNpc1("npc.vc_gambler_rich1") { startDialogue(it.npc) { gamer() } }
    }

    private suspend fun Dialogue.hooknosedJack() {
        val option =
            choice3(
                "Can you tell me where I would find ....",
                1,
                "Can you tell me more about the pits?",
                2,
                "Can you show me to the rat pits?",
                3,
            )
        when (option) {
            1 -> whereToFind()
            2 -> morePits()
            else -> {
                chatPlayer(quiz, "Can you show me to the rat pits?")
                chatNpc(
                    neutral,
                    "Didn't you hear me? They're through that manhole over there. You can't miss " +
                        "it!",
                )
            }
        }
    }

    private suspend fun Dialogue.whereToFind() {
        val option =
            choice5(
                "Grimesquit and Phingspet",
                1,
                "Jimmy Dazzler",
                2,
                "Smokin' Joe",
                3,
                "The Face",
                4,
                "Felkrash",
                5,
            )
        when (option) {
            1 -> {
                chatPlayer(neutral, "Can you tell me where I could find Phingspet and Grimesquit?")
                chatNpc(
                    neutral,
                    "The sisters Grime? You'll find them down in the sewers here in Varrock.",
                )
            }
            2 -> jimmyDazzler()
            3 -> {
                chatPlayer(neutral, "Can you tell me where I could find Smokin' Joe?")
                chatNpc(neutral, "He should be in Keldagrim, the city of the dwarves.")
                chatNpc(sad, "Tell him I said hello, will you? I've been meaning to catch up.")
            }
            4 -> {
                chatPlayer(neutral, "Can you tell me where I could find 'The Face'?")
                chatNpc(
                    neutral,
                    "She'll be in Port Sarim, scaring people away from the entrance to the pits.",
                )
            }
            else -> {
                chatPlayer(neutral, "Can you tell me where I could find Felkrash?")
                chatNpc(
                    neutral,
                    "Her nibs? She'll be in the rat pits at Port Sarim. Good luck getting in " +
                        "there, though.",
                )
            }
        }
    }

    private suspend fun Dialogue.jimmyDazzler() {
        chatPlayer(quiz, "Can you tell me where I could find Jimmy Dazzler?")
        chatNpc(angry, "Pah! There's nothing else he can teach you, I'll tell you that.")
        chatPlayer(quiz, "But where would I find him?")
        chatNpc(angry, "Probably cosying up to the nobles over in Ardougne.")
        chatPlayer(neutral, "What's your problem with Jimmy, anyway?")
        chatNpc(angry, "He reckons he's too good for the likes of us.")
        chatPlayer(neutral, "Are you sure? He spoke very highly of you.")
        chatNpc(angry, "I'll believe it when I see it.")
        chatNpc(neutral, "Anyway. The Flying Horse, Ardougne. You'll find him there.")
    }

    private suspend fun Dialogue.morePits() {
        chatPlayer(quiz, "Can you tell me more about the pits?")
        if (!access.inv.contains(MANUAL) && !access.bank.contains(MANUAL)) {
            chatNpc(neutral, "Here take this, you might find it useful.")
            if (access.invAdd(access.inv, MANUAL).success) {
                objbox(MANUAL, "You are handed the rat pits manual.")
            }
        }
        chatNpc(neutral, "What do you want to know?")
        do {
            val pointers =
                choice2(
                    "Do you have any pointers for me and my cat?",
                    true,
                    "What benefits does my cat get from training in the pits?",
                    false,
                )
            if (pointers) catPointers() else catBenefits()
            chatPlayer(neutral, "Thanks for your help.")
            chatNpc(neutral, "Do you have any other questions about the pits?")
        } while (choice2("I'd like to ask you...", true, "No thanks", false))
        chatPlayer(neutral, "No thanks.")
    }

    private suspend fun Dialogue.catPointers() {
        chatPlayer(quiz, "Do you have any pointers for me and my cat?")
        chatNpc(
            neutral,
            "You should wear an Amulet of Catspeak. It will allow you to organise tactics with " +
                "your cat.",
        )
        chatPlayer(quiz, "What would be the best tactic to adopt then?")
        chatNpc(
            neutral,
            "Well, that depends on your outlook, whether you're ready to risk your cat's life or " +
                "not and how defensive or aggressive you want your cat to be.",
        )
        val risk =
            choice2(
                "I will not risk my cat's life.",
                false,
                "I am willing to risk my cat if it improves my chances.",
                true,
            )
        if (!risk) {
            chatPlayer(neutral, "I will not risk my cat's life.")
            return
        }
        chatPlayer(neutral, "I am willing to risk my cat, if it increases my chances of victory.")
        chatNpc(neutral, "In that case you are left with two choices.")
        chatNpc(
            neutral,
            "You could go for all out attack which will boost your cat's attack and strength, " +
                "but weaken it.",
        )
        chatNpc(
            neutral,
            "Or, go for a less aggressive stance, which will increase your cat's strength and " +
                "attack to a lesser degree, but carries no defensive penalties.",
        )
        val aggressive =
            choice2(
                "I think I would adopt a very aggressive strategy.",
                true,
                "I think I would adopt a less aggressive strategy.",
                false,
            )
        if (aggressive) {
            chatPlayer(neutral, "I think I would adopt a very aggressive strategy.")
            chatNpc(neutral, "In that case you should tell your cat: 'Go berserk!'")
        } else {
            chatPlayer(neutral, "I think I would adopt a less aggressive strategy.")
        }
    }

    private suspend fun Dialogue.catBenefits() {
        chatPlayer(quiz, "What benefits does my cat get from training in the pits?")
        chatNpc(neutral, "It depends on what kind of cat you have.")
        chatNpc(
            neutral,
            "In the Varrock pits, your cat will grow up faster by gaining more experience.",
        )
    }

    private suspend fun Dialogue.gamer() {
        chatPlayer(neutral, "Hello.")
        chatNpc(neutral, "What do you want?")
        chatPlayer(quiz, "I was just wondering what you were doing here?")
        chatNpc(neutral, "I often ask myself the same question.")
        chatNpc(
            neutral,
            "What am I doing down here in these pits with the filthy dregs of society?",
        )
        chatPlayer(quiz, "And do you ever come up with an answer?")
        chatNpc(neutral, "Always.")
        chatNpc(madlaugh, "I'm here taking their money!")
        chatPlayer(quiz, "So how do these rat pits work?")
        chatNpc(neutral, "It's simple.")
        chatNpc(
            neutral,
            "In Varrock if you have a grown cat and a couple of coins you can challenge someone " +
                "else who also has a grown cat and some spare change.",
        )
        chatPlayer(neutral, "That sounds simple enough so far.")
        chatNpc(
            neutral,
            "You then agree on a wager. It's rumoured that some ratcatchers can talk to their " +
                "own cats and give them tactics to aid them.",
        )
        chatPlayer(neutral, "So then how do you win the challenge?")
        chatNpc(
            neutral,
            "I was just getting to that. Both the challengers' cats are placed inside the arena, " +
                "and whichever kills 7 rats first wins.",
        )
        val reply =
            choice5(
                "That sounds vile and cruel.",
                "That sounds vile and cruel.",
                "It sounds a little dangerous for my cat.",
                "It sounds a little dangerous for my cat.",
                "Wow, that sounds like fun.",
                "Wow, that sounds like fun.",
                "Is there a limit to how much you can bet?",
                "Is there a limit to how much you can bet?",
                "Can I challenge you and your cat?",
                "Can I challenge you and your cat?",
            )
        chatPlayer(neutral, reply)
    }

    private companion object {
        const val MANUAL = "obj.ratpits_manual"
    }
}
