package org.rsmod.content.areas.city.varrock.museum

import org.rsmod.api.player.ui.ifCloseOverlay
import org.rsmod.api.player.ui.ifOpenOverlay
import org.rsmod.events.EventBus
import org.rsmod.game.entity.Player

internal fun Player.openKudos(eventBus: EventBus) {
    ifOpenOverlay(KUDOS_OVERLAY, "component.toplevel_osrs_stretch:overlay_hud", eventBus)
}

internal fun Player.closeKudos(eventBus: EventBus) {
    ifCloseOverlay(KUDOS_OVERLAY, eventBus)
}

private const val KUDOS_OVERLAY = "interface.vm_kudos"
