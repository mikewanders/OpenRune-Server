package org.rsmod.content.areas.misc.barbarianvillage.locs

import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocInfo
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * The longhall's double doors. Unlike castle double doors they swing on their own tiles: the live
 * capture has the right leaf turning one way and the left leaf the other, with neither moving.
 * Opening either leaf swings both.
 */
class LonghallDoors @Inject constructor(private val locRepo: LocRepository) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1(CLOSED_RIGHT) { swing(it.loc, open = true) }
        onOpLoc1(CLOSED_LEFT) { swing(it.loc, open = true) }
        onOpLoc1(OPEN_RIGHT) { swing(it.loc, open = false) }
        onOpLoc1(OPEN_LEFT) { swing(it.loc, open = false) }
    }

    private fun ProtectedAccess.swing(clicked: BoundLocInfo, open: Boolean) {
        soundSynth(if (open) OPEN_SYNTH else CLOSE_SYNTH)
        val clickedLoc = LocInfo(clicked.layer, clicked.coords, clicked.entity)
        val partner = findPartner(clickedLoc)
        swingLeaf(clickedLoc)
        partner?.let { swingLeaf(it) }
    }

    private fun findPartner(door: LocInfo): LocInfo? {
        val partnerId = PARTNERS[door.id] ?: return null
        val neighbours =
            listOf(
                door.coords.translateX(1),
                door.coords.translateX(-1),
                door.coords.translateZ(1),
                door.coords.translateZ(-1),
            )
        return neighbours.firstNotNullOfOrNull { tile ->
            locRepo.findAll(tile).firstOrNull { it.id == partnerId }
        }
    }

    private fun swingLeaf(leaf: LocInfo) {
        val stage = STAGES.getValue(leaf.id)
        locRepo.del(leaf, DURATION)
        locRepo.add(leaf.coords, stage.next, DURATION, leaf.turnAngle(stage.rotations), leaf.shape)
    }

    private data class Stage(val next: String, val rotations: Int)

    private companion object {
        private const val CLOSED_RIGHT = "loc.fai_barbarian_large_doubledoor_r"
        private const val CLOSED_LEFT = "loc.fai_barbarian_large_doubledoor_l"
        private const val OPEN_RIGHT = "loc.fai_barbarian_open_large_doubledoor_r"
        private const val OPEN_LEFT = "loc.fai_barbarian_open_large_doubledoor_l"

        private const val OPEN_SYNTH = "synth.door_open"
        private const val CLOSE_SYNTH = "synth.door_close"
        private const val DURATION = 500

        private val STAGES by lazy {
            mapOf(
                CLOSED_RIGHT.loc() to Stage(OPEN_RIGHT, rotations = 1),
                CLOSED_LEFT.loc() to Stage(OPEN_LEFT, rotations = 3),
                OPEN_RIGHT.loc() to Stage(CLOSED_RIGHT, rotations = 3),
                OPEN_LEFT.loc() to Stage(CLOSED_LEFT, rotations = 1),
            )
        }

        private val PARTNERS by lazy {
            mapOf(
                CLOSED_RIGHT.loc() to CLOSED_LEFT.loc(),
                CLOSED_LEFT.loc() to CLOSED_RIGHT.loc(),
                OPEN_RIGHT.loc() to OPEN_LEFT.loc(),
                OPEN_LEFT.loc() to OPEN_RIGHT.loc(),
            )
        }

        private fun String.loc(): Int = asRSCM(RSCMType.LOC)
    }
}
