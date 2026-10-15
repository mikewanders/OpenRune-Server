package org.rsmod.content.areas.city.varrock.museum

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.music.MusicPlayer
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.table.MusicRow
import org.rsmod.content.quest.area.misthalin.SceneCamera
import org.rsmod.content.quest.area.misthalin.lockedScene
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.player.Appearance
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private class Lecture(
    val option: String,
    val request: String,
    val intro: String,
    val anim: String,
    val music: String,
    val vantage: CoordGrid,
    val playerFace: CoordGrid,
    val historianAt: CoordGrid,
    val historianFace: CoordGrid,
    val camera: SceneCamera,
    val returnTo: CoordGrid,
    val lines: List<LectureLine>,
)

class NaturalHistoryScript
@Inject
constructor(
    private val npcRepo: NpcRepository,
    private val musicPlayer: MusicPlayer,
    private val eventBus: EventBus,
) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.vm_natural_historian_west") {
            startDialogue(it.npc) { historian(WEST_LECTURES, naturalHistory = false) }
        }
        onOpNpc1("npc.vm_natural_historian_east") {
            startDialogue(it.npc) { historian(EAST_LECTURES, naturalHistory = false) }
        }
        onOpNpc1("npc.vm_natural_historian_north") {
            startDialogue(it.npc) { historian(NORTH_LECTURES, naturalHistory = false) }
        }
        onOpNpc1("npc.vm_natural_historian_south") {
            startDialogue(it.npc) { historian(SOUTH_LECTURES, naturalHistory = true) }
        }
        onOpNpc1("npc.vm_nathis_apprentice") {
            startDialogue(it.npc) {
                chatNpc(happy, "Thanks for all your help, mate. Hope you enjoy the museum!")
            }
        }
    }

    private suspend fun Dialogue.historian(lectures: List<Lecture>, naturalHistory: Boolean) {
        if (vars[GOT_SPIEL] == 0) {
            chatNpc(
                happy,
                "Hello there and welcome to the Natural History exhibit of the Varrock Museum!",
            )
            chatPlayer(happy, "Hello. So what is it you do here?")
            chatNpc(happy, "Well, we look after all of the research in this section.")
            chatNpc(
                happy,
                "When I'm not doing that, I'm teaching people like yourself about how " +
                    "wonderful the natural world is.",
            )
            chatPlayer(happy, "Nice. So then, I've got a few questions for you.")
            chatNpc(
                happy,
                "Well, fire away and I'll give you an insight into the exciting world of animals.",
            )
            vars[GOT_SPIEL] = 1
        } else {
            val title = if (player.appearance.bodyType == Appearance.BODY_TYPE_A) "sir" else "madam"
            chatNpc(happy, "Hello again, $title, how can I help you on this fine day?")
            chatPlayer(happy, "I was hoping you could tell me about something.")
        }
        val topic =
            if (naturalHistory) {
                choice4(
                    NATURAL_HISTORY,
                    -1,
                    lectures[0].option,
                    0,
                    lectures[1].option,
                    1,
                    ENOUGH,
                    -2,
                )
            } else {
                choice5(
                    lectures[0].option,
                    0,
                    lectures[1].option,
                    1,
                    lectures[2].option,
                    2,
                    lectures[3].option,
                    3,
                    ENOUGH,
                    -2,
                )
            }
        when (topic) {
            -1 -> naturalHistory()
            -2 -> {
                chatPlayer(neutral, ENOUGH)
                chatNpc(happy, "Nonsense! There's always room for more.")
                chatNpc(happy, "And remember, science isn't dull!")
            }
            else -> {
                val lecture = lectures[topic]
                chatPlayer(neutral, lecture.request)
                chatNpc(happy, lecture.intro)
                chatNpc(
                    happy,
                    "If you just follow me to the display case I shall explain all about them.",
                )
                access.lecture(lecture)
            }
        }
    }

    private suspend fun Dialogue.naturalHistory() {
        chatPlayer(neutral, "Tell me about natural history.")
        chatNpc(
            happy,
            "Well, the field of natural history covers a wide range of sciences.",
        )
        chatNpc(
            happy,
            "So we use biology, the study of living things, botany, the study of plants and " +
                "zoology, the study of animals.",
        )
        chatNpc(
            happy,
            "Though the field is growing all the time and we're also using techniques from " +
                "magic, astrology and numerology.",
        )
        chatNpc(happy, "A person interested in natural history is known as a naturalist.")
    }

    private suspend fun ProtectedAccess.lecture(lecture: Lecture) {
        player.closeKudos(eventBus)
        musicPlayer.play(player, MusicRow.getRow(lecture.music))
        var historian: Npc? = null
        lockedScene(
            vantage = lecture.vantage,
            faceAt = lecture.playerFace,
            camera = lecture.camera,
            returnTo = lecture.returnTo,
            underFade = { historian = spawnHistorian(lecture) },
            teardown = { historian?.let(::despawn) },
            cameraRate = CAMERA_RATE,
        ) {
            val npc = historian ?: return@lockedScene
            startDialogue(npc) {
                for (line in lecture.lines) {
                    if (line.anim) {
                        npc.anim(lecture.anim)
                    }
                    chatNpc(happy, line.text)
                }
            }
        }
        musicPlayer.playNext(player)
        player.openKudos(eventBus)
    }

    private fun spawnHistorian(lecture: Lecture): Npc {
        val npc = Npc("npc.vm_natural_historian_cutscene", lecture.historianAt)
        npcRepo.add(npc, HISTORIAN_TICKS)
        npc.faceSquare(lecture.historianFace)
        return npc
    }

    private fun despawn(npc: Npc) {
        if (npc.isSlotAssigned) {
            npcRepo.del(npc, Int.MAX_VALUE)
        }
    }

    private companion object {
        const val GOT_SPIEL = "varbit.vm_nathis_got_spiel"
        const val NATURAL_HISTORY = "Tell me about natural history."
        const val ENOUGH = "That's enough education for one day."
        const val CAMERA_RATE = 100
        const val HISTORIAN_TICKS = 300

        fun camera(eye: CoordGrid, eyeHeight: Int, look: CoordGrid, lookHeight: Int) =
            SceneCamera(eye, eyeHeight, look, lookHeight)

        fun at(x: Int, z: Int) = CoordGrid(x, z, 0)

        val CAMELS =
            Lecture(
                option = "Tell me about camels.",
                request = "Tell me about camels.",
                intro = "Ahh camels, the ships of the desert.",
                anim = "seq.vm_natural_historian_camel_hump",
                music = "dbrow.music_the_desert",
                vantage = at(1736, 4960),
                playerFace = at(1736, 4962),
                historianAt = at(1736, 4962),
                historianFace = at(1736, 4960),
                camera = camera(at(1739, 4956), 500, at(1737, 4962), 300),
                returnTo = at(1736, 4961),
                lines = LectureText.CAMEL,
            )

        val LEECHES =
            Lecture(
                option = "Tell me about leeches.",
                request = "Tell me about leeches.",
                intro = "Ahh leeches, the haemophagic parasites.",
                anim = "seq.vm_natural_historian_leech_twist",
                music = "dbrow.music_deadlands",
                vantage = at(1743, 4960),
                playerFace = at(1743, 4962),
                historianAt = at(1743, 4962),
                historianFace = at(1743, 4960),
                camera = camera(at(1740, 4956), 500, at(1743, 4963), 300),
                returnTo = at(1743, 4961),
                lines = LectureText.LEECH,
            )

        val MOLES =
            Lecture(
                option = "Tell me about moles.",
                request = "Tell me about moles.",
                intro = "Ahh moles, the mammalian mountain makers.",
                anim = "seq.vm_natural_historian_mole_tunnel",
                music = "dbrow.music_the_mad_mole",
                vantage = at(1736, 4960),
                playerFace = at(1736, 4958),
                historianAt = at(1736, 4958),
                historianFace = at(1736, 4960),
                camera = camera(at(1739, 4965), 500, at(1737, 4960), 300),
                returnTo = at(1736, 4959),
                lines = LectureText.MOLE,
            )

        val PENGUINS =
            Lecture(
                option = "Tell me about penguins.",
                request = "Tell me about penguins.",
                intro = "Ahh penguins, the cunning birds of the sea.",
                anim = "seq.vm_natural_historian_penguin_waddle",
                music = "dbrow.music_next_id_remembered",
                vantage = at(1743, 4960),
                playerFace = at(1743, 4958),
                historianAt = at(1743, 4958),
                historianFace = at(1743, 4960),
                camera = camera(at(1740, 4965), 650, at(1743, 4960), 300),
                returnTo = at(1743, 4959),
                lines = LectureText.PENGUIN,
            )

        val SNAILS =
            Lecture(
                option = "Tell me about snails.",
                request = "Tell me about snails.",
                intro = "Ahh snails, the gelatinous gastropods.",
                anim = "seq.vm_natural_historian_snail_antenna",
                music = "dbrow.music_stagnant",
                vantage = at(1775, 4960),
                playerFace = at(1775, 4962),
                historianAt = at(1775, 4962),
                historianFace = at(1775, 4960),
                camera = camera(at(1772, 4956), 525, at(1775, 4963), 300),
                returnTo = at(1775, 4961),
                lines = LectureText.SNAIL,
            )

        val MONKEYS =
            Lecture(
                option = "Tell me about monkeys.",
                request = "Tell me about monkeys.",
                intro = "Ahh monkeys, the simian collective.",
                anim = "seq.vm_natural_historian_monkey_hop",
                music = "dbrow.music_tribal",
                vantage = at(1775, 4960),
                playerFace = at(1775, 4958),
                historianAt = at(1775, 4958),
                historianFace = at(1775, 4960),
                camera = camera(at(1777, 4966), 600, at(1776, 4960), 300),
                returnTo = at(1775, 4959),
                lines = LectureText.MONKEY,
            )

        val SEA_SLUGS =
            Lecture(
                option = "Tell me about sea slugs.",
                request = "Tell me about the sea slugs.",
                intro = "Ahh sea slugs, the cute crustaceans.",
                anim = "seq.vm_natural_historian_seaslug_twist",
                music = "dbrow.music_fruits_de_mer",
                vantage = at(1782, 4960),
                playerFace = at(1782, 4958),
                historianAt = at(1782, 4958),
                historianFace = at(1782, 4960),
                camera = camera(at(1778, 4964), 500, at(1782, 4958), 300),
                returnTo = at(1782, 4959),
                lines = LectureText.SEASLUG,
            )

        val SNAKES =
            Lecture(
                option = "Tell me about snakes.",
                request = "Tell me about snakes.",
                intro = "Ahh snakes, the slithering squamata.",
                anim = "seq.vm_natural_historian_snake_twist",
                music = "dbrow.music_jungle_island",
                vantage = at(1782, 4960),
                playerFace = at(1782, 4960),
                historianAt = at(1782, 4962),
                historianFace = at(1782, 4960),
                camera = camera(at(1778, 4956), 500, at(1780, 4960), 300),
                returnTo = at(1782, 4961),
                lines = LectureText.SNAKE,
            )

        val TERRORBIRDS =
            Lecture(
                option = "Tell me about terrorbirds.",
                request = "Tell me about the terrorbirds.",
                intro = "Ahh terrorbirds, the fastest bird on two legs.",
                anim = "seq.vm_natural_historian_terror_bird",
                music = "dbrow.music_gnome_village",
                vantage = at(1758, 4939),
                playerFace = at(1756, 4939),
                historianAt = at(1756, 4939),
                historianFace = at(1758, 4939),
                camera = camera(at(1761, 4946), 500, at(1758, 4942), 300),
                returnTo = at(1757, 4939),
                lines = LectureText.TERROR,
            )

        val KALPHITE_QUEEN =
            Lecture(
                option = "Tell me about the Kalphite Queen.",
                request = "Tell me about the Kalphite Queen.",
                intro = "Ahh kalphites, the insectoid eating machines.",
                anim = "seq.vm_natural_historian_kalphite_queen_pincers",
                music = "dbrow.music_insect_queen",
                vantage = at(1759, 4939),
                playerFace = at(1759, 4938),
                historianAt = at(1761, 4939),
                historianFace = at(1759, 4939),
                camera = camera(at(1757, 4947), 500, at(1762, 4939), 300),
                returnTo = at(1760, 4939),
                lines = LectureText.KALPHITE,
            )

        val LIZARDS =
            Lecture(
                option = "Tell me about lizards.",
                request = "Tell me about lizards.",
                intro = "Ahh lizards, the scaly carnivores.",
                anim = "seq.vm_natural_historian_lizard_hop",
                music = "dbrow.music_desert_voyage",
                vantage = CoordGrid(1741, 4975, 0),
                playerFace = at(1741, 4977),
                historianAt = at(1741, 4977),
                historianFace = at(1741, 4975),
                camera = camera(at(1750, 4977), 505, at(1742, 4978), 300),
                returnTo = at(1741, 4976),
                lines = LectureText.LIZARD,
            )

        val TORTOISES =
            Lecture(
                option = "Tell me about tortoises.",
                request = "Tell me about the tortoises.",
                intro = "Ahh tortoises, the armoured ancients.",
                anim = "seq.vm_natural_historian_battle_tortoise",
                music = "dbrow.music_attack1",
                vantage = at(1752, 4975),
                playerFace = at(1752, 4977),
                historianAt = at(1752, 4977),
                historianFace = at(1752, 4975),
                camera = camera(at(1759, 4974), 500, at(1752, 4978), 300),
                returnTo = at(1751, 4976),
                lines = LectureText.TORTOISE,
            )

        val DRAGONS =
            Lecture(
                option = "Tell me about dragons.",
                request = "Tell me about dragons.",
                intro = "Ahh dragons, the mighty hunters of the sky.",
                anim = "seq.vm_natural_historian_dragon_flap",
                music = "dbrow.music_scape_wild",
                vantage = at(1767, 4975),
                playerFace = at(1767, 4977),
                historianAt = at(1767, 4977),
                historianFace = at(1767, 4975),
                camera = camera(at(1759, 4975), 675, at(1767, 4978), 300),
                returnTo = at(1767, 4977),
                lines = LectureText.DRAGON,
            )

        val WYVERNS =
            Lecture(
                option = "Tell me about wyverns.",
                request = "Tell me about the wyverns.",
                intro = "Ahh, wyverns. The extinct lizards.",
                anim = "seq.vm_natural_historian_wyvern",
                music = "dbrow.music_woe_of_the_wyvern",
                vantage = at(1777, 4975),
                playerFace = at(1777, 4977),
                historianAt = at(1777, 4977),
                historianFace = at(1777, 4975),
                camera = camera(at(1769, 4975), 750, at(1777, 4978), 175),
                returnTo = at(1776, 4976),
                lines = LectureText.WYVERN,
            )

        val WEST_LECTURES = listOf(CAMELS, LEECHES, MOLES, PENGUINS)
        val EAST_LECTURES = listOf(SNAILS, MONKEYS, SEA_SLUGS, SNAKES)
        val NORTH_LECTURES = listOf(LIZARDS, TORTOISES, DRAGONS, WYVERNS)
        val SOUTH_LECTURES = listOf(TERRORBIRDS, KALPHITE_QUEEN)
    }
}
