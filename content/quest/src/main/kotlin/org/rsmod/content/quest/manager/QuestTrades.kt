package org.rsmod.content.quest.manager

import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import org.rsmod.api.invtx.add
import org.rsmod.api.invtx.delete
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.player.protect.ProtectedAccess

/**
 * Takes every [take] obj and gives every [give] obj from the inventory in one transaction, so
 * either all of them change hands or none do.
 */
public fun ProtectedAccess.tradeItems(
    take: Map<String, Int> = emptyMap(),
    give: Map<String, Int> = emptyMap(),
): Boolean =
    player
        .invTransaction(inv) {
            val target = select(inv)
            for ((obj, count) in take) {
                delete(target, obj.asRSCM(RSCMType.OBJ), count)
            }
            for ((obj, count) in give) {
                add(target, obj.asRSCM(RSCMType.OBJ), count)
            }
        }
        .success
