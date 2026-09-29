package org.rsmod.content.areas.city.varrock.npcs

import jakarta.inject.Inject
import org.rsmod.api.invtx.invAddOrDrop
import org.rsmod.api.invtx.invTakeFee
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.output.spam
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.obj.ObjRepository
import org.rsmod.api.script.onApNpc1
import org.rsmod.api.script.onOpNpc1
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class BlueMoonBartender @Inject constructor(private val objRepo: ObjRepository) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1(BARTENDER) { talk(it.npc) }
        onApNpc1(BARTENDER) {
            if (isWithinDistance(it.npc, ACROSS_BAR)) talk(it.npc) else apRange(ACROSS_BAR)
        }
    }

    private suspend fun ProtectedAccess.talk(npc: Npc) =
        startDialogue(npc) {
            chatNpc(quiz, "What can I do yer for?")
            when (
                choice3(
                    "A glass of your finest ale please.",
                    1,
                    "Can you recommend where an adventurer might make his fortune?",
                    2,
                    "Do you know where I can get some good equipment?",
                    3,
                )
            ) {
                1 -> buyBeer()
                2 -> fortune()
                else -> equipment()
            }
        }

    private suspend fun Dialogue.buyBeer() {
        chatPlayer(happy, "A glass of your finest ale please.")
        chatNpc(happy, "No problemo. That'll be $BEER_PRICE coins.")
        if (!player.invTakeFee(fee = BEER_PRICE)) {
            chatPlayer(sad, "Oh dear. I don't seem to have enough money.")
            return
        }
        player.spam("You buy a pint of beer.")
        player.invAddOrDrop(objRepo, "obj.beer")
    }

    private suspend fun Dialogue.fortune() {
        chatPlayer(quiz, "Can you recommend where an adventurer might make his fortune?")
        chatNpc(
            neutral,
            "Ooh I don't know if I should be giving away information, makes the game too easy.",
        )
        when (
            choice3(
                "Oh ah well...",
                1,
                "Game? What are you talking about?",
                2,
                "Just a small clue?",
                3,
            )
        ) {
            1 -> chatPlayer(sad, "Oh ah well...")
            2 -> {
                chatPlayer(confused, "Game? What are you talking about?")
                chatNpc(
                    neutral,
                    "This world around us... is an online game... called Old School RuneScape.",
                )
                chatPlayer(
                    confused,
                    "Nope, still don't understand what you are talking about. What does " +
                        "'online' mean?",
                )
                chatNpc(
                    neutral,
                    "It's a sort of connection between magic boxes across the world, big boxes " +
                        "on people's desktops and little ones people can carry. They can talk to " +
                        "each other to play games.",
                )
                chatPlayer(angry, "I give up. You're obviously completely mad!")
            }
            else -> {
                chatPlayer(quiz, "Just a small clue?")
                chatNpc(
                    neutral,
                    "Go and talk to the bartender at the Jolly Boar Inn, he doesn't seem to mind " +
                        "giving away clues.",
                )
            }
        }
    }

    private suspend fun Dialogue.equipment() {
        chatPlayer(quiz, "Do you know where I can get some good equipment?")
        chatNpc(
            neutral,
            "Well, there's the sword shop across the road, or there's also all sorts of shops up " +
                "around the market.",
        )
    }

    private companion object {
        const val BARTENDER = "npc.bluemoon_bartender"
        const val BEER_PRICE = 2
        const val ACROSS_BAR = 2
    }
}
