package org.rsmod.content.quest.manager

import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import dev.openrune.types.util.UncheckedType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.rsmod.api.testing.GameTestState
import org.rsmod.api.testing.scope.GameTestScope
import org.rsmod.game.inv.InvObj

@OptIn(UncheckedType::class)
@Execution(ExecutionMode.SAME_THREAD)
class QuestTradesTest {
    @Test
    fun GameTestState.`the casket opens into all three treasures`() =
        runGameTest {
            player.clearInv()
            player.inv[0] = obj(CASKET, 1)
            var opened = false
            player.withProtectedAccess { opened = tradeItems(mapOf(CASKET to 1), TREASURE) }
            assertTrue(opened)
            assertEquals(0, player.inv.count(CASKET))
            assertEquals(450, player.inv.count(COINS))
            assertEquals(1, player.inv.count(RING))
            assertEquals(1, player.inv.count(EMERALD))
        }

    @Test
    fun GameTestState.`a casket in a nearly full pack stays shut and nothing is lost`() =
        runGameTest {
            player.clearInv()
            player.inv[0] = obj(CASKET, 1)
            fillAllBut(freeSlots = 1)
            var opened = true
            player.withProtectedAccess { opened = tradeItems(mapOf(CASKET to 1), TREASURE) }
            assertFalse(opened)
            assertEquals(1, player.inv.count(CASKET))
            assertEquals(0, player.inv.count(COINS))
            assertEquals(0, player.inv.count(RING))
            assertEquals(0, player.inv.count(EMERALD))
        }

    @Test
    fun GameTestState.`paying for an item with a full pack keeps the coins`() =
        runGameTest {
            player.clearInv()
            player.inv[0] = obj(COINS, 100)
            fillAllBut(freeSlots = 0)
            var paid = true
            player.withProtectedAccess { paid = tradeItems(mapOf(COINS to 15), mapOf(KEY to 1)) }
            assertFalse(paid)
            assertEquals(100, player.inv.count(COINS))
            assertEquals(0, player.inv.count(KEY))
        }

    @Test
    fun GameTestState.`paying with room takes exactly the price`() =
        runGameTest {
            player.clearInv()
            player.inv[0] = obj(COINS, 100)
            var paid = false
            player.withProtectedAccess { paid = tradeItems(mapOf(COINS to 15), mapOf(KEY to 1)) }
            assertTrue(paid)
            assertEquals(85, player.inv.count(COINS))
            assertEquals(1, player.inv.count(KEY))
        }

    @Test
    fun GameTestState.`a gift with no room is refused`() =
        runGameTest {
            player.clearInv()
            fillAllBut(freeSlots = 0)
            var given = true
            player.withProtectedAccess { given = tradeItems(give = mapOf(CASKET to 1)) }
            assertFalse(given)
            assertEquals(0, player.inv.count(CASKET))
        }

    private fun obj(name: String, count: Int) = InvObj(name.asRSCM(RSCMType.OBJ), count)

    private fun GameTestScope.fillAllBut(freeSlots: Int) {
        val empty = (0 until player.inv.size).filter { player.inv[it] == null }
        for (slot in empty.dropLast(freeSlots)) {
            player.inv[slot] = obj(FILLER, 1)
        }
    }

    private companion object {
        const val CASKET = "obj.pirate_casket"
        const val COINS = "obj.coins"
        const val RING = "obj.gold_ring"
        const val EMERALD = "obj.emerald"
        const val KEY = "obj.princeskey"
        const val FILLER = "obj.beer"
        val TREASURE = mapOf(COINS to 450, RING to 1, EMERALD to 1)
    }
}
