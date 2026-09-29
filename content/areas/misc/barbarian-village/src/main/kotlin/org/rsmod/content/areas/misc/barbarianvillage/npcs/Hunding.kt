package org.rsmod.content.areas.misc.barbarianvillage.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Hunding, keeping watch from the top of the tower on the east side of the village. */
class Hunding : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.fai_barbarian_9") { talk(it.npc) }
    }

    private suspend fun ProtectedAccess.talk(npc: Npc) {
        startDialogue(npc) {
            chatPlayer(quiz, "Hello.")
            chatNpc(angry, "What are you doing in our village, outlander?")
            when (
                choice3(
                    "Nothing much.",
                    1,
                    "I'm exploring.",
                    2,
                    "I came to kill you all!",
                    3,
                    title = TITLE,
                )
            ) {
                1 -> {
                    chatPlayer(bored, "Nothing much.")
                    goToLonghall()
                }
                2 -> exploring()
                else -> {
                    chatPlayer(happy, "I came to kill you all!")
                    chatNpc(
                        laugh,
                        "Ho ho! Brave words indeed from an outerlander! Go down to the longhall " +
                            "and try it!",
                    )
                }
            }
        }
    }

    private suspend fun Dialogue.exploring() {
        chatPlayer(quiz, "I'm exploring.")
        chatNpc(
            angry,
            "Bah! You cannot hope to learn anything of us just by strolling through our " +
                "village! We are an ancient tribe, our ways date back to the time before " +
                "Avarrocka was founded!",
        )
        when (
            choice3(
                "Tell me about your tribe.",
                1,
                "You look like a load of primitive savages.",
                2,
                "I'm bored.",
                3,
                title = TITLE,
            )
        ) {
            1 -> tribeHistory()
            2 -> {
                chatPlayer(happy, "You look like a load of primitive savages.")
                chatNpc(
                    angry,
                    "And you look like an arrogant fool. And you smell like a raccoon's bottom.",
                )
            }
            else -> {
                chatPlayer(bored, "I'm bored.")
                goToLonghall()
            }
        }
    }

    private suspend fun Dialogue.goToLonghall() {
        chatNpc(
            angry,
            "Bah! Go down to the longhall, and there you will find excitement aplenty! Our " +
                "finest warrior, Gunthor the Brave, will give you a rousing welcome!",
        )
        chatPlayer(shifty, "I'll bear it in mind.")
    }

    private suspend fun Dialogue.tribeHistory() {
        chatPlayer(quiz, "Would you care to tell me more?")
        chatNpc(
            neutral,
            "Oh? You are not so ignorant as I thought. Very well, I shall speak of our tribe...",
        )
        chatNpc(
            happy,
            "Our elders remember that about a century ago we were living in the lands far to " +
                "the west. We were a large nomadic mountain tribe, settling wherever there was " +
                "food, moving on when it had run out. As the tribe",
        )
        chatNpc(
            happy,
            "grew larger, it was hard to find enough food for everyone, and we were forced to " +
                "shift our camp more and more often. In time, a warrior called Gunnar took his " +
                "friends and their families and left the larger tribe,",
        )
        chatNpc(
            happy,
            "moving south in search of new places. They eventually settled here and built this " +
                "village, finding that the old nomadic traditions were no longer needed.",
        )
        chatNpc(
            happy,
            "Our current chieftain, Gunthor the Brave, is a direct- line descendent of Gunnar.",
        )
        chatNpc(
            neutral,
            "However, our ways have changed little in the last century. Although more and more " +
                "people use magical powers, we do not believe it is wise to take upon oneself " +
                "the power of the gods in this way. To this day, we fight",
        )
        chatNpc(
            neutral,
            "with the mighty sword, the vicious axe and the swift arrow on the wind. Some of our " +
                "young, wishing to try so-called 'civilisation' and the softness of city life, " +
                "abandon the tribe and move to the cities. It is sad to",
        )
        chatNpc(
            neutral,
            "see them go, but we do not prevent them; we live here in our village because we " +
                "love this life - we do not force it on those whom it does not suit.",
        )
        chatNpc(bored, "There, I have said enough.")
        chatPlayer(happy, "Thank you.")
    }

    private companion object {
        private const val TITLE = "What would you like to say?"
    }
}
