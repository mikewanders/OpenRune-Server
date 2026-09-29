package org.rsmod.content.skills.magic.telegrab

import dev.openrune.ServerCacheManager
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.api.combat.commons.magic.MagicSpell
import org.rsmod.api.combat.manager.MagicRuneManager
import org.rsmod.api.combat.manager.MagicRuneManager.Companion.isFailure
import org.rsmod.api.config.Constants
import org.rsmod.api.invtx.invAdd
import org.rsmod.api.player.hook.PlayerObjTakeValidator
import org.rsmod.api.player.hook.PlayerObjTakenNotifier
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.repo.world.WorldRepository
import org.rsmod.api.script.onApObjT
import org.rsmod.api.script.onPlayerQueueWithArgs
import org.rsmod.api.spells.MagicSpellRegistry
import org.rsmod.game.obj.Obj
import org.rsmod.game.proj.ProjAnim
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

/**
 * Telekinetic Grab. The obj stays on the ground until the projectile lands three ticks after the
 * cast, so another player can still take it first.
 */
class TelekineticGrab
@Inject
constructor(
    private val spells: MagicSpellRegistry,
    private val runes: MagicRuneManager,
    private val objRepo: ObjRepository,
    private val worldRepo: WorldRepository,
    private val takeValidator: PlayerObjTakeValidator,
    private val takenNotifier: PlayerObjTakenNotifier,
) : PluginScript() {
    private val spell: MagicSpell? by lazy {
        ServerCacheManager.getItem(SPELL_OBJ.asRSCM(RSCMType.OBJ))?.let(spells::getObjSpell)
    }

    override fun ScriptContext.startup() {
        onApObjT(SPELL_COMPONENT) { cast(it.obj) }
        onPlayerQueueWithArgs<Obj>(GRAB_QUEUE) { land(it.args) }
    }

    private fun ProtectedAccess.cast(obj: Obj) {
        val spell = spell ?: return
        if (!runes.canCastSpell(player, spell)) {
            return
        }
        val type = ServerCacheManager.getItem(obj.type) ?: return
        val denial = takeValidator.validate(player, obj, type)
        if (denial != null) {
            mes(denial)
            return
        }
        if (!invAddTransaction(obj).success) {
            mes(Constants.dm_take_invspace)
            return
        }
        if (runes.attemptCast(player, spell).isFailure()) {
            return
        }

        faceSquare(obj.coords)
        anim(CAST_SEQ)
        spotanim(CAST_SPOTANIM, height = CAST_SPOTANIM_HEIGHT)
        worldRepo.projAnim(projectile(obj))
        spotanimMap(worldRepo, IMPACT_SPOTANIM, obj.coords, IMPACT_HEIGHT, IMPACT_DELAY)
        soundArea(worldRepo, obj.coords, IMPACT_SYNTH, delay = IMPACT_SYNTH_DELAY, radius = 10)
        soundSynth(CAST_SYNTH)
        statAdvance("stat.magic", spell.castXp)
        takenNotifier.notify(player, obj, telegrab = true)
        queue(GRAB_QUEUE, FLIGHT_TICKS, obj)
    }

    private fun ProtectedAccess.land(obj: Obj) {
        if (!objRepo.del(obj)) {
            mes(Constants.dm_take_taken)
            return
        }
        val grab = invAddTransaction(obj)
        if (grab.success) {
            grab.commitAll()
        } else {
            mes(Constants.dm_take_invspace)
        }
    }

    private fun ProtectedAccess.invAddTransaction(obj: Obj) =
        player.invAdd(player.inv, obj.type, obj.count, autoCommit = false)

    private fun ProtectedAccess.projectile(obj: Obj): ProjAnim =
        ProjAnim(
            spotanim = TRAVEL_SPOTANIM.asRSCM(RSCMType.SPOTANIM),
            startHeight = PROJ_START_HEIGHT,
            endHeight = PROJ_END_HEIGHT,
            startTime = PROJ_START_TIME,
            endTime = PROJ_END_TIME,
            angle = PROJ_ANGLE,
            progress = PROJ_PROGRESS,
            sourceIndex = 0,
            targetIndex = 0,
            startCoord = player.coords,
            endCoord = obj.coords,
        )

    private companion object {
        private const val SPELL_OBJ = "obj.33_tele_grab"
        private const val SPELL_COMPONENT = "component.magic_spellbook:telegrab"
        private const val GRAB_QUEUE = "queue.telegrab"
        private const val FLIGHT_TICKS = 3

        private const val CAST_SEQ = "seq.human_casttelegrab"
        private const val CAST_SPOTANIM = "spotanim.telegrab_casting"
        private const val CAST_SPOTANIM_HEIGHT = 92
        private const val CAST_SYNTH = "synth.telegrab_cast"

        private const val TRAVEL_SPOTANIM = "spotanim.telegrab_travel"
        private const val PROJ_START_HEIGHT = 140
        private const val PROJ_END_HEIGHT = 80
        private const val PROJ_START_TIME = 48
        private const val PROJ_END_TIME = 90
        private const val PROJ_ANGLE = 16
        private const val PROJ_PROGRESS = 64

        private const val IMPACT_SPOTANIM = "spotanim.telegrab_impact"
        private const val IMPACT_HEIGHT = 100
        private const val IMPACT_DELAY = 90
        private const val IMPACT_SYNTH = "synth.telegrab_impact"
        private const val IMPACT_SYNTH_DELAY = 3
    }
}
