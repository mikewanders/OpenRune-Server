package org.rsmod.content.areas.city.varrock.npcs

import jakarta.inject.Inject
import org.rsmod.api.invtx.invAddOrDrop
import org.rsmod.api.invtx.invTakeFee
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onApNpc1
import org.rsmod.api.script.onOpNpc1
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class DancingDonkeyNpcs @Inject constructor(private val objRepo: ObjRepository) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.gambler2") { startDialogue(it.npc) { chancy() } }
        onOpNpc1("npc.artist2") { startDialogue(it.npc) { daVinci() } }
        onOpNpc1("npc.drunk2") { startDialogue(it.npc) { chatNpc(drunk, HOPS_BUSY) } }
        onOpNpc1(BARTENDER) { bartender(it.npc) }
        onApNpc1(BARTENDER) {
            if (isWithinDistance(it.npc, ACROSS_BAR)) bartender(it.npc) else apRange(ACROSS_BAR)
        }
    }

    private suspend fun Dialogue.chancy() {
        chatPlayer(happy, "Good morning.")
        chatNpc(angry, "Leave me alone. I'm trying to find my gambling buddies!")
    }

    private suspend fun Dialogue.daVinci() {
        chatNpc(
            angry,
            "Bah! A great artist such as myself should not have to suffer the HUMILIATION of " +
                "spending time where the likes of you wander everywhere!",
        )
    }

    private suspend fun ProtectedAccess.bartender(npc: Npc) =
        startDialogue(npc) {
            chatPlayer(happy, "Hello.")
            chatNpc(happy, "Good day to you, brave adventurer. Can I get you a refreshing beer?")
            when (choice3("Yes please!", 1, "No thanks.", 2, "How much?", 3)) {
                1 -> buyBeer()
                2 -> chatPlayer(neutral, "No thanks.")
                else -> chatPlayer(quiz, "How much?")
            }
        }

    private suspend fun Dialogue.buyBeer() {
        chatPlayer(happy, "Yes please!")
        chatNpc(happy, "Ok then, that's two gold coins please.")
        if (!player.invTakeFee(fee = BEER_PRICE)) {
            player.mes("You don't have enough gold!")
            return
        }
        player.invAddOrDrop(objRepo, "obj.beer")
    }

    private companion object {
        const val BARTENDER = "npc.donkey_bartender"
        const val BEER_PRICE = 2
        const val ACROSS_BAR = 2
        const val HOPS_BUSY = "Hops don't wanna talk now."
    }
}
