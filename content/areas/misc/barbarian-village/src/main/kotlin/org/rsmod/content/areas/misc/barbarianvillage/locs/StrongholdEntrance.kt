package org.rsmod.content.areas.misc.barbarianvillage.locs

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.script.onOpLoc1
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The hole beside Litara that drops into the first floor of the Stronghold of Security, and the
 * ladder at the bottom that leads back out. The capture shows no climbing animation on the way in,
 * just the teleport with the message box on the same tick.
 */
class StrongholdEntrance : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1("loc.sos_dung_ent_open") { enter() }
        onOpLoc1("loc.sos_war_ladd_up") { leave() }
    }

    private suspend fun ProtectedAccess.enter() {
        arriveDelay()
        telejump(WAR_FLOOR)
        mesbox(
            "You squeeze through the hole and find a ladder a few feet down leading into the " +
                "Stronghold of Security."
        )
    }

    private suspend fun ProtectedAccess.leave() {
        anim("seq.human_reachforladder")
        delay(1)
        mes("You climb up the ladder to the surface.")
        telejump(SURFACE)
    }

    private companion object {
        private val WAR_FLOOR = CoordGrid(1859, 5243, 0)
        private val SURFACE = CoordGrid(3081, 3421, 0)
    }
}
