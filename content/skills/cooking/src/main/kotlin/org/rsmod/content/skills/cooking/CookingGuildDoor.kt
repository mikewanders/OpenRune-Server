package org.rsmod.content.skills.cooking

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.cookingLvl
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.generic.locs.doors.walkThroughInviswallDoor
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class CookingGuildDoor @Inject constructor(
    private val locRepo: LocRepository,
) : PluginScript() {

    override fun ScriptContext.startup() {
        onOpLoc1("loc.chefdoor") {
            when {
                !player.hasGuildEntryOutfit() && player.cookingLvl >= 32 ->
                    denyEntry {
                        chatNpcSpecific(
                            title = "Head chef",
                            type = "npc.head_chef",
                            mesanim = neutral,
                            text = "You can't come in here unless you're wearing a chef's hat, or something like that.",
                        )
                    }

                !player.hasGuildEntryOutfit() && player.cookingLvl < 32 ->
                    denyEntry {
                        chatNpcSpecific(
                            title = "Head chef",
                            type = "npc.head_chef",
                            mesanim = neutral,
                            text =
                                "Sorry. Only the finest chefs are allowed in here. Get your cooking level up to 32 " +
                                    "and come back wearing a chef's hat.",
                        )
                    }

                player.hasGuildEntryOutfit() && player.cookingLvl < 32 ->
                    denyEntry {
                        chatNpcSpecific(
                            title = "Head chef",
                            type = "npc.head_chef",
                            mesanim = neutral,
                            text =
                                "Sorry. Only the finest chefs are allowed in here. Get your cooking level up to 32.",
                        )
                    }

                else ->
                    walkThroughInviswallDoor(locRepo, it.vis, "loc.chefdoor_open", "synth.door_open")
            }
        }
    }

    private suspend fun ProtectedAccess.denyEntry(lines: suspend Dialogue.() -> Unit) {
        startDialogue { lines() }
    }
}
