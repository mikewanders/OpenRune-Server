package org.rsmod.content.areas.city.varrock.locs

import dev.openrune.rscm.RSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.api.config.Constants.em_face_north
import org.rsmod.api.config.Constants.em_face_south
import org.rsmod.api.config.refs.params
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.righthand
import org.rsmod.api.player.stat.agilityLvl
import org.rsmod.api.player.stat.baseAttackLvl
import org.rsmod.api.player.stat.baseCookingLvl
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.repo.world.WorldRepository
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpLoc2
import org.rsmod.api.script.onOpLoc3
import org.rsmod.api.script.onPlayerLogin
import org.rsmod.content.generic.locs.doors.walkThroughInviswallDoor
import org.rsmod.content.quest.manager.QuestRequirements
import org.rsmod.game.entity.Player
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.type.getOrNull
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private var Player.whiteTree by intVarBit("varbit.garden_white_tree_varbit")

class VarrockLocScript
@Inject
constructor(private val locRepo: LocRepository, private val worldRepo: WorldRepository) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1("loc.fai_varrock_resting_broken_door") { brokenDoor() }
        onOpLoc1("loc.ds2_varrock_door") {
            walkThroughInviswallDoor(
                locRepo,
                it.vis,
                "loc.ds2_varrock_door_open",
                "synth.varrock_castle_door_open",
            )
        }
        onOpLoc1("loc.vmq1_prisondoor") { lockedPrisonGate() }
        onOpLoc1("loc.fai_varrock_cook_guild_door") { cooksGuildUpperDoor(it.vis) }

        onOpLoc1("loc.fai_varrock_bank_stairs") { descendBankStairs() }
        onOpLoc1("loc.fai_varrock_stairs_from_cellar") { climbBankCellarStairs() }

        onOpLoc1("loc.fai_varrock_mill_spiralstairs") { climbMillStairs(it.loc, UP_FROM_GROUND) }
        onOpLoc1("loc.fai_varrock_mill_spiralstairs_middle") { millStairsMenu(it.loc) }
        onOpLoc2("loc.fai_varrock_mill_spiralstairs_middle") {
            climbMillStairs(it.loc, UP_FROM_MIDDLE)
        }
        onOpLoc3("loc.fai_varrock_mill_spiralstairs_middle") {
            climbMillStairs(it.loc, DOWN_FROM_MIDDLE)
        }
        onOpLoc1("loc.fai_varrock_mill_spiralstairs_top") { climbMillStairs(it.loc, DOWN_FROM_TOP) }
        onOpLoc1("loc.dairy_churn_metal") { churn() }

        onOpLoc1("loc.fai_varrock_chest_closed") { openChest(it.loc) }
        onOpLoc1("loc.fai_varrock_chest_open") { searchChest() }
        onOpLoc2("loc.fai_varrock_chest_open") { shutChest(it.loc) }
        onOpLoc1("loc.fai_varrock_sword_dummy") { hitDummy(it.loc) }

        onOpLoc1("loc.manholeclosed") { openManhole(it.loc) }
        onOpLoc1("loc.manholeopen") { climbDownManhole() }
        onOpLoc2("loc.manholeopen") { closeManhole(it.loc) }
        onOpLoc1("loc.fai_varrock_manhole_ladder") { climbUpManhole() }

        onOpLoc1("loc.garden_trellis_concave_shortcut") { climbTrellis(it.loc) }
        onOpLoc1("loc.garden_white_tree_patch") { pickWhiteTreeFruit() }
        onPlayerLogin { player.growWhiteTreeFruit() }
    }

    private suspend fun ProtectedAccess.brokenDoor() {
        arriveDelay()
        mes("It looks about as open as it's ever going to be.")
    }

    private suspend fun ProtectedAccess.lockedPrisonGate() {
        arriveDelay()
        soundSynth("synth.locked")
        mesbox("The gate is securely locked.")
    }

    private suspend fun ProtectedAccess.cooksGuildUpperDoor(door: BoundLocInfo) {
        if (player.baseCookingLvl < MASTER_LEVEL) {
            mesbox(
                "You must have completed the Varrock Achievement Diary Hard tasks or master " +
                    "the Cooking skill to gain entry."
            )
            return
        }
        walkThroughInviswallDoor(
            locRepo,
            door,
            "loc.fai_varrock_cook_guild_door_inactive",
            "synth.door_open",
        )
    }

    private suspend fun ProtectedAccess.descendBankStairs() {
        arriveDelay()
        telejump(BANK_CELLAR_LANDING)
    }

    private suspend fun ProtectedAccess.climbBankCellarStairs() {
        if (player.coords.z < BANK_CELLAR_MIN_Z) {
            return
        }
        arriveDelay()
        telejump(BANK_STAIRS_TOP)
    }

    private fun ProtectedAccess.climbMillStairs(stairs: BoundLocInfo, step: MillStep) {
        telejump(stairs.coords.translate(step.dx, step.dz, step.dLevel))
    }

    private suspend fun ProtectedAccess.millStairsMenu(stairs: BoundLocInfo) {
        startDialogue {
            val step =
                choice2(
                    "Climb up.",
                    UP_FROM_MIDDLE,
                    "Climb down.",
                    DOWN_FROM_MIDDLE,
                    title = "Climb up or down the stairs?",
                )
            access.climbMillStairs(stairs, step)
        }
    }

    private suspend fun ProtectedAccess.churn() {
        arriveDelay()
        if (CHURNABLE.none { inv.contains(it) }) {
            mes("You need some milk, cream or butter to use in the churn.")
        }
    }

    private suspend fun ProtectedAccess.openChest(chest: BoundLocInfo) {
        arriveDelay()
        anim("seq.human_openchest")
        soundSynth("synth.varrock_chest_open")
        delay(1)
        locRepo.change(chest, "loc.fai_varrock_chest_open", CHEST_OPEN_TICKS)
    }

    private suspend fun ProtectedAccess.searchChest() {
        arriveDelay()
        mes("You search the chest but find nothing.")
    }

    private suspend fun ProtectedAccess.shutChest(chest: BoundLocInfo) {
        arriveDelay()
        anim("seq.human_openchest")
        locRepo.change(chest, "loc.fai_varrock_chest_closed", CHEST_OPEN_TICKS)
    }

    private suspend fun ProtectedAccess.hitDummy(dummy: BoundLocInfo) {
        arriveDelay()
        spam("You hit the dummy.")
        anim(player.weaponAttackAnim())
        locAnim(worldRepo, dummy, "seq.varrock_heavybag_defend")
        soundSynth("synth.human_unarmedblock")
        delay(1)
        if (player.baseAttackLvl >= DUMMY_MAX_ATTACK) {
            mes("There is nothing more you can learn from hitting a dummy.")
            return
        }
        statAdvance("stat.attack", DUMMY_XP)
    }

    private fun Player.weaponAttackAnim(): String {
        val anim = getOrNull(righthand)?.paramOrNull(params.attack_anim_stance1)
        return anim?.let { RSCM.getReverseMapping(RSCMType.SEQ, it.id) }
            ?: "seq.human_unarmedpunch"
    }

    private suspend fun ProtectedAccess.openManhole(manhole: BoundLocInfo) {
        arriveDelay()
        spam("You pull back the cover from over the manhole.")
        locRepo.change(manhole, "loc.manholeopen", MANHOLE_OPEN_TICKS)
        soundSynth("synth.coffin_open")
    }

    private suspend fun ProtectedAccess.climbDownManhole() {
        arriveDelay()
        spam("You climb down through the manhole.")
        telejump(SEWER_LANDING)
    }

    private suspend fun ProtectedAccess.closeManhole(manhole: BoundLocInfo) {
        arriveDelay()
        spam("You place the cover back over the manhole.")
        locRepo.change(manhole, "loc.manholeclosed", MANHOLE_OPEN_TICKS)
    }

    private suspend fun ProtectedAccess.climbUpManhole() {
        arriveDelay()
        anim("seq.human_reachforladder")
        delay(1)
        telejump(MANHOLE_EXIT)
    }

    private suspend fun ProtectedAccess.climbTrellis(trellis: BoundLocInfo) {
        if (!QuestRequirements.hasCompleted(player, GARDEN_OF_TRANQUILLITY)) {
            mes("You need to help Queen Ellamaria with her garden before you can climb this.")
            return
        }
        if (player.agilityLvl < TRELLIS_AGILITY) {
            mes("You need an Agility level of $TRELLIS_AGILITY to climb this trellis.")
            return
        }
        arriveDelay()
        val leaving = player.coords == trellis.coords
        val dest = if (leaving) trellis.coords.translateZ(-1) else trellis.coords
        anim("seq.human_climb_trellis")
        exactMove(
            coords,
            dest,
            delay1 = 99,
            delay2 = 112,
            dir = if (leaving) em_face_south else em_face_north,
        )
        soundSynth("synth.varrock_trellis_climb")
        soundSynth("synth.varrock_trellis_climb", delay = 75)
        delay(5)
        resetAnim()
    }

    private suspend fun ProtectedAccess.pickWhiteTreeFruit() {
        if (player.whiteTree !in WHITE_TREE_FRUIT_STAGES) {
            return
        }
        if (inv.isFull()) {
            mes("You don't have enough inventory space.")
            return
        }
        arriveDelay()
        anim("seq.picking_high")
        delay(2)
        spam("You pick a white fruit.")
        player.whiteTree--
        invAdd(inv, "obj.garden_white_tree_fruit")
        soundSynth("synth.varrock_white_tree_pick")
        statAdvance("stat.farming", WHITE_FRUIT_XP)
    }

    private fun Player.growWhiteTreeFruit() {
        if (!QuestRequirements.hasCompleted(this, GARDEN_OF_TRANQUILLITY)) {
            return
        }
        if (whiteTree == 0 || whiteTree >= WHITE_TREE_FULLY_GROWN) {
            whiteTree = WHITE_TREE_FULL_OF_FRUIT
        }
    }

    private class MillStep(val dx: Int, val dz: Int, val dLevel: Int)

    private companion object {
        val UP_FROM_GROUND = MillStep(0, 2, 1)
        val UP_FROM_MIDDLE = MillStep(0, -1, 1)
        val DOWN_FROM_MIDDLE = MillStep(0, 2, -1)
        val DOWN_FROM_TOP = MillStep(0, 2, -1)

        val BANK_CELLAR_LANDING = CoordGrid(3190, 9834, 0)
        val BANK_STAIRS_TOP = CoordGrid(3186, 3434, 0)
        const val BANK_CELLAR_MIN_Z = 9000

        val SEWER_LANDING = CoordGrid(3237, 9858, 0)
        val MANHOLE_EXIT = CoordGrid(3236, 3458, 0)
        const val MANHOLE_OPEN_TICKS = 500

        const val CHEST_OPEN_TICKS = 500
        const val DUMMY_MAX_ATTACK = 8
        const val DUMMY_XP = 5.0
        const val MASTER_LEVEL = 99

        const val GARDEN_OF_TRANQUILLITY = "quest_gardenoftranquillity"
        const val TRELLIS_AGILITY = 35
        const val WHITE_TREE_FULLY_GROWN = 8
        const val WHITE_TREE_FULL_OF_FRUIT = 12
        val WHITE_TREE_FRUIT_STAGES = 9..12
        const val WHITE_FRUIT_XP = 12.0

        val CHURNABLE = listOf("obj.bucket_milk", "obj.pot_of_cream", "obj.pot_of_butter")
    }
}
