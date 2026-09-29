package org.rsmod.content.areas.misc.barbarianvillage.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/** Peksa and his helmet shop, beside the longhall. */
class Peksa @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.peksa") { talk(it.npc) }
        onOpNpc3("npc.peksa") { player.openHelmetShop(it.npc) }
    }

    private suspend fun ProtectedAccess.talk(npc: Npc) {
        startDialogue(npc) {
            chatNpc(neutral, "Are you interested in buying or selling a helmet?")
            if (choice2("I could be, yes.", true, "No, I'll pass on that.", false)) {
                player.openHelmetShop(npc)
                return@startDialogue
            }
            chatPlayer(neutral, "No, I'll pass on that.")
            chatNpc(neutral, "Well, come back if you change your mind.")
        }
    }

    private fun Player.openHelmetShop(npc: Npc) {
        shops.open(this, npc, "Helmet Shop.", "inv.helmetshop")
    }
}
