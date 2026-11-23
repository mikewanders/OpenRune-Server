package org.rsmod.content.areas.city.varrock.npcs

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.random.GameRandom
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.repo.world.WorldRepository
import org.rsmod.api.script.onAiTimer
import org.rsmod.api.script.onOpNpc1
import org.rsmod.game.entity.Npc
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class VarrockPalaceNpcs
@Inject
constructor(
    private val random: GameRandom,
    private val locRepo: LocRepository,
    private val worldRepo: WorldRepository,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.king_roald") { startDialogue(it.npc) { kingRoald() } }
        onOpNpc1("npc.aluft_gnome_diplomat_varrock") { startDialogue(it.npc) { ferrnook() } }
        onOpNpc1("npc.surok_surok_type1") { startDialogue(it.npc) { surok() } }
        onOpNpc1("npc.postie_pete") {
            if (it.npc.coords.inPalaceLibrary()) {
                startDialogue(it.npc) { postiePeteLibrary() }
            }
        }

        for (guard in TRAINING_GUARDS) {
            onAiTimer(guard) { npc.hitHeavyBag() }
        }
        onAiTimer("npc.fai_varrock_guard_trainer02") { npc.say(TRAINER_SHOUT) }
    }

    private suspend fun Dialogue.kingRoald() {
        chatPlayer(happy, "Greetings, your majesty.")
        chatNpc(quiz, "Yes, citizen. Do you need something?")
        chatPlayer(neutral, "Not really.")
        chatNpc(
            neutral,
            "You will have to excuse me then. I am very busy as I have a kingdom to run!",
        )
    }

    private suspend fun Dialogue.ferrnook() {
        chatPlayer(neutral, "Hello Ambassador. Are you here visiting King Roald?")
        chatNpc(happy, "Well, in theory, but he always seems to be busy.")
        chatPlayer(happy, "You don't seem that upset by that, though...")
        chatNpc(
            happy,
            "Oh no, I like travelling, and if you become a diplomat patience is a vital skill.",
        )
        chatPlayer(neutral, "I'll try to remember that.")
    }

    private suspend fun Dialogue.surok() {
        chatPlayer(neutral, "Excuse me?")
        chatNpc(
            angry,
            "What do you want? ...Oh, wait. I know! You're probably just like all the others, " +
                "aren't you? After some fancy spell or potion from me, I bet!",
        )
        chatPlayer(
            shock,
            "What? No! At least, I don't think so. What sort of spells do you have?",
        )
        chatNpc(
            angry,
            "Hah! I knew it! I expect you want my Aphro-Dizzy-Yak spell! Want someone to fall " +
                "madly in love with you, eh?",
        )
        chatPlayer(
            shock,
            "That spell sounds very interesting, but I didn't mean to disturb you!",
        )
        chatNpc(
            neutral,
            "Well, I see that you do have some manners. I'm glad to see that you use them.",
        )
        chatNpc(
            neutral,
            "Now, if it's all the same, I am very busy at the moment. Unless you want something " +
                "specific, please come back another time.",
        )
        chatPlayer(neutral, "Yes, of course. Goodbye!")
    }

    private suspend fun Dialogue.postiePeteLibrary() {
        chatPlayer(laugh, "HEY PETE, WHAT ARE YOU DOING HERE?")
        chatNpc(neutral, "Shhhh! It's a library you know!")
        chatPlayer(happy, "Oh, sorry! So, what's new?")
        chatNpc(
            neutral,
            "Not much, just doing some research on Dwarvern Culture. Did you know that " +
                "Keldagrim was originally constructed by a long lost clan who embraced the power " +
                "of magic, and used it to aid with glorious buildings like",
        )
        chatNpc(neutral, "the royal palace?")
        chatPlayer(quiz, "Really??")
        chatNpc(
            neutral,
            "Yes. They even stayed down there for 1000 years or more, in a time they call 'The " +
                "age of Kings'.",
        )
        chatPlayer(shock, "WOW!")
        chatNpc(angry, "Shhhh! That's enough history for one day I think!")
    }

    private fun Npc.hitHeavyBag() {
        aiTimer(random.of(GUARD_SWING_MIN, GUARD_SWING_MAX))
        val bagCoords = coords.translateZ(-1)
        anim("seq.fai_varrock_guard_attack")
        faceSquare(bagCoords)
        val bag = locRepo.findExact(bagCoords, LocShape.CentrepieceStraight) ?: return
        worldRepo.locAnim(bag, "seq.varrock_heavybag_defend")
    }

    private fun CoordGrid.inPalaceLibrary(): Boolean =
        level == 0 && x in 3200..3215 && z in 3490..3500

    private val Dialogue.shock
        get() = mesanim("mesanim.shock")

    private companion object {
        const val GUARD_SWING_MIN = 10
        const val GUARD_SWING_MAX = 11
        const val TRAINER_SHOUT = "Let's get some proper bloodlust here!"

        val TRAINING_GUARDS =
            listOf(
                "npc.fai_varrock_guard_training02",
                "npc.fai_varrock_guard_training02_f",
                "npc.fai_varrock_guard_training02_variant01",
                "npc.fai_varrock_guard_training02_f_variant01",
            )
    }
}
