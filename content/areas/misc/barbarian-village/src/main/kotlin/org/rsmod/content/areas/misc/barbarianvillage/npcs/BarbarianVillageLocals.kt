package org.rsmod.content.areas.misc.barbarianvillage.npcs

import dev.openrune.types.MesAnimType
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpNpc1
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The villagers who only have a line or two: Checkal and Atlas (their Barbarian Village lines
 * before Below Ice Mountain moves them on) and Tassie Slipcast at the pottery wheel.
 */
class BarbarianVillageLocals : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.bim_checkal_barb") { checkal(it.npc) }
        onOpNpc1("npc.bim_checkal") { checkal(it.npc) }
        onOpNpc1("npc.bim_atlas") { atlas(it.npc) }
        onOpNpc1("npc.favour_tassie_slipcast") { tassie(it.npc) }
    }

    private suspend fun ProtectedAccess.checkal(npc: Npc) {
        val title = if (isBodyTypeA()) "sir" else "miss"
        startDialogue(npc) {
            mesbox("Checkal stares at you intensely.")
            chatNpc(neutral, "You best not be here to start any trouble, $title.")
            chatPlayer(neutral, "Wouldn't dream of it.")
        }
    }

    private suspend fun ProtectedAccess.atlas(npc: Npc) {
        val size = if (isBodyTypeA()) "man" else "woman"
        startDialogue(npc) {
            chatNpc(angry, "What do you want, little $size?")
            chatPlayer(scared, "Nothing. Just thought I'd try to strike up a conversation.")
            chatNpc(angry, "Not now little one, I'm busy.")
            chatPlayer(scared, "Okay then.")
        }
    }

    private suspend fun ProtectedAccess.tassie(npc: Npc) {
        startDialogue(npc) {
            chatNpc(
                neutral,
                "Please feel free to use the pottery wheel, I won't be using it all the time. " +
                    "Put your pots in the kiln when you've made one.",
            )
            chatNpc(angry, "And make sure you tidy up after yourself!")
        }
    }

    private val Dialogue.scared: MesAnimType
        get() = mesanim("mesanim.scared")
}
