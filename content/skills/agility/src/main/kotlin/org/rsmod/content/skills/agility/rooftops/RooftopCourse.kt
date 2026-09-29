package org.rsmod.content.skills.agility.rooftops

import org.rsmod.map.CoordGrid

/**
 * A rooftop course. [helperCourse] is the value the client's obstacle-highlight helper expects while
 * a lap is in progress, and [markTiles] are the tiles a Mark of Grace can appear on: roofs the lap
 * crosses on foot, excluding the first and the last.
 */
enum class RooftopCourse(
    val displayName: String,
    val level: Int,
    val helperCourse: Int,
    val markChance: Int,
    val markTiles: List<CoordGrid>,
) {
    Falador(
        displayName = "Falador Rooftop",
        level = 50,
        helperCourse = 12,
        markChance = 3,
        markTiles =
            listOf(
                CoordGrid(3047, 3346, 3),
                CoordGrid(3049, 3348, 3),
                CoordGrid(3050, 3351, 3),
                CoordGrid(3048, 3358, 3),
                CoordGrid(3048, 3361, 3),
                CoordGrid(3046, 3361, 3),
                CoordGrid(3045, 3361, 3),
                CoordGrid(3041, 3361, 3),
                CoordGrid(3039, 3361, 3),
                CoordGrid(3037, 3361, 3),
                CoordGrid(3035, 3361, 3),
                CoordGrid(3028, 3355, 3),
                CoordGrid(3027, 3353, 3),
                CoordGrid(3021, 3353, 3),
                CoordGrid(3020, 3353, 3),
                CoordGrid(3018, 3353, 3),
                CoordGrid(3018, 3349, 3),
                CoordGrid(3017, 3347, 3),
                CoordGrid(3016, 3346, 3),
                CoordGrid(3014, 3346, 3),
                CoordGrid(3013, 3344, 3),
                CoordGrid(3013, 3342, 3),
                CoordGrid(3013, 3340, 3),
                CoordGrid(3013, 3338, 3),
                CoordGrid(3013, 3336, 3),
            ),
    )
}
