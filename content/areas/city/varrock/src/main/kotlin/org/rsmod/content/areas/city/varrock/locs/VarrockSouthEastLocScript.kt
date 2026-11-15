package org.rsmod.content.areas.city.varrock.locs

import jakarta.inject.Inject
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.forcedWalk
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.generic.locs.doors.DoorTranslations
import org.rsmod.content.generic.locs.doors.walkThroughInviswallDoor
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class VarrockSouthEastLocScript @Inject constructor(private val locRepo: LocRepository) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1("loc.guidorgatelclosed") { walkThroughGuidorGate() }
        onOpLoc1("loc.guidorgaterclosed") { walkThroughGuidorGate() }
        onOpLoc1("loc.guidordoor") { guidorBedroomDoor() }

        onOpLoc1("loc.phoenixdoor") {
            walkThroughInviswallDoor(
                locRepo,
                it.vis,
                "loc.fai_varrock_poor_door_inactive",
                "synth.door_open",
            )
        }
        onOpLoc1("loc.phoenixdoor2") { lockedDoor() }
        onOpLoc1("loc.blackarmdoor") { lockedDoor() }

        onOpLoc1("loc.fai_varrock_poor_door_flipped") { openFlippedDoor(it.loc) }
        onOpLoc1("loc.fai_varrock_poor_door_open_flipped") { closeFlippedDoor(it.loc) }

        onOpLoc1("loc.fai_varrock_shelf_books") { searchBooks() }
    }

    private suspend fun ProtectedAccess.walkThroughGuidorGate() {
        arriveDelay()
        for (gate in listOf(GATE_LEFT, GATE_RIGHT)) {
            val closed = locRepo.findExact(gate, LocShape.WallStraight) ?: continue
            locRepo.del(closed, GATE_OPEN_TICKS)
            locRepo.add(gate, "loc.inviswall", GATE_OPEN_TICKS, LocAngle.West, LocShape.WallStraight)
        }
        locRepo.add(
            GATE_LEFT_OPEN,
            "loc.guidorgatelopen",
            GATE_OPEN_TICKS,
            LocAngle.South,
            LocShape.WallStraight,
        )
        locRepo.add(
            GATE_RIGHT_OPEN,
            "loc.guidorgateropen",
            GATE_OPEN_TICKS,
            LocAngle.South,
            LocShape.WallStraight,
        )
        soundSynth("synth.picketgate_open")
        val z = player.coords.z.coerceIn(GATE_LEFT.z, GATE_RIGHT.z)
        val x = if (player.coords.x < GATE_LEFT.x) GATE_LEFT.x else GATE_LEFT.x - 1
        forcedWalk(CoordGrid(x, z, GATE_LEFT.level), crossTiles = 1)
    }

    private suspend fun ProtectedAccess.guidorBedroomDoor() {
        arriveDelay()
        startDialogue {
            chatNpcSpecific(
                "Julie",
                JULIE,
                sad,
                "Please leave my husband alone. He's very sick, and I don't want anyone " +
                    "bothering him.",
            )
            chatPlayer(quiz, "I'm sorry to hear that. Is there anything I can do?")
            chatNpcSpecific("Julie", JULIE, sad, "Thank you, but I just want him to see a priest.")
            chatPlayer(quiz, "A priest? Hmmm...")
        }
    }

    private suspend fun ProtectedAccess.lockedDoor() {
        arriveDelay()
        soundSynth("synth.locked")
        mesbox("The door is securely locked.")
    }

    private suspend fun ProtectedAccess.openFlippedDoor(closed: BoundLocInfo) {
        arriveDelay()
        val opened = DoorTranslations.translateOpen(closed.coords, closed.shape, closed.angle)
        locRepo.del(closed, FLIPPED_DOOR_TICKS)
        locRepo.add(
            opened,
            "loc.fai_varrock_poor_door_open_flipped",
            FLIPPED_DOOR_TICKS,
            closed.turnAngle(rotations = -1),
            closed.shape,
        )
        soundSynth("synth.door_open")
    }

    private suspend fun ProtectedAccess.closeFlippedDoor(open: BoundLocInfo) {
        arriveDelay()
        val closedAngle = open.turnAngle(rotations = 1)
        val closedCoords = DoorTranslations.translateOpenOpposite(open.coords, open.shape, closedAngle)
        val original = locRepo.findPendingRespawn(closedCoords, open.layer)
        locRepo.del(open, FLIPPED_DOOR_TICKS)
        if (original != null) {
            locRepo.add(original, FLIPPED_DOOR_TICKS)
        } else {
            locRepo.add(
                closedCoords,
                "loc.fai_varrock_poor_door_flipped",
                FLIPPED_DOOR_TICKS,
                closedAngle,
                open.shape,
            )
        }
        soundSynth("synth.door_close")
    }

    private suspend fun ProtectedAccess.searchBooks() {
        arriveDelay()
        spam("You search the books...")
        mes("You don't find anything that you'd ever want to read.")
    }

    private companion object {
        const val JULIE = "npc.guidors_wife"

        val GATE_LEFT = CoordGrid(3264, 3405, 0)
        val GATE_RIGHT = CoordGrid(3264, 3406, 0)
        val GATE_LEFT_OPEN = CoordGrid(3263, 3405, 0)
        val GATE_RIGHT_OPEN = CoordGrid(3262, 3405, 0)
        const val GATE_OPEN_TICKS = 3

        const val FLIPPED_DOOR_TICKS = 500
    }
}
