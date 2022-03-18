package liray.listeners

import liray.utils.Extensions.inProtectedRegion
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent


object EventHandler : Listener {

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        event.player.sendMessage("interact")
        if(event.player.inProtectedRegion()) {
            event.player.sendMessage("Interact In Region")
        }
    }

    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        event.player.sendMessage("move")
        if(event.player.inProtectedRegion()) {
            event.player.sendMessage("Move In Region")
        }
    }

}
