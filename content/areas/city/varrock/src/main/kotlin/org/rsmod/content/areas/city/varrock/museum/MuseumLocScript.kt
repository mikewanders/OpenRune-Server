package org.rsmod.content.areas.city.varrock.museum

import dev.openrune.definition.type.widget.IfEvent
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import dev.openrune.types.hunt.HuntVis
import jakarta.inject.Inject
import org.rsmod.api.hunt.NpcSearch
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.forcedWalk
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpLoc1
import org.rsmod.api.script.onOpNpc1
import org.rsmod.content.generic.locs.doors.walkThroughInviswallDoor
import org.rsmod.content.quest.manager.QuestRequirements
import org.rsmod.events.EventBus
import org.rsmod.game.loc.BoundLocInfo
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private class DisplayCase(
    val loc: String,
    val number: Int,
    val model: String,
    val modelName: String,
    val text: String,
    val textFirst: Boolean = false,
    val replacedText: String? = null,
)

private enum class MapFloor(val layer: String) {
    Basement("component.vm_museum_map:vm_level_basement_layer"),
    Ground("component.vm_museum_map:vm_lvl_0"),
    First("component.vm_museum_map:vm_lvl_1"),
    Second("component.vm_museum_map:vm_lvl_2"),
}

class MuseumLocScript
@Inject
constructor(
    private val locRepo: LocRepository,
    private val npcSearch: NpcSearch,
    private val eventBus: EventBus,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpLoc1("loc.fai_varrock_museum_door_closed_r") {
            walkThroughInviswallDoor(
                locRepo,
                it.vis,
                "loc.fai_varrock_museum_door_open_l",
                "synth.door_open",
                openAngle = LocAngle.West,
            )
        }
        onOpLoc1("loc.fai_varrock_museum_door_closed_l") {
            walkThroughInviswallDoor(
                locRepo,
                it.vis,
                "loc.fai_varrock_museum_door_open_r",
                "synth.door_open",
                openAngle = LocAngle.East,
            )
        }
        onOpLoc1("loc.vm_store_room_door") {
            arriveDelay()
            soundSynth("synth.locked")
            mesbox("The door is locked.")
        }

        onOpNpc1("npc.vm_museum_guard") { startDialogue(it.npc) { gateGuard() } }
        onOpLoc1("loc.vm_barrier_guard_gate") {
            arriveDelay()
            passGuardGate()
        }

        onOpLoc1("loc.vm_digsite_wheelbarrow_rock_pile") { takeRocks() }
        onOpLoc1("loc.vm_crate") {
            arriveDelay()
            startDialogue {
                chatPlayer(neutral, "I don't think I've got anything suitable to put in there.")
            }
        }

        for (case in DISPLAY_CASES) {
            onOpLoc1(case.loc) { studyDisplay(case) }
        }
        onIfModalButton("component.vm_digsite:com_1") { closeMuseumModal() }

        for (map in WALL_MAPS) {
            onOpLoc1(map) { lookAtMap() }
        }
        for ((button, floor) in MAP_BUTTONS) {
            onIfModalButton(button) { showMapFloor(floor) }
        }
        onIfModalButton("component.vm_museum_map:vm_map_close") { closeMuseumModal() }

        onOpLoc1("loc.vm_basement_wall_stairs_down") { useWallStairs(it.loc, STAIRS_DOWN) }
        onOpLoc1("loc.vm_basement_wall_stairs_up") { useWallStairs(it.loc, STAIRS_UP) }
        onOpLoc1("loc.fossil_storage_crate1") { openFossilStorage() }
    }

    private suspend fun Dialogue.gateGuard() {
        chatNpcSpecific(
            "Guard",
            "npc.vm_museum_guard",
            happy,
            "Welcome! Would you like to go into the Dig Site archaeology cleaning area?",
        )
        val goIn =
            choice2(
                "Yes, I'll go in!",
                true,
                "No thanks, I'll take a look around out here.",
                false,
            )
        if (!goIn) {
            chatPlayer(neutral, "No thanks, I'll take a look around out here.")
            return
        }
        access.passGuardGate()
    }

    private suspend fun ProtectedAccess.passGuardGate() {
        if (!QuestRequirements.hasCompleted(player, DIG_SITE)) {
            startDialogue {
                chatNpcSpecific(
                    "Museum guard",
                    "npc.vm_museum_guard",
                    neutral,
                    "You're not permitted in this area.",
                )
            }
            return
        }
        val entering = player.coords.z > GATE.z
        val guard = npcSearch.find(GATE, "npc.vm_museum_guard", GUARD_RANGE, HuntVis.Off)
        guard?.anim(
            if (entering) "seq.vm_museum_guard_gate_mirrored" else "seq.vm_museum_guard_gate"
        )
        soundSynth("synth.varrock_museum_gate")
        delay(GATE_SWING_TICKS)
        locRepo.add(GATE, "loc.vm_invisbarrier", GATE_OPEN_TICKS, LocAngle.North, WALL)
        locRepo.add(GATE_OPEN, "loc.vm_barrier_guard_gate", GATE_OPEN_TICKS, LocAngle.West, WALL)
        forcedWalk(if (entering) GATE else GATE_OPEN, crossTiles = 1)
    }

    private suspend fun ProtectedAccess.takeRocks() {
        arriveDelay()
        delay(1)
        while (true) {
            if (inv.isFull()) {
                mes("You don't have enough inventory space.")
                return
            }
            spam("You sort through the pile of finds and take a random rock. Now to clean it!")
            anim("seq.human_pickupfloor")
            invAdd(inv, "obj.vm_digsite_find_unclean")
            soundSynth("synth.pick")
            delay(ROCK_PILE_TICKS)
        }
    }

    private suspend fun ProtectedAccess.studyDisplay(case: DisplayCase) {
        arriveDelay()
        anim("seq.vm_display_case_ponder")
        player.closeKudos(eventBus)
        if (case.textFirst) {
            ifSetText(DISPLAY_TEXT, case.text)
        } else {
            ifSetText(DISPLAY_TEXT, "Item removed for cleaning.")
        }
        ifSetText("component.vm_digsite:vm_digsite_display_num", case.number.toString())
        ifSetModel(case.model, case.modelName.asRSCM())
        runClientScript(
            "clientscript.[clientscript,set_spinner]".asRSCM(RSCMType.CLIENTSCRIPT),
            0,
            5,
            0,
            case.model.asRSCM(RSCMType.COMPONENT),
        )
        if (!case.textFirst) {
            ifSetText(DISPLAY_TEXT, case.text)
        }
        case.replacedText?.let { ifSetText(DISPLAY_TEXT, it) }
        ifOpenMainModal("interface.vm_digsite", colour = 0, transparency = 100)
    }

    private fun ProtectedAccess.closeMuseumModal() {
        resetAnim()
        ifClose()
        player.openKudos(eventBus)
    }

    private suspend fun ProtectedAccess.lookAtMap() {
        arriveDelay()
        anim("seq.vm_display_case_ponder")
        player.closeKudos(eventBus)
        showMapFloor(MapFloor.Ground)
        for (display in EMPTY_MAP_DISPLAYS) {
            ifSetText(display, "Empty<br>display")
        }
        ifOpenMainModal("interface.vm_museum_map", colour = MAP_COLOUR, transparency = 150)
    }

    private fun ProtectedAccess.showMapFloor(floor: MapFloor) {
        val order =
            if (floor == MapFloor.Basement) {
                listOf(MapFloor.Ground, MapFloor.First, MapFloor.Second, MapFloor.Basement)
            } else {
                listOf(MapFloor.Basement, MapFloor.Ground, MapFloor.First, MapFloor.Second)
            }
        for (layer in order) {
            ifSetHide(layer.layer, hide = layer != floor)
        }
    }


    private suspend fun ProtectedAccess.useWallStairs(
        stairs: BoundLocInfo,
        exits: Map<CoordGrid, CoordGrid>,
    ) {
        val dest = exits[stairs.coords] ?: return
        arriveDelay()
        telejump(dest)
    }

    private suspend fun ProtectedAccess.openFossilStorage() {
        arriveDelay()
        soundSynth("synth.pillory_success")
        ifOpenMainSidePair("interface.fossil_storage", "interface.fossil_storage_inv")
        ifSetEvents(
            "component.fossil_storage:list",
            0..29,
            IfEvent.Op1,
            IfEvent.Op2,
            IfEvent.Op3,
            IfEvent.Op4,
            IfEvent.Op5,
            IfEvent.Op10,
        )
        interfaceInvInit(
            inv = inv,
            target = "component.fossil_storage_inv:inv",
            objRowCount = 4,
            objColCount = 7,
            op1 = "Deposit 1",
            op2 = "Deposit 5",
            op3 = "Deposit 10",
            op4 = "Deposit All",
            op5 = "Deposit X",
        )
        ifSetEvents(
            "component.fossil_storage_inv:inv",
            0..27,
            IfEvent.Op1,
            IfEvent.Op2,
            IfEvent.Op3,
            IfEvent.Op4,
            IfEvent.Op5,
            IfEvent.Op10,
        )
    }

    private companion object {
        const val DIG_SITE = "quest_digsite"
        const val DISPLAY_TEXT = "component.vm_digsite:vm_digsite_text"
        const val MAP_COLOUR = 12632226
        const val GUARD_RANGE = 3
        const val GATE_SWING_TICKS = 3
        const val GATE_OPEN_TICKS = 4
        const val ROCK_PILE_TICKS = 3

        val WALL = LocShape.WallStraight
        val GATE = CoordGrid(3261, 3446, 0)
        val GATE_OPEN = CoordGrid(3261, 3447, 0)

        val STAIRS_DOWN =
            mapOf(
                CoordGrid(3255, 3451, 0) to CoordGrid(1759, 4958, 0),
                CoordGrid(1758, 4963, 0) to CoordGrid(1631, 4957, 0),
            )
        val STAIRS_UP =
            mapOf(
                CoordGrid(1630, 4958, 0) to CoordGrid(1761, 4964, 0),
                CoordGrid(1758, 4959, 0) to CoordGrid(3258, 3452, 0),
            )

        val WALL_MAPS =
            listOf(
                "loc.vm_museum_wallmounted_map_01",
                "loc.vm_museum_wallmounted_map_02",
                "loc.vm_museum_wallmounted_map_03",
            )

        val EMPTY_MAP_DISPLAYS =
            listOf(
                "component.vm_museum_map:vm_staff_of_armadyll",
                "component.vm_museum_map:vm_terracotta_statue",
                "component.vm_museum_map:vm_robert_the_strong",
                "component.vm_museum_map:vm_surok_tomb",
            )

        val MAP_BUTTONS =
            listOf(
                "component.vm_museum_map:lvl0_level_up" to MapFloor.First,
                "component.vm_museum_map:lvl0_level_up_1" to MapFloor.First,
                "component.vm_museum_map:lvl0_level_down_1" to MapFloor.Basement,
                "component.vm_museum_map:lvl0_level_down_1_1" to MapFloor.Basement,
                "component.vm_museum_map:lvl1_level_up" to MapFloor.Second,
                "component.vm_museum_map:lvl1_level_up_1" to MapFloor.Second,
                "component.vm_museum_map:lvl1_level_down" to MapFloor.Ground,
                "component.vm_museum_map:lvl1_level_down_1" to MapFloor.Ground,
                "component.vm_museum_map:lvl2_level_down" to MapFloor.First,
                "component.vm_museum_map:lvl2_level_down_1" to MapFloor.First,
                "component.vm_museum_map:basement_level_up_1" to MapFloor.Ground,
                "component.vm_museum_map:basement_level_up_1_1" to MapFloor.Ground,
            )

        val DISPLAY_CASES =
            listOf(
                DisplayCase(
                    "loc.vm_digsite_finds_talisman",
                    42,
                    "component.vm_digsite:vm_digsite_talisman_model",
                    "models.vm_display_talisman",
                    "Found at the Dig Site east of Varrock by a recently qualified " +
                        "archaeologist. It appears to be a symbol of the god, Zaros.",
                ),
                DisplayCase(
                    "loc.vm_digsite_finds_tablet",
                    43,
                    "component.vm_digsite:vm_digsite_tablet_model",
                    "models.vm_display_tablet",
                    "Excavated recently from a cavernous temple below the Dig Site by a " +
                        "recently qualified archaeologist. It is of great significance, as it " +
                        "proves the existence of a settlement that predates the Saradominist " +
                        "city currently being excavated. The temple appears to be dedicated " +
                        "to the pagan god, Zaros, and survived the destruction of the Zarosian " +
                        "city which was rebuilt by Saradominists.",
                ),
                DisplayCase(
                    "loc.vm_digsite_finds_saradomin_symbol_ancient",
                    37,
                    "component.vm_digsite:vm_digsite_symbol_ancient_model",
                    "models.vm_display_symbol_ancient",
                    "This small symbol was used as a decoration on the Saradominist altars " +
                        "throughout the city. Two or three examples have been found, but this " +
                        "is the best. Worked in blue-enamelled bronze, it has not survived the " +
                        "ages well. We believe this to be from the early years when the city " +
                        "was still being built.",
                ),
                DisplayCase(
                    "loc.vm_digsite_finds_arrow_heads",
                    41,
                    "component.vm_digsite:vm_digsite_arrow_heads_model",
                    "models.vm_display_arrowheads",
                    "Arrowheads of crude bronze have been found along with the finds deeper " +
                        "in the Dig Site, which leads us to believe that the forces occupying " +
                        "the city before the Saradominists used bows, as well as other methods " +
                        "of war.",
                    textFirst = true,
                ),
                DisplayCase(
                    "loc.vm_digsite_finds_vase",
                    38,
                    "component.vm_digsite:vm_digsite_vase_model",
                    "models.vm_display_vase",
                    "One of the few vases found in good condition. Its markings show some " +
                        "kind of celebration to Saradomin.",
                    textFirst = true,
                ),
                DisplayCase(
                    "loc.vm_digsite_finds_jewellery",
                    40,
                    "component.vm_digsite:vm_digsite_jewllery_model",
                    "models.vm_display_jewellery",
                    "Fine silver and gold jewellery has been found concealed in one of the " +
                        "many urns scattered around the Dig Site. Not much is known about it, " +
                        "although most of the items do have Saradominist markings.",
                    textFirst = true,
                ),
                DisplayCase(
                    "loc.vm_digsite_finds_pottery",
                    22,
                    "component.vm_digsite:vm_digsite_pottery_model",
                    "models.vm_display_pottery",
                    "Two beautiful examples of the many items of pottery found around the " +
                        "Dig Site. There appear to be Saradominist markings upon these " +
                        "artefacts, indicating that this was at one time a Saradominist city. " +
                        "This pottery appears to be dated around the beginning of the 4th Age.",
                    textFirst = true,
                    replacedText =
                        "Two distinct types of pottery have been recovered from the Dig Site, " +
                            "adding evidence to the two settlements on this site. The red clay " +
                            "pottery appears to be much older and occasionally has purple " +
                            "Zarosian symbols, while the Saradominist artefacts are usually of " +
                            "a lighter shade with blue decorations.",
                ),
                DisplayCase(
                    "loc.vm_digsite_finds_coin_senntisten",
                    44,
                    "component.vm_digsite:vm_digsite_coin_senntisten_model",
                    "models.vm_display_coin_senntisten",
                    "A battered and bent coin with Zamorakian markings. It bears the word " +
                        "'Senntisten', which we believe to be the original name of the city, " +
                        "Saranthium, before it was rebuilt by those loyal to Saradomin. " +
                        "Partial numbers on the coin would indicate that it is from the year " +
                        "3740, presumably from the 3rd Age.",
                ),
                DisplayCase(
                    "loc.vm_digsite_finds_coin_saranthium",
                    45,
                    "component.vm_digsite:vm_digsite_coin_saranthium_model",
                    "models.vm_display_coin_saranthium",
                    "A coin in very good condition with Saradominist markings. It bears the " +
                        "word 'Saranthium', which we have found to be the name of the city " +
                        "being excavated east of Varrock. The numbers on the coin would " +
                        "indicate that it is from the year 3804, presumably from the 3rd Age " +
                        "as the Godwars were coming to an end.",
                ),
            )
    }
}
