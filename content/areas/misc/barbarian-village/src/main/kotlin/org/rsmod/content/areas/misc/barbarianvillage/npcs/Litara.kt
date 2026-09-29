package org.rsmod.content.areas.misc.barbarianvillage.npcs

import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onOpNpc1
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private var Player.brotherFound by intVarBit("varbit.sos_brother_found")

/**
 * Litara, by the Stronghold of Security entrance, still waiting on news of her brother Solztun.
 * Her reply follows `sos_brother_found`: 0 before finding him, 1 once his spirit has been met at the
 * bottom of the Stronghold, 2 after she has been told.
 */
class Litara : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.sos_barb") { talk(it.npc) }
        onOpNpc1("npc.sos_barb_visible") { talk(it.npc) }
    }

    private suspend fun ProtectedAccess.talk(npc: Npc) {
        startDialogue(npc) {
            chatNpc(happy, "Hello there. You look lost, are you okay?")
            when (player.brotherFound) {
                0 -> lookingForStronghold()
                1 -> breakTheNews()
                else -> alreadyTold()
            }
        }
    }

    private suspend fun Dialogue.lookingForStronghold() {
        val looking =
            choice2(
                "I'm looking for a stronghold or something...",
                true,
                "I'm fine, just passing through.",
                false,
            )
        if (!looking) {
            chatPlayer(neutral, "I'm fine, just passing through.")
            return
        }
        chatPlayer(quiz, "I'm looking for a stronghold or something...")
        chatNpc(neutral, "Ahh... the Stronghold of Security. It's down there.")
        mesbox("Litara point's on the hole in the ground that looks like you could squeeze through.")
        chatPlayer(worried, "Looks kind of... deep and dark.")
        chatNpc(sad, "Yeah... tell that to my brother, he still hasn't come back.")
        chatPlayer(quiz, "Your brother?")
        chatNpc(
            sad,
            "He's an explorer too. When the miner fell down that hole he'd made and came back " +
                "babbling about doors, questions and treasure, my brother went to explore. " +
                "No-one has seen him since.",
        )
        chatPlayer(sad, "Oh... that's not good.")
        chatNpc(
            sad,
            "Lots of people have been down there, but none of them have seen him, Let me know if " +
                "you do, will you?",
        )
        chatPlayer(neutral, "I'll certainly keep my eyes open.")
    }

    private suspend fun Dialogue.breakTheNews() {
        chatPlayer(neutral, "I travelled to the bottom of the stronghold, and I emerge victorious!")
        chatNpc(happy, "Well done! You didn't happen to find my brother?")
        chatPlayer(sad, "About that...")
        chatNpc(sad, "I sense bad news. It's okay, he's been gone so long. I believe I've moved on.")
        chatPlayer(sad, "Well, at least he died doing what he loved.")
        chatNpc(
            sad,
            "True, I will raise a drink for him tonight, he was a good man, even if he was a " +
                "pacifist.",
        )
        chatPlayer(neutral, "As will I. See you around, Litara.")
        chatNpc(neutral, "Goodbye, ${player.displayName}.")
        player.brotherFound = 2
    }

    private suspend fun Dialogue.alreadyTold() {
        chatPlayer(neutral, "I travelled to the bottom of the stronghold, and I emerge victorious!")
        chatNpc(
            sad,
            "I am glad the challenges of the stronghold have been overcome; it feels like my " +
                "brother has somehow been avenged.",
        )
    }
}
