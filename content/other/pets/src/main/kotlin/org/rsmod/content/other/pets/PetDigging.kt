package org.rsmod.content.other.pets

import org.rsmod.api.player.hook.SpadeDigHook
import org.rsmod.api.player.protect.ProtectedAccess

internal suspend fun ProtectedAccess.petDig(hooks: Set<SpadeDigHook>) {
    val hook = hooks.firstOrNull { it.claims(player) }
    if (hook == null) {
        delay(1)
        mes("Nothing interesting happens.")
        return
    }
    if (with(hook) { beforeDig() }) {
        with(hook) { dig() }
    }
}
