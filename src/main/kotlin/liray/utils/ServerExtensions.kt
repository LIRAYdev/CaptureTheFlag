package liray.utils

import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.entity.Player

val server: Server get() = Bukkit.getServer()

val Server.mainWorld get() = worlds[0]!!

val Player.spigot: Player.Spigot get() = player.spigot()