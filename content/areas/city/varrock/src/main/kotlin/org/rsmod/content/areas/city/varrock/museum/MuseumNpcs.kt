package org.rsmod.content.areas.city.varrock.museum

import dev.openrune.types.MesAnimType
import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.random.GameRandom
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.script.onOpNpc3
import org.rsmod.content.quest.manager.QuestRequirements
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class MuseumNpcs @Inject constructor(private val random: GameRandom) : PluginScript() {
    override fun ScriptContext.startup() {
        onOpNpc1("npc.curator") { startDialogue(it.npc) { curator() } }
        onOpNpc1("npc.vm_museum_guard3") { startDialogue(it.npc) { backDoorGuard() } }
        for (archaeologist in ARCHAEOLOGISTS) {
            onOpNpc1(archaeologist) { startDialogue(it.npc) { archaeologist() } }
        }
        onOpNpc1("npc.vm_collector") { startDialogue(it.npc) { collector() } }
        onOpNpc3("npc.vm_collector") { startDialogue(it.npc) { collectorGetLog() } }
        for (child in SCHOOL_CHILDREN) {
            onOpNpc1(child) { startDialogue(it.npc) { chatNpc(happy, random.pick(CHILD_LINES)) } }
        }
        onOpNpc1("npc.vm_teacher_girl_multi") { startDialogue(it.npc) { teacherAndGirl() } }
        onOpNpc1("npc.vm_school_teacher_male_and_boy") {
            startDialogue(it.npc) { teacherAndBoy() }
        }
        onOpNpc1("npc.vm_advent_01") { startDialogue(it.npc) { visitor() } }
        onOpNpc1("npc.vm_advent_02") { startDialogue(it.npc) { visitor() } }
        onOpNpc1("npc.vm_timeline_historian") { startDialogue(it.npc) { historianMinas() } }
        onOpNpc1("npc.vm_art_critic") { startDialogue(it.npc) { artCritic() } }
    }

    private val Dialogue.shock: MesAnimType
        get() = mesanim("mesanim.shock")

    private suspend fun Dialogue.curator() {
        chatNpc(neutral, "Welcome to the museum of Varrock.")
        val option =
            choice4(
                "Talk about Fossil Island.",
                1,
                "Have you any interesting news?",
                2,
                "Do you know where I could find any treasure?",
                3,
                "How do I get a medallion?",
                4,
            )
        when (option) {
            1 -> {
                chatPlayer(happy, "I've got some questions regarding Fossil Island.")
                chatNpc(happy, "Ah, how can I help?")
                val fossils = "What can I do with fossils I find on Fossil Island?"
                if (choice2(fossils, true, "Nevermind.", false)) {
                    chatPlayer(quiz, fossils)
                    chatNpc(happy, "I'm not entirely sure just yet.")
                } else {
                    chatPlayer(neutral, "Nevermind.")
                }
            }
            2 -> {
                chatPlayer(neutral, "Have you any interesting news?")
                chatNpc(happy, "We've seen some incredible specimens come back from Fossil Island!")
                chatNpc(
                    happy,
                    "Be sure to bring anything you find back for examination - we've got a brand " +
                        "new Palaeontology room downstairs dedicated to finds from the island.",
                )
            }
            3 -> {
                chatPlayer(quiz, "Do you know where I could find any treasure?")
                chatNpc(neutral, "Look around you! This museum is full of treasures!")
                chatPlayer(angry, "No, I meant treasures for ME.")
                chatNpc(
                    neutral,
                    "Any treasures this museum knows about it goes to great lengths to acquire.",
                )
            }
            4 -> curatorMedallion()
        }
    }

    private suspend fun Dialogue.curatorMedallion() {
        val inv = player.inv
        val pendant = DIGSITE_PENDANTS.firstOrNull { it in inv }
        val hasAll =
            pendant != null &&
                access.invTotal(inv, "obj.ruby") >= MEDALLION_RUBIES &&
                access.invTotal(inv, "obj.cosmicrune") >= MEDALLION_COSMICS &&
                access.invTotal(inv, "obj.firerune") >= MEDALLION_FIRES
        if (!hasAll || pendant == null) {
            chatNpc(
                sad,
                "Oh dear. You'll need 250 rubies, a Digsite pendant, 100 cosmic runes and 500 " +
                    "fire runes with you I'm afraid.",
            )
            return
        }
        chatNpc(happy, "Why, I can give you one now if you wish?")
        val exchange =
            choice2(
                "Yes, exchange my runes, rubies and pendant for a medallion.",
                true,
                "No, keep my stuff.",
                false,
            )
        if (!exchange) {
            return
        }
        access.invDel(inv, "obj.ruby", count = MEDALLION_RUBIES)
        access.invDel(inv, "obj.cosmicrune", count = MEDALLION_COSMICS)
        access.invDel(inv, "obj.firerune", count = MEDALLION_FIRES)
        access.invDel(inv, pendant)
        access.invAdd(inv, "obj.poh_curator_medallion")
        objbox("obj.poh_curator_medallion", "The curator hands you a medallion.")
    }

    private suspend fun Dialogue.backDoorGuard() {
        chatNpc(happy, "Hello there. Come to see the new museum?")
        chatPlayer(happy, "Yes, how do I get in?")
        chatNpc(
            happy,
            "Well, the main entrance is 'round the front. Just head west then north slightly, " +
                "you can't miss it!",
        )
        chatPlayer(quiz, "What about these doors?")
        if (!QuestRequirements.hasCompleted(player, DIG_SITE)) {
            chatNpc(
                happy,
                "They're for the workmen bringing finds from the Dig Site; sorry, but you " +
                    "can't go through.",
            )
            return
        }
        chatNpc(
            happy,
            "They're primarily for the workmen bringing finds from the Dig Site, but you can go " +
                "through if you want.",
        )
        chatPlayer(happy, "Okay, thanks.")
    }

    private suspend fun Dialogue.archaeologist() {
        when (random.of(0, 2)) {
            0 -> chatNpc(happy, "Hello there; I see you're qualified. Come to help us out?")
            1 ->
                chatNpc(
                    happy,
                    "Hello! You're that student who recently finished all three Earth " +
                        "Sciences exams, aren't you? Come to help us out?",
                )
            else -> chatNpc(happy, "Greetings! Have you come to give us a hand?")
        }
        val option =
            choice3(
                "Yes, how can I help out?",
                1,
                "I found something interesting.",
                2,
                "No thanks.",
                3,
            )
        when (option) {
            1 -> {
                chatPlayer(happy, "Yes, how can I help out?")
                if (hasCleaningKit()) {
                    chatNpc(happy, "I see you have your equipment - well done.")
                    chatNpc(
                        happy,
                        "To really give us a hand, you could clean some finds for us. They're " +
                            "mostly done - just some fine work to finish off. Take a find from " +
                            "the pile that the workmen are bringing in, then there are spare " +
                            "tables over there",
                    )
                    chatNpc(
                        happy,
                        "to clean them on. If you find anything interesting, let us know and " +
                            "we'll see if it's good enough to go on display.",
                    )
                    return
                }
                chatNpc(
                    sad,
                    "Well, you'll need to get your equipment first - it's all there on the tool " +
                        "rack. Use what you learned from your Earth Sciences exams. You'll need " +
                        "to be wearing your leather gloves and boots as well as have access to",
                )
                chatNpc(sad, "your trowel, rock pick and specimen brush.")
            }
            2 -> {
                chatPlayer(happy, "I found something interesting.")
                chatNpc(happy, "Oh? Let's take a look...")
                chatPlayer(confused, "Err... I seem to have lost it. Sorry.")
                chatNpc(laugh, "Sounds like you never had it!")
            }
            3 -> chatPlayer(neutral, "No, just here to watch.")
        }
    }

    private fun Dialogue.hasCleaningKit(): Boolean {
        val worn = player.worn
        val wearing = "obj.leather_gloves" in worn && "obj.leather_boots" in worn
        return wearing && CLEANING_TOOLS.all { it in player.inv }
    }

    private suspend fun Dialogue.collector() {
        chatNpc(happy, "It's beautiful, isn't it?")
        chatPlayer(quiz, "What is?")
        chatNpc(
            happy,
            "Everything! The wonders right here in this museum, collected from all corners of " +
                "the land.",
        )
        chatPlayer(neutral, "I guess it is, you're right.")
        if (hasCollectionLog()) {
            return
        }
        chatNpc(
            neutral,
            "Matter of fact, I consider myself quite the collector. I keep a record of just " +
                "about everything that I find!",
        )
        chatNpc(quiz, "Would you like one? You could keep track of your own collections.")
        if (!choice2("Yes.", true, "No thanks.", false)) {
            chatPlayer(neutral, "No thanks. I think I'll pass.")
            chatNpc(
                sad,
                "I should've guessed you wouldn't understand the true beauty of a good " +
                    "collection!",
            )
            return
        }
        chatPlayer(happy, "Yes, that sounds helpful!")
        giveCollectionLog()
    }

    private suspend fun Dialogue.collectorGetLog() {
        if (hasCollectionLog()) {
            chatNpc(quiz, "Don't you have one lying around somewhere already?")
            chatPlayer(happy, "I think you're right!")
            return
        }
        giveCollectionLog()
    }

    private suspend fun Dialogue.giveCollectionLog() {
        if (player.inv.isFull()) {
            chatNpc(neutral, "You might struggle to carry that right now.")
            chatPlayer(neutral, "Oh... I'll come back when I'm carrying a little less.")
            return
        }
        access.invAdd(player.inv, COLLECTION_LOG)
        objbox(COLLECTION_LOG, "The collector gives you a collection log.")
        chatNpc(
            happy,
            "There! Now you'll be able to see the true beauty of everything you collect on " +
                "your adventures.",
        )
        chatPlayer(happy, "Thanks!")
    }

    private fun Dialogue.hasCollectionLog(): Boolean =
        COLLECTION_LOG in player.inv || COLLECTION_LOG in player.invMap.getOrPut("inv.bank")

    private suspend fun Dialogue.teacherAndGirl() {
        val coords = player.coords
        when {
            coords.z > BASEMENT_MIN_Z -> {
                chatTeacher(happy, "What do you want to see now, dear?")
                chatGirl(happy, "The penguins, the penguins!")
            }
            coords.level == 2 -> {
                chatGirl(quiz, "That man over there talks funny, miss.")
                chatTeacher(
                    happy,
                    "That's because he's an art critic, dear. They have some very funny ideas.",
                )
            }
            else -> {
                chatTeacher(angry, "Stop pulling, we've plenty of time to see everything.")
                chatGirl(happy, "Aww, but miss, it's sooo exciting.")
            }
        }
    }

    private suspend fun Dialogue.chatTeacher(mesanim: MesAnimType, text: String) =
        chatNpcSpecific("Teacher", "npc.vm_female_school_teacher_dummy", mesanim, text)

    private suspend fun Dialogue.chatGirl(mesanim: MesAnimType, text: String) =
        chatNpcSpecific("Pupil", "npc.vm_female_school_teacher_girl_dummy", mesanim, text)

    private suspend fun Dialogue.teacherAndBoy() {
        chatBoy(shock, "Teacher! Sir! I need the toilet!")
        chatSir(bored, "I told you to go before we got here.")
        chatBoy(shock, "But sir, I didn't need to go then!")
        chatSir(bored, "Alright, come on then.")
    }

    private suspend fun Dialogue.chatSir(mesanim: MesAnimType, text: String) =
        chatNpcSpecific("Teacher", "npc.vm_male_school_teacher_chathead_dummy", mesanim, text)

    private suspend fun Dialogue.chatBoy(mesanim: MesAnimType, text: String) =
        chatNpcSpecific("Pupil", "npc.vm_male_school_teacher_boy_chathead_dummy", mesanim, text)

    private suspend fun Dialogue.visitor() {
        when (random.of(0, 4)) {
            0 -> visitorQuesting()
            1 -> visitorDigSite()
            2 -> visitorBobTheCat()
            3 -> visitorUpdatedDisplay()
            else -> visitorEngrossed()
        }
    }

    private suspend fun Dialogue.visitorQuesting() {
        chatPlayer(happy, "Hello.")
        chatNpc(neutral, "Hi there. So much to see here!")
        chatPlayer(laugh, "Yep, could be looking all day!")
        chatPlayer(quiz, "Updated any displays yet?")
        chatNpc(
            happy,
            "Yes, I just learned more about some priestly warriors and a staff of the gods.",
        )
        chatPlayer(quiz, "Sounds fun, how did you do that?")
        chatNpc(
            neutral,
            "Ahh, I'm big on quests me, love questing. So when I've done one, I check here " +
                "with 'Historian' Minas to see if I can help the Museum out with some " +
                "information. Sometimes they already know it, or don't want to know,",
        )
        chatNpc(neutral, "and sometimes it earns me a reward or two.")
        chatPlayer(happy, "That sounds really good. Perhaps I should do the same!")
        chatNpc(
            neutral,
            "You should, as well as everything else there is here in the Museum. Did you know " +
                "they have a really fun-looking display down in the basement? I love monkeys.",
        )
        chatPlayer(happy, "I'll check it out. Got to get going now, nice talking to you.")
        chatNpc(happy, "Have a good day!")
    }

    private suspend fun Dialogue.visitorDigSite() {
        chatNpc(happy, "Hello!")
        chatPlayer(neutral, "Hi, have you seen the Dig Site displays?")
        chatNpc(happy, "Yes, good aren't they! Have you done the quest?")
        if (!QuestRequirements.hasCompleted(player, DIG_SITE)) {
            chatPlayer(neutral, "Not yet.")
            chatNpc(
                happy,
                "Oh you should, it lets you get into that bit where they clean the finds, and " +
                    "you can join in!",
            )
            chatPlayer(happy, "Sounds fun, I should probably check it out.")
            return
        }
        chatPlayer(happy, "Yes, I did it all.")
        chatNpc(
            neutral,
            "Did you know after the quest you can get the teddy bear back by stealing it from " +
                "the female student?",
        )
        chatPlayer(happy, "I shall have to try that sometime.")
    }

    private suspend fun Dialogue.visitorBobTheCat() {
        chatNpc(happy, "Hi there!")
        chatPlayer(quiz, "Hello, what are you doing here?")
        chatNpc(happy, "Oh, just taking a look at these displays. I'm a bit of a history buff.")
        chatPlayer(quiz, "Really? Tell me something I didn't know!")
        chatNpc(
            neutral,
            "Well, did you know that Bob the Cat is... no, no... you'd never believe me anyway.",
        )
        chatPlayer(quiz, "He's what? Tell me!")
        chatNpc(neutral, "No, no. Couldn't possibly ruin the surprise.")
        chatPlayer(angry, "ARG! Tell me!")
        chatNpc(
            neutral,
            "If you really want to know, go talk to Unferth in his house in Burthorpe.",
        )
    }

    private suspend fun Dialogue.visitorUpdatedDisplay() {
        chatPlayer(happy, "Hi!")
        chatNpc(neutral, "Oh, hello. I was just looking at this display here.")
        chatPlayer(quiz, "Anything interesting?")
        chatNpc(
            happy,
            "Yes, one I just updated by talking to 'Historian' Minas. I'm a bit of a quester " +
                "you see.",
        )
        chatPlayer(neutral, "Okay, I'll let you get on then.")
        chatNpc(
            neutral,
            "Hmm, yes. Okay. Don't forget to check out the Natural History exhibit - you could " +
                "learn a lot.",
        )
        chatPlayer(happy, "Right, bye!")
    }

    private suspend fun Dialogue.visitorEngrossed() {
        chatPlayer(happy, "Hi!")
        chatNpc(confused, "Hmm... 3rd Age.... No, no...maybe 4th...")
        chatPlayer(quiz, "Hello?")
        chatNpc(
            confused,
            "Yes, that's it, just like that. Mustn't forget the map either....hmm...what else?",
        )
        chatPlayer(angry, "HELLO! Are you there?")
        chatNpc(
            neutral,
            "Oh, hello there. Sorry, was a bit engrosed in updating this display. I've just been " +
                "out questing and thought I'd stop off here you see. It's quite rewarding too.",
        )
        chatPlayer(angry, "Yes, well, no need to ignore me.")
        chatNpc(
            neutral,
            "No offence intended, my dear chap. Just very interested in this history stuff, " +
                "that's all.",
        )
        chatPlayer(quiz, "Okay. Have you seen the Natural History exhibit in the basement?")
        chatNpc(
            happy,
            "Oh, yes, marvellous isn't it! I particularly like the wyvern, very interesting " +
                "considering they've never found a live specimen. And to think they could " +
                "possibly have been flapping around at one time!",
        )
        chatPlayer(
            quiz,
            "A bit like the crazy gnomes and their gliders. Have you seen the model they have " +
                "upstairs?",
        )
        chatNpc(
            neutral,
            "Yes, very intricate isn't it? Worrying to think some people risk their lives in " +
                "them! You wouldn't catch me up in one. Had a chat with Jacques while I was up " +
                "there, the art critic...he's a bit funny in the head.",
        )
        chatPlayer(angry, "That's not very nice!")
        chatNpc(neutral, "Well, he thinks he's going to get an entire portrait gallery to himself!")
        chatPlayer(
            neutral,
            "Who knows, the Museum just got really big and appears to have cash. They might " +
                "entertain him with his very own floor - we'll see.",
        )
        chatNpc(
            happy,
            "I guess so. Wonder what poetry he'll come up with then! Still, I must get on with " +
                "this display. See you around I hope!",
        )
        chatPlayer(happy, "Bye!")
    }

    private suspend fun Dialogue.historianMinas() {
        chatPlayer(neutral, "Hello!")
        chatNpc(happy, "Hello there, welcome to Varrock Museum! Can I help you?")
        val option =
            choice3(
                "Tell me about the Museum.",
                1,
                "I have some information that might be of use in your displays.",
                2,
                "No thanks.",
                3,
            )
        when (option) {
            1 -> minasMuseum()
            2 -> minasInformation()
            3 -> chatPlayer(neutral, "No thanks.")
        }
    }

    private suspend fun Dialogue.minasMuseum() {
        chatPlayer(quiz, "Tell me about the Museum.")
        chatNpc(
            happy,
            "Well, as you can see we have recently expanded a great deal to cope with the " +
                "influx of finds from the Dig Site. Also, of course, to prepare for the new dig " +
                "we're opening soon.",
        )
        val option =
            choice3(
                "Tell me about the new dig.",
                1,
                "I have some information which might be of use in your displays.",
                2,
                "Tell me about other sections of the Museum.",
                3,
            )
        when (option) {
            1 -> {
                chatPlayer(quiz, "Tell me about the new dig.")
                chatNpc(
                    happy,
                    "Well, we recently had a large chunk of funds donated to us, so we sent out " +
                        "all sorts of explorers - I'm sure you'll meet them as you go around " +
                        "the world. People like Simon down in the desert near the pyramid as " +
                        "well",
                )
                chatNpc(
                    happy,
                    "as Aristarchus in Pollnivneach, Varmen and, of course, Anna Jones. " +
                        "Anyway, we think we have discovered an island that has massive " +
                        "archaeological significance. We can't say much beyond that at this " +
                        "point, as we haven't",
                )
                chatNpc(
                    happy,
                    "researched it. If you have the qualifications and help us out around the " +
                        "museum, I'm sure we could offer you a place on the dig when we're " +
                        "ready to go.",
                )
                chatPlayer(happy, "That would be very nice.")
            }
            2 -> minasInformation()
            3 -> minasSections()
        }
    }

    private suspend fun Dialogue.minasInformation() {
        chatPlayer(happy, "I have some information that might be of use in your displays.")
        chatNpc(
            happy,
            "That's grand! We're always glad to have more facts. Now, tell me what you know...",
        )
    }

    private suspend fun Dialogue.minasSections() {
        chatPlayer(quiz, "Tell me about other sections of the Museum.")
        chatNpc(happy, "I'd be delighted to; which areas are you interested in?")
        while (true) {
            val option =
                choice4(
                    "Natural History exhibit",
                    1,
                    "Dig Site exhibit",
                    2,
                    "Dig Site find cleaning area",
                    3,
                    "Timeline exhibit",
                    4,
                )
            when (option) {
                1 -> {
                    chatPlayer(quiz, "Tell me about the Natural History exhibit.")
                    chatNpc(
                        happy,
                        "Ahh, our newest expansion. The Natural History exhibit in the basement " +
                            "was set up to inform our citizens of the weird and wonderful " +
                            "diversity in our environment today. Indeed, I am sure you will " +
                            "recognise some of",
                    )
                    chatNpc(
                        happy,
                        "the creatures down there. Hopefully, our lectures will teach you some " +
                            "things you didn't know about them!",
                    )
                    chatPlayer(quiz, "So where does the 'history' bit come in?")
                    chatNpc(
                        happy,
                        "Ahh, I see. The 'history' refers to that of the species - it doesn't " +
                            "necessarily mean it's died out. After all, we have human history " +
                            "and we're still here!",
                    )
                    chatPlayer(neutral, "I guess that makes sense.")
                }
                2 -> {
                    chatPlayer(quiz, "Tell me about the Dig Site exhibit.")
                    chatNpc(
                        happy,
                        "Ahh, those. We're slowly filling them up with the help of people who " +
                            "have their Earth Sciences exams completed at the Dig Site. If you " +
                            "have, feel free to give us a hand. We'd appreciate it.",
                    )
                }
                3 -> {
                    chatPlayer(quiz, "Tell me about the Dig Site find cleaning area.")
                    chatNpc(
                        happy,
                        "The cleaning area, yes. Well, we have some really good archaeologists " +
                            "on site right now. They're cleaning up the finds that are shipped " +
                            "over from the Dig Site to the east of here. If you have the right " +
                            "qualifications, you can help them clean the finds.",
                    )
                    chatPlayer(quiz, "What qualifications?")
                    chatNpc(
                        happy,
                        "Level 3 Earth Science exams. You can take them over at the Exam " +
                            "Centre on the Dig Site.",
                    )
                    chatPlayer(happy, "Oh, I already have those!")
                    chatNpc(
                        happy,
                        "That's excellent. If you feel like helping us out, pop downstairs and " +
                            "clean up some finds for us.",
                    )
                }
                4 -> {
                    chatPlayer(quiz, "Tell me about the Timeline exhibit.")
                    chatNpc(
                        happy,
                        "Ahh, my speciality. Being a historian, matters of the past are my " +
                            "passion. Collated on this floor and the one above are just part of " +
                            "my life's work. I am still working on updating and finding out " +
                            "more everyday, so if you find",
                    )
                    chatNpc(
                        happy,
                        "any information on your travels that may be useful, please do let me " +
                            "know.",
                    )
                    chatNpc(
                        happy,
                        "You can follow the timeline of the history of Gielinor from the " +
                            "creation of the world to the present day, simply by walking around " +
                            "this display. A wondrous journey, I think you'll agree!",
                    )
                    chatPlayer(
                        happy,
                        "Wow, that's a lot of information. I'm sure in my travels and quests " +
                            "I'll pick up a few bits of information for you.",
                    )
                }
            }
        }
    }

    private suspend fun Dialogue.artCritic() {
        chatPlayer(neutral, "Hello.")
        chatNpc(happy, "Ah, many greetings, and welcome to the Museum!")
        if (choice2("Who are you?", true, "What do you do here?", false)) {
            chatPlayer(quiz, "Who are you?")
            chatNpc(happy, "My name is Jacques and I am the Museum's finest art critic!")
            chatPlayer(quiz, "Looks to me like you're the only one.")
            chatNpc(
                happy,
                "This is true. However, in the future, I will have my own entire floor of the " +
                    "Museum with paintings galore!",
            )
            chatPlayer(quiz, "Er... has that been approved, or are you just dreaming?")
            chatNpc(neutral, "Ah, I see you're being witty, eh?")
            chatPlayer(confused, "I am?")
            chatNpc(
                happy,
                "Yes! You're poking fun at me! I shall have my gallery. You will see " +
                    "eventually. Then I shall be the one with the last laugh!",
            )
            return
        }
        chatPlayer(quiz, "What do you do here?")
        chatNpc(
            laugh,
            "I critique the art! See the beautiful colours and the masterful use of the brush " +
                "to capture perfectly the royal presence...",
        )
        if (choice2("Er... it's a painting.", true, "Yes, I see!", false)) {
            chatPlayer(neutral, "Er... it's a painting.")
            chatNpc(
                angry,
                "Just a painting? How could you say such a thing? When you learn how to " +
                    "appreciate the wonderful works of art, you may talk to me.",
            )
            return
        }
        chatPlayer(happy, "Yes, I see!")
        chatPlayer(
            happy,
            "And look at the use of shade and contrast - isn't it just magnificent?",
        )
        chatNpc(
            sad,
            "So it is. So perfect, it almost makes me weep with joy! The grandiloquent strokes " +
                "of the brush convey a kind of mystic quality... It is almost as if he were " +
                "here with us.",
        )
        chatNpc(
            neutral,
            "No words can truly describe my feelings, so let us continue to admire this work in " +
                "silence.",
        )
    }

    private companion object {
        const val DIG_SITE = "quest_digsite"
        const val COLLECTION_LOG = "obj.collection_log"
        const val BASEMENT_MIN_Z = 4900
        const val MEDALLION_RUBIES = 250
        const val MEDALLION_COSMICS = 100
        const val MEDALLION_FIRES = 500

        val ARCHAEOLOGISTS = (1..6).map { "npc.vm_archaeologist${it}_npc_dummy" }

        val SCHOOL_CHILDREN =
            listOf(
                "npc.vm_school_boy_blonde",
                "npc.vm_school_boy_brunnete",
                "npc.vm_school_girl_blonde",
                "npc.vm_school_girl_brunette",
            )

        val CHILD_LINES =
            listOf(
                "*sniff* They won't let me take an arrowhead as a souvenir.",
                "I wonder what they're doing behind that rope.",
                "Can you find my teacher? I need the toilet!",
                "*cough* It's so dusty in here.",
                "Teacher! Can we go to the Natural History exhibit now?",
                "Maz...Zar...Za-mor-ak is bestest!",
                "I wanna be an archaeologist when I grow up!",
                "Yaaay! A day off school.",
                "Sada...Sram...Sa-ra-do-min is bestest!",
                "The monkeys are so funny-looking!",
                "My dad says he got bitten by a giant snail once.",
            )

        val DIGSITE_PENDANTS = (1..5).map { "obj.necklace_of_digsite_$it" }

        val CLEANING_TOOLS = listOf("obj.trowel", "obj.rockpick", "obj.specimen_brush")
    }
}
