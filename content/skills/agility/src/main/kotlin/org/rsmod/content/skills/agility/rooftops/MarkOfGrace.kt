package org.rsmod.content.skills.agility.rooftops

import jakarta.inject.Inject
import org.rsmod.api.player.stat.baseAgilityLvl
import org.rsmod.api.player.vars.intVarp
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.content.skills.agility.Agility
import org.rsmod.game.MapClock
import org.rsmod.game.entity.Player

private var Player.markOfGraceClock by intVarp("varp.agility_mark_of_grace_clock")

/**
 * Rolls a Mark of Grace when a lap begins. Two rules keep the drip slow: nothing can spawn until the
 * cooldown since the last mark has elapsed, and being far enough over the course requirement cuts
 * the roll to a fifth of its usual chance.
 */
class MarkOfGrace
@Inject
constructor(
    private val random: GameRandom,
    private val objRepo: ObjRepository,
    private val mapClock: MapClock,
) {
    fun rollLapStart(player: Player, course: RooftopCourse) {
        if (mapClock.cycle < player.markOfGraceClock) {
            return
        }
        val overLevelled = player.baseAgilityLvl >= course.level + OVER_LEVEL_THRESHOLD
        val chance = if (overLevelled) course.markChance * OVER_LEVEL_PENALTY else course.markChance
        if (random.of(chance) != 0) {
            return
        }
        val tile = course.markTiles[random.of(course.markTiles.size)]
        objRepo.add(Agility.MARK_OF_GRACE, tile, MARK_DURATION, receiver = player)
        player.markOfGraceClock = mapClock.cycle + SPAWN_COOLDOWN
    }

    private companion object {
        const val OVER_LEVEL_THRESHOLD = 20
        const val OVER_LEVEL_PENALTY = 5
        const val SPAWN_COOLDOWN = 300
        const val MARK_DURATION = 1000
    }
}
