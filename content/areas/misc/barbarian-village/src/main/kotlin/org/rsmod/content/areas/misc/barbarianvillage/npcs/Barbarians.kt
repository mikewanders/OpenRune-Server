package org.rsmod.content.areas.misc.barbarianvillage.npcs

import jakarta.inject.Inject
import org.rsmod.api.npc.interact.AiPlayerInteractions
import org.rsmod.api.npc.opPlayer2
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The village barbarians and their chieftain Gunthor. Talking to any of them gets a single line
 * and then a fight: in the capture Gunthor swung at the player three ticks after his line closed.
 */
class Barbarians
@Inject
constructor(private val aiPlayerInteractions: AiPlayerInteractions) : PluginScript() {
    override fun ScriptContext.startup() {
        for (barbarian in BARBARIANS) {
            onOpNpc1(barbarian) { challenge(it.npc) }
        }
        onOpNpc1("npc.gunthor_the_brave") { gunthor(it.npc) }
    }

    private suspend fun ProtectedAccess.challenge(npc: Npc) {
        val line = random.of(maxExclusive = LINES)
        startDialogue(npc) {
            when (line) {
                0 -> chatNpc(happy, "Wanna fight?")
                1 -> chatNpc(happy, "Ah, you come for fight, ja?")
                2 -> chatNpc(laugh, "You look funny.")
                3 -> chatNpc(angry, "Grrr!")
                4 -> chatNpc(angry, "What you want?")
                else -> chatNpc(angry, "Go Away!")
            }
        }
        npc.opPlayer2(player, aiPlayerInteractions)
    }

    private suspend fun ProtectedAccess.gunthor(npc: Npc) {
        startDialogue(npc) { chatNpc(happy, "Ah, you've come for fight!") }
        npc.opPlayer2(player, aiPlayerInteractions)
    }

    private companion object {
        private const val LINES = 6

        private val BARBARIANS =
            (listOf(1, 2, 3, 4, 5, 6, 7, 8, 10, 11, 12, 13, 14).map { "npc.fai_barbarian_$it" } +
                (1..4).map { "npc.fai_barbarian_female_$it" })
    }
}
