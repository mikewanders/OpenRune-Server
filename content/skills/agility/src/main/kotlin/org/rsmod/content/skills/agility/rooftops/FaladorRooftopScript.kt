package org.rsmod.content.skills.agility.rooftops

import jakarta.inject.Inject
import org.rsmod.api.config.Constants.em_face_east
import org.rsmod.api.config.Constants.em_face_north
import org.rsmod.api.config.Constants.em_face_south
import org.rsmod.api.config.Constants.em_face_west
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onOpLoc1
import org.rsmod.content.skills.agility.Agility
import org.rsmod.game.entity.Player
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private const val SEQ_REACH_FOR_LADDER = "seq.human_reachforladder"
private const val SEQ_LOG_BALANCE = "seq.human_walk_logbalance_loop"
private const val SEQ_SPOT_JUMP = "seq.human_spot_jump"
private const val SEQ_JUMP_HURDLE = "seq.human_jump_hurdle"
private const val SEQ_WALL_JUMP = "seq.agility_shortcut_wall_jump"
private const val SEQ_WALL_JUMP_DOWN = "seq.agility_shortcut_wall_jumpdown"
private const val SEQ_WALL_JUMP_DOWN_LAND = "seq.agility_shortcut_wall_jumpdown2"
private const val SEQ_HANDHOLDS_MIDDLE = "seq.agilityarena_handholds_middle"
private const val SEQ_HANDHOLDS_LAST = "seq.agilityarena_handholds_last"

private const val SOUND_TIGHTROPE = "synth.tightrope"
private const val SOUND_TIGHTROPE_3 = "synth.deathslide_first"
private const val SOUND_TIGHTROPE_3_LAND = "synth.fall_land"
private const val SOUND_JUMP = "synth.jump"
private const val SOUND_WALL_JUMP = "synth.jump_up"
private const val SOUND_WALL_JUMP_DOWN = "synth.jump2"
private const val SOUND_HANDHOLDS = "synth.handholds_loop"

private var Player.highlightedCourse by intVarBit(Agility.HIGHLIGHTED_COURSE)

private var Player.faladorLaps by intVarBit("varbit.agility_laps_falador")

private enum class RooftopJump(
    val loc: String,
    val dest: CoordGrid,
    val dir: Int,
    val seq: String,
    val xp: Double,
) {
    Gap1("loc.rooftops_falador_gap_1", CoordGrid(3048, 3361, 3), em_face_north, SEQ_SPOT_JUMP, 26.8),
    Gap2("loc.rooftops_falador_gap_2", CoordGrid(3041, 3361, 3), em_face_west, SEQ_SPOT_JUMP, 26.8),
    Gap3("loc.rooftops_falador_gap_3", CoordGrid(3018, 3349, 3), em_face_south, SEQ_JUMP_HURDLE, 30.0),
    Ledge1("loc.rooftops_falador_ledge_1", CoordGrid(3014, 3346, 3), em_face_west, SEQ_JUMP_HURDLE, 13.4),
    Ledge2("loc.rooftops_falador_ledge_2", CoordGrid(3013, 3342, 3), em_face_south, SEQ_JUMP_HURDLE, 13.4),
    Ledge3a("loc.rooftops_falador_ledge_3a", CoordGrid(3015, 3334, 3), em_face_east, SEQ_JUMP_HURDLE, 13.4),
    Ledge3b("loc.rooftops_falador_ledge_3b", CoordGrid(3015, 3334, 3), em_face_east, SEQ_JUMP_HURDLE, 13.4),
    Ledge4("loc.rooftops_falador_ledge_4", CoordGrid(3019, 3334, 3), em_face_east, SEQ_JUMP_HURDLE, 13.4),
}

class FaladorRooftopScript @Inject constructor(private val marks: MarkOfGrace) : PluginScript() {
    private val course = RooftopCourse.Falador

    override fun ScriptContext.startup() {
        onOpLoc1("loc.rooftops_falador_wallclimb") { climbRoughWall() }
        onOpLoc1("loc.rooftops_falador_tightrope_1") { crossTightrope1() }
        onOpLoc1("loc.rooftops_falador_handholds_start") { crossHandHolds() }
        onOpLoc1("loc.rooftops_falador_tightrope_2") { crossTightrope2() }
        onOpLoc1("loc.rooftops_falador_tightrope_3") { crossTightrope3() }
        onOpLoc1("loc.rooftops_falador_edge") { jumpFinalEdge() }

        for (jump in RooftopJump.entries) {
            onOpLoc1(jump.loc) { leap(jump) }
        }
    }

    private suspend fun ProtectedAccess.climbRoughWall() {
        arriveDelay()
        if (!hasLevel()) {
            return
        }
        anim(SEQ_REACH_FOR_LADDER, delay = 15)
        delay(2)
        telejump(WALL_CLIMB_LANDING)
        player.highlightedCourse = course.helperCourse
        marks.rollLapStart(player, course)
        statAdvance(Agility.STAT, XP_ROUGH_WALL)
    }

    private suspend fun ProtectedAccess.crossTightrope1() {
        arriveDelay()
        if (!hasLevel()) {
            return
        }
        soundSynth(SOUND_TIGHTROPE, loops = 5)
        balanceAcross(CoordGrid(3047, 3343, 3), em_face_east)
        exactMove(
            coords,
            CoordGrid(3047, 3344, 3),
            delay1 = 0,
            delay2 = Agility.WALK_CYCLES_PER_TILE,
            dir = em_face_north,
        )
        delay(1)
        resetAnim()
        statAdvance(Agility.STAT, XP_TIGHTROPE_1)
    }

    /**
     * The only obstacle that changes height mid-way: the player swings onto the hand holds one plane
     * below the roofs, shuffles north along them, then pulls back up at the far end.
     */
    private suspend fun ProtectedAccess.crossHandHolds() {
        arriveDelay()
        if (!hasLevel()) {
            return
        }
        exactMove(coords, CoordGrid(3050, 3351, 3), delay1 = 25, delay2 = 30, dir = em_face_north)
        anim(SEQ_WALL_JUMP, delay = 20)
        soundSynth(SOUND_WALL_JUMP, delay = 20)
        delay(1)

        telejump(CoordGrid(3050, 3351, 2))
        anim(SEQ_HANDHOLDS_MIDDLE)
        soundSynth(SOUND_HANDHOLDS, delay = 35)
        delay(1)

        exactMove(coords, CoordGrid(3051, 3352, 2), delay1 = 13, delay2 = 22, dir = em_face_north)
        delay(1)

        for (z in 3353..3355) {
            exactMove(coords, CoordGrid(3051, z, 2), delay1 = 34, delay2 = 52, dir = em_face_north)
            anim(SEQ_HANDHOLDS_MIDDLE)
            soundSynth(SOUND_HANDHOLDS, delay = 35)
            delay(2)
        }

        exactMove(coords, CoordGrid(3050, 3357, 2), delay1 = 34, delay2 = 60, dir = em_face_north)
        anim(SEQ_HANDHOLDS_LAST)
        soundSynth(SOUND_HANDHOLDS, delay = 35)
        delay(2)

        telejump(CoordGrid(3050, 3357, 3))
        resetAnim()
        statAdvance(Agility.STAT, XP_HAND_HOLDS)
    }

    private suspend fun ProtectedAccess.crossTightrope2() {
        arriveDelay()
        if (!hasLevel()) {
            return
        }
        soundSynth(SOUND_TIGHTROPE, loops = 4)
        balanceAcross(CoordGrid(3033, 3361, 3), em_face_west)
        balanceAcross(CoordGrid(3028, 3356, 3), em_face_west)
        balanceAcross(CoordGrid(3028, 3354, 3), em_face_south)
        resetAnim()
        statAdvance(Agility.STAT, XP_TIGHTROPE_2)
    }

    private suspend fun ProtectedAccess.crossTightrope3() {
        arriveDelay()
        if (!hasLevel()) {
            return
        }
        exactMove(coords, CoordGrid(3021, 3353, 3), delay1 = 0, delay2 = 89, dir = em_face_west)
        anim(SEQ_LOG_BALANCE)
        soundSynth(SOUND_TIGHTROPE_3, delay = 5)
        delay(3)
        resetAnim()
        soundSynth(SOUND_TIGHTROPE_3_LAND)
        statAdvance(Agility.STAT, XP_TIGHTROPE_3)
    }

    private suspend fun ProtectedAccess.leap(jump: RooftopJump) {
        arriveDelay()
        if (!hasLevel()) {
            return
        }
        exactMove(coords, jump.dest, delay1 = 15, delay2 = 30, dir = jump.dir)
        anim(jump.seq)
        soundSynth(SOUND_JUMP)
        delay(1)
        resetAnim()
        statAdvance(Agility.STAT, jump.xp)
    }

    private suspend fun ProtectedAccess.jumpFinalEdge() {
        arriveDelay()
        if (!hasLevel()) {
            return
        }
        exactMove(coords, EDGE_LIP, delay1 = 15, delay2 = 30, dir = em_face_east)
        anim(SEQ_JUMP_HURDLE)
        soundSynth(SOUND_JUMP)
        delay(1)

        anim(SEQ_WALL_JUMP_DOWN, delay = 15)
        soundSynth(SOUND_WALL_JUMP_DOWN, delay = 15)
        delay(1)

        player.highlightedCourse = 0
        player.faladorLaps = (player.faladorLaps + 1).coerceAtMost(MAX_LAPS)
        telejump(EDGE_LANDING)
        anim(SEQ_WALL_JUMP_DOWN_LAND)
        statAdvance(Agility.STAT, XP_EDGE)
        mes("Your ${course.displayName} lap count is: <col=ff0000>${player.faladorLaps}</col>.")
    }

    private suspend fun ProtectedAccess.balanceAcross(end: CoordGrid, dir: Int) {
        val tiles = coords.chebyshevDistance(end)
        exactMove(
            coords,
            end,
            delay1 = 0,
            delay2 = tiles * Agility.WALK_CYCLES_PER_TILE,
            dir = dir,
        )
        anim(SEQ_LOG_BALANCE)
        delay(tiles)
    }

    private fun ProtectedAccess.hasLevel(): Boolean {
        if (player.agilityLvl >= course.level) {
            return true
        }
        mes("You need an Agility level of ${course.level} to attempt this obstacle.")
        return false
    }

    private companion object {
        val WALL_CLIMB_LANDING = CoordGrid(3036, 3342, 3)
        val EDGE_LIP = CoordGrid(3027, 3334, 3)
        val EDGE_LANDING = CoordGrid(3029, 3334, 0)

        const val MAX_LAPS = 65535

        const val XP_ROUGH_WALL = 10.7
        const val XP_TIGHTROPE_1 = 22.7
        const val XP_HAND_HOLDS = 60.3
        const val XP_TIGHTROPE_2 = 60.3
        const val XP_TIGHTROPE_3 = 53.6
        const val XP_EDGE = 241.2
    }
}
