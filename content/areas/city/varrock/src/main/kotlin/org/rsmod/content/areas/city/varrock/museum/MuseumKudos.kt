package org.rsmod.content.areas.city.varrock.museum

import jakarta.inject.Inject
import org.rsmod.api.player.ui.ifCloseOverlay
import org.rsmod.api.player.ui.ifOpenOverlay
import org.rsmod.api.script.onArea
import org.rsmod.api.script.onAreaExit
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class MuseumKudos @Inject constructor(private val eventBus: EventBus) : PluginScript() {
    override fun ScriptContext.startup() {
        onArea(MUSEUM_AREA) { player.openKudos(eventBus) }
        onAreaExit(MUSEUM_AREA) { player.closeKudos(eventBus) }
    }
}

internal fun Player.openKudos(eventBus: EventBus) {
    ifOpenOverlay(KUDOS_OVERLAY, "component.toplevel_osrs_stretch:overlay_hud", eventBus)
}

internal fun Player.closeKudos(eventBus: EventBus) {
    ifCloseOverlay(KUDOS_OVERLAY, eventBus)
}

private const val MUSEUM_AREA = "area.varrock_museum"
private const val KUDOS_OVERLAY = "interface.vm_kudos"
