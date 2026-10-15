package org.rsmod.content.generic.locs.doors

import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.forcedWalk
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.map.CoordGrid

/**
 * Walks the player through a door that OSRS keeps shut: the closed door becomes an inviswall
 * while the open leaf shows for two ticks, then both revert on their own.
 */
suspend fun ProtectedAccess.walkThroughInviswallDoor(
    locRepo: LocRepository,
    door: BoundLocInfo,
    openLoc: String,
    sound: String,
    openAngle: LocAngle = door.turnAngle(rotations = 1),
) {
    arriveDelay()
    val across = door.crossingTile(player.coords)
    locRepo.del(door, INVISWALL_DOOR_TICKS)
    locRepo.add(door.coords, "loc.inviswall", INVISWALL_DOOR_TICKS, door.angle, door.shape)
    locRepo.add(
        DoorTranslations.translateOpen(door.coords, door.shape, door.angle),
        openLoc,
        INVISWALL_DOOR_TICKS,
        openAngle,
        door.shape,
    )
    soundSynth(sound)
    forcedWalk(across, crossTiles = 1)
}

private fun BoundLocInfo.crossingTile(from: CoordGrid): CoordGrid {
    if (from != coords) {
        return coords
    }
    return when (angle) {
        LocAngle.West -> coords.translateX(-1)
        LocAngle.North -> coords.translateZ(1)
        LocAngle.East -> coords.translateX(1)
        LocAngle.South -> coords.translateZ(-1)
    }
}

// LocRepository reverts one tick early, so a two-tick open window needs three.
private const val INVISWALL_DOOR_TICKS = 3
