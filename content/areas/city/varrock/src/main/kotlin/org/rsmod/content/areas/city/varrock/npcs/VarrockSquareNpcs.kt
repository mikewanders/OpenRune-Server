package org.rsmod.content.areas.city.varrock.npcs

import dev.openrune.definition.type.widget.IfEvent
import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onAiTimer
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.api.script.onOpNpc4
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class VarrockSquareNpcs @Inject constructor(private val random: GameRandom) : PluginScript() {
    override fun ScriptContext.startup() {
        for (eliza in listOf("npc.speedrunning_shop_1op", "npc.speedrunning_shop_3op")) {
            onOpNpc1(eliza) { startDialogue(it.npc) { eliza() } }
        }
        onOpNpc3("npc.speedrunning_shop_3op") { openSpeedrunningRewards() }
        onOpNpc4("npc.speedrunning_shop_3op") { startDialogue(it.npc) { claimTrophies() } }

        for (nevet in listOf("npc.events_shop_1op", "npc.events_shop_3op")) {
            onOpNpc1(nevet) { startDialogue(it.npc) { nevet() } }
        }
        onOpNpc3("npc.events_shop_3op") { openEventRewards() }

        onOpNpc1("npc.qip_soa_newspaperseller") { startDialogue(it.npc) { benny() } }
        onAiTimer("npc.qip_soa_newspaperseller") { npc.say(random.pick(BENNY_SHOUTS)) }

        onOpNpc1("npc.baraek") { startDialogue(it.npc) { baraek() } }
    }

    private suspend fun Dialogue.eliza() {
        chatNpc(quiz, "Hey there. Looking to buy something?")
        if (vars[ELIZA_MET] == 0) {
            vars[ELIZA_MET] = 1
            elizaSelling()
            return
        }
        val option =
            choice4("Yes please.", 1, "What are you selling?", 2, ELIZA_CLAIM, 3, "I'll pass.", 4)
        when (option) {
            1 -> elizaYes()
            2 -> elizaSelling()
            3 -> claimTrophies()
            4 -> pass()
        }
    }

    private suspend fun Dialogue.elizaSelling() {
        chatPlayer(quiz, "What are you selling?")
        chatNpc(
            happy,
            "A selection of fun items for the best of adventurers! That said, be aware that I " +
                "have little interest in the usual currencies...",
        )
        chatPlayer(confused, "Right...")
        mesbox(
            "You can only purchase items from the Speedrunning Rewards Shop using Speedrun " +
                "Points. Speedrun Points can only be earned on Speedrunning Worlds."
        )
        chatNpc(quiz, "So, looking to buy?")
        when (choice3("Yes please.", 1, ELIZA_CLAIM, 2, "I'll pass.", 3)) {
            1 -> elizaYes()
            2 -> claimTrophies()
            3 -> pass()
        }
    }

    private suspend fun Dialogue.elizaYes() {
        chatPlayer(happy, "Yes please.")
        access.openSpeedrunningRewards()
    }

    private suspend fun Dialogue.claimTrophies() {
        chatPlayer(quiz, ELIZA_CLAIM)
        chatNpc(neutral, "I'm afraid you don't have any trophies to claim.")
        mesbox(
            "A Speedrun Trophy can only be claimed on your main profile if you have earned that " +
                "trophy for all 15 quests available on Speedrunning Worlds."
        )
    }

    private fun ProtectedAccess.openSpeedrunningRewards() {
        ifOpenMainModal("interface.speedrunning_rewards")
        ifSetEvents("component.speedrunning_rewards:rewards_select", 0..5, IfEvent.Op1)
    }

    private suspend fun Dialogue.nevet() {
        chatNpc(quiz, "Hello! Looking to buy?")
        if (vars[NEVET_MET] == 0) {
            vars[NEVET_MET] = 1
            nevetSelling()
            return
        }
        when (choice3("Yes please.", 1, "What are you selling?", 2, "I'll pass.", 3)) {
            1 -> nevetYes()
            2 -> nevetSelling()
            3 -> pass()
        }
    }

    private suspend fun Dialogue.nevetSelling() {
        chatPlayer(quiz, "What are you selling?")
        chatNpc(
            happy,
            "A selection of items for those who partake in variety! That said, be aware that I " +
                "have little interest in coins...",
        )
        chatPlayer(confused, "Right...")
        mesbox(
            "You can only purchase items from the Events Rewards Shop using Event Points. Event " +
                "Points can only be earned within limited-time Events on separate worlds."
        )
        chatNpc(quiz, "So, looking to buy?")
        if (choice2("Yes please.", true, "I'll pass.", false)) {
            nevetYes()
        } else {
            pass()
        }
    }

    private suspend fun Dialogue.nevetYes() {
        chatPlayer(happy, "Yes please.")
        access.openEventRewards()
    }

    private fun ProtectedAccess.openEventRewards() {
        ifOpenMainModal("interface.event_rewards")
        ifSetEvents("component.event_rewards:rewards_select", 0..9, IfEvent.Op1)
    }

    private suspend fun Dialogue.pass() {
        chatPlayer(neutral, "I'll pass.")
        chatNpc(happy, "Fair enough.")
    }

    private suspend fun Dialogue.benny() {
        val option =
            choice4(
                "Can I have a newspaper, please?",
                1,
                "How much does a paper cost?",
                2,
                "Varrock Herald? Never heard of it.",
                3,
                "Anything interesting in there?",
                4,
            )
        when (option) {
            1 -> {
                chatPlayer(neutral, "Can I have a newspaper, please?")
                chatNpc(happy, "Certainly, guv. That'll be 50 coins, please.")
            }
            2 -> {
                chatPlayer(quiz, "How much does a paper cost?")
                chatNpc(happy, "Just 50 coins! An absolute bargain! Want one?")
            }
            3 -> {
                chatPlayer(neutral, "Varrock Herald? Never heard of it.")
                chatNpc(
                    angry,
                    "For the illiterate amongst us, I shall elucidate. The Varrock Herald is a " +
                        "new newspaper. It is edited, printed and published by myself, Benny " +
                        "Gutenberg, and each edition promises to enthrall the reader with",
                )
                chatNpc(
                    angry,
                    "captivating material! Now, can I interest you in buying one for a mere 50 " +
                        "coins?",
                )
            }
            4 -> {
                chatPlayer(quiz, "Anything interesting in there?")
                chatNpc(
                    happy,
                    "Of course there is, mate. Packed full of thought provoking insights, " +
                        "contentious interviews and celebrity scandalmongering! An excellent " +
                        "read and all for just 50 coins! Want one?",
                )
            }
        }
        buyNewspaper()
    }

    private suspend fun Dialogue.buyNewspaper() {
        if (access.invTotal(player.inv, "obj.coins") < NEWSPAPER_PRICE) {
            chatPlayer(sad, "Maybe when I've got more money on me.")
            chatNpc(sad, "Well, no cash, no paper. Live in ignorance.")
            return
        }
        if (!choice2("Sure, here you go...", true, "No, thanks.", false)) {
            chatNpc(neutral, "Okay, suit yourself. Plenty more fish in the sea.")
            return
        }
        chatPlayer(happy, "Sure, here you go...")
        access.invDel(player.inv, "obj.coins", count = NEWSPAPER_PRICE)
        access.invAdd(player.inv, "obj.qip_soa_newspaper2")
    }

    private suspend fun Dialogue.baraek() {
        val wolfFur = "obj.grey_wolf_fur" in player.inv
        val fur = "obj.fur" in player.inv
        val option =
            when {
                wolfFur ->
                    choice3(
                        "Can you sell me some furs?",
                        1,
                        "Hello! I'm in search of a quest.",
                        2,
                        "Would you like to buy my grey wolf fur?",
                        3,
                    )
                fur ->
                    choice3(
                        "Can you sell me some furs?",
                        1,
                        "Hello! I'm in search of a quest.",
                        2,
                        "Would you like to buy my fur?",
                        4,
                    )
                else ->
                    choice2(
                        "Can you sell me some furs?",
                        1,
                        "Hello! I'm in search of a quest.",
                        2,
                    )
            }
        when (option) {
            1 -> baraekSellFur()
            2 -> {
                chatPlayer(happy, "Hello! I'm in search of a quest.")
                chatNpc(neutral, "Sorry kiddo, I'm a fur trader not a damsel in distress.")
            }
            3 -> baraekBuyWolfFur()
            4 -> baraekBuyFur()
        }
    }

    private suspend fun Dialogue.baraekSellFur() {
        chatPlayer(quiz, "Can you sell me some furs?")
        chatNpc(neutral, "Yeah, sure. They're 20 gold coins each.")
        val accept =
            choice2("Yeah, okay, here you go.", true, "20 gold coins? That's an outrage!", false)
        if (!accept) {
            chatPlayer(angry, "20 gold coins? That's an outrage!")
            chatNpc(
                neutral,
                "Well, I can't go any cheaper than that mate. I have a family to feed.",
            )
            return
        }
        chatPlayer(neutral, "Yeah, okay, here you go.")
        if (sellFur(FUR_PRICE)) {
            return
        }
        chatPlayer(sad, "Oh dear, I don't have enough money!")
        chatNpc(neutral, "Well, my best price is 18 coins.")
        if (!choice2("Okay, here you go.", true, "No thanks, I'll leave it.", false)) {
            chatPlayer(neutral, "No thanks, I'll leave it.")
            chatNpc(neutral, "It's your loss mate.")
            return
        }
        chatPlayer(neutral, "Okay, here you go.")
        if (sellFur(FUR_BEST_PRICE)) {
            return
        }
        chatPlayer(sad, "Oh dear, I don't have that either.")
        chatNpc(
            neutral,
            "Well, I can't go any cheaper than that mate. I've got a family to feed.",
        )
        chatPlayer(neutral, "Oh well, never mind.")
    }

    private suspend fun Dialogue.sellFur(price: Int): Boolean {
        if (access.invTotal(player.inv, "obj.coins") < price) {
            return false
        }
        access.invDel(player.inv, "obj.coins", count = price)
        access.invAdd(player.inv, "obj.fur")
        objbox("obj.fur", "Baraek sells you a fur.")
        return true
    }

    private suspend fun Dialogue.baraekBuyWolfFur() {
        chatPlayer(quiz, "Would you like to buy my grey wolf fur?")
        chatNpc(happy, "GREY WOLF FUR??? NOW you're talking!")
        chatNpc(happy, "Grey wolf fur is something of a desirable item to me.")
        chatNpc(happy, "I'll take all you have for 120 gold pieces per fur, does that sound fair?")
        val refusal = "No! I almost got my throat torn out by a wolf to get this!"
        if (!choice2("Yep, sounds fine.", true, refusal, false)) {
            chatPlayer(angry, refusal)
            return
        }
        chatPlayer(happy, "Yep, that sounds fine.")
        sellAll("obj.grey_wolf_fur", WOLF_FUR_PRICE)
        chatPlayer(happy, "Thanks!")
    }

    private suspend fun Dialogue.baraekBuyFur() {
        chatPlayer(quiz, "Would you like to buy my fur?")
        chatNpc(neutral, "Let's have a look at it.")
        objbox("obj.fur", "You hand Baraek your fur to look at.")
        val each = if (access.invTotal(player.inv, "obj.fur") > 1) "for each one" else "for it"
        chatNpc(neutral, "It's not in the best condition. I guess I could give you 12 coins $each.")
        if (!choice2("Yeah, that'll do.", true, "I think I'll keep hold of it actually.", false)) {
            chatPlayer(neutral, "I think I'll keep hold of it actually!")
            chatNpc(angry, "Oh ok. Didn't want it anyway!")
            return
        }
        chatPlayer(happy, "Yeah, that'll do.")
        sellAll("obj.fur", BEAR_FUR_PRICE)
        chatPlayer(happy, "Thanks!")
    }

    private fun Dialogue.sellAll(obj: String, price: Int) {
        val count = access.invTotal(player.inv, obj)
        access.invDel(player.inv, obj, count = count)
        access.invAdd(player.inv, "obj.coins", count = count * price)
    }

    private val Dialogue.shock
        get() = mesanim("mesanim.shock")

    private companion object {
        const val ELIZA_MET = "varbit.speedrunning_shop_owner_met"
        const val NEVET_MET = "varbit.events_shop_owner_met"
        const val ELIZA_CLAIM = "I'd like to claim some trophies."

        const val NEWSPAPER_PRICE = 50
        const val FUR_PRICE = 20
        const val FUR_BEST_PRICE = 18
        const val WOLF_FUR_PRICE = 120
        const val BEAR_FUR_PRICE = 12

        val BENNY_SHOUTS =
            listOf(
                "Read all about it!",
                "Extra! Extra! Read all about it!",
                "Buy your Varrock Herald now!",
                "Varrock Herald, now only 50 coins!",
                "Get your Varrock Herald here!",
                "Varrock Herald, on sale here!",
            )
    }
}
