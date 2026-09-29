package org.rsmod.content.areas.wilderness

import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.api.npc.interact.AiPlayerInteractions
import org.rsmod.api.npc.opPlayer2
import org.rsmod.api.player.hook.PlayerObjTakenHook
import org.rsmod.api.player.output.mes
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.game.entity.Player
import org.rsmod.game.obj.Obj
import org.rsmod.map.CoordGrid
import org.rsmod.map.zone.ZoneKey

/**
 * The wine on the Chaos Temple's ground floor, next to Goblin Village. Taking or grabbing it turns
 * every monk downstairs on the player, while the wine upstairs can be grabbed in peace.
 */
public class ChaosTempleWineHook
@Inject
constructor(
    private val npcRepo: NpcRepository,
    private val aiPlayerInteractions: AiPlayerInteractions,
) : PlayerObjTakenHook {
    private val wine by lazy { "obj.wine_of_zamorak".asRSCM(RSCMType.OBJ) }
    private val monks by lazy { MONKS.map { it.asRSCM(RSCMType.NPC) }.toSet() }

    override fun onTaken(player: Player, obj: Obj, telegrab: Boolean) {
        if (obj.type != wine || !obj.coords.isInTempleGroundFloor()) {
            return
        }
        val angered =
            npcRepo
                .findAll(ZoneKey.from(obj.coords), zoneRadius = 1)
                .filter { it.id in monks && it.coords.isInTempleGroundFloor() }
                .toList()
        if (angered.isEmpty()) {
            return
        }
        player.mes("STOP STEALING MY WINE! GAH!")
        angered.random().say("Leave Zamorak's wine alone!")
        for (monk in angered) {
            monk.opPlayer2(player, aiPlayerInteractions)
        }
    }

    private fun CoordGrid.isInTempleGroundFloor(): Boolean =
        level == 0 && x in TEMPLE_X && z in TEMPLE_Z

    private companion object {
        private val MONKS = listOf("npc.chaos_temple_monk1", "npc.chaos_temple_monk2")
        private val TEMPLE_X = 2926..2942
        private val TEMPLE_Z = 3509..3521
    }
}
