package org.rsmod.content.areas.city.varrock.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.stat.baseCookingLvl
import org.rsmod.api.player.vars.intVarBit
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.shops.Shops
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private var Player.romilyPieType by intVarBit("varbit.varrock_romily_pie_type")
private var Player.romilyPiesOwed by intVarBit("varbit.varrock_romily_pie_count")

private enum class RomilyPie(val obj: String, val title: String, val level: Int, val reward: Int) {
    Redberry("obj.redberry_pie", "Redberry", 10, 15),
    Meat("obj.meat_pie", "Meat", 20, 18),
    Mud("obj.mud_pie", "Mud", 29, 67),
    Apple("obj.apple_pie", "Apple", 30, 37),
    Garden("obj.garden_pie", "Garden", 34, 30),
    Fish("obj.fish_pie", "Fish", 47, 125),
    Admiral("obj.admiral_pie", "Admiral", 70, 387),
    Wild("obj.wild_pie", "Wild", 85, 227),
    Summer("obj.summer_pie", "Summer", 95, 175),
}

class CooksGuildNpcs @Inject constructor(private val shops: Shops) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.merchant_pies") { startDialogue(it.npc) { romily(it.npc) } }
        onOpNpc3("npc.merchant_pies") { player.openPieShop(it.npc) }
    }

    private fun Player.openPieShop(npc: Npc) {
        shops.open(this, npc, "Pie Shop", "inv.shop_pies")
    }

    private suspend fun Dialogue.romily(npc: Npc) {
        chatNpc(happy, "Hello and welcome to my pie shop, how can I help you?")
        val errand = player.romilyErrand()
        val option =
            choice3(
                "I'd like to buy some pies.",
                1,
                if (errand == null) "Do you need any help?" else "I've got those pies you wanted.",
                2,
                "I'm good thanks.",
                3,
            )
        when (option) {
            1 -> player.openPieShop(npc)
            2 -> if (errand == null) offerHelp() else deliverPies(errand)
            3 -> chatPlayer(neutral, "I'm good thanks.")
        }
    }

    private suspend fun Dialogue.offerHelp() {
        chatPlayer(quiz, "Do you need any help?")
        chatNpc(
            neutral,
            "Actually I could, you see I'm running out of stock and I don't have time to bake " +
                "any more pies. Would you be willing to bake me some pies? I'll pay you well for " +
                "them.",
        )
        if (!choice2("Sure, what do you need?", true, "Sorry, I can't help you.", false)) {
            chatPlayer(neutral, "Sorry, I can't help you.")
            return
        }
        chatPlayer(quiz, "Sure, what do you need?")
        val bakeable =
            RomilyPie.entries
                .filter { player.baseCookingLvl >= it.level }
                .ifEmpty { listOf(RomilyPie.Redberry) }
        val pie = bakeable[access.random.of(0, bakeable.lastIndex)]
        val amount = access.random.of(MIN_PIES, MAX_PIES)
        player.romilyPieType = pie.ordinal + 1
        player.romilyPiesOwed = amount
        chatNpc(happy, "Great, can you bake me $amount ${pie.title} Pies please.")
    }

    private suspend fun Dialogue.deliverPies(pie: RomilyPie) {
        chatPlayer(happy, "I've got those pies you wanted.")
        val owed = player.romilyPiesOwed
        val handed = minOf(owed, access.invTotal(player.inv, pie.obj))
        if (handed == 0) {
            chatNpc(
                neutral,
                "Doesn't look like you have any of the $owed ${pie.title} Pies I requested.",
            )
            return
        }
        access.invDel(player.inv, pie.obj, count = handed)
        access.invAdd(player.inv, "obj.coins", count = handed * pie.reward)
        val remaining = owed - handed
        if (remaining > 0) {
            player.romilyPiesOwed = remaining
            chatNpc(happy, "Thank you, if you could bring me the other $remaining that'd be great!")
            return
        }
        player.romilyPieType = 0
        player.romilyPiesOwed = 0
        chatNpc(happy, "Thank you very much!")
    }

    private fun Player.romilyErrand(): RomilyPie? =
        RomilyPie.entries.getOrNull(romilyPieType - 1)?.takeIf { romilyPiesOwed > 0 }

    private companion object {
        const val MIN_PIES = 4
        const val MAX_PIES = 28
    }
}
