package liray.commands

import liray.plugin
import liray.point.RegionPlayer
import liray.utils.mainWorld
import liray.utils.server
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object AdminCaptureCommand : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return true
        if (!sender.isOp) return true
        val regionPlayer = RegionPlayer(sender)

        if (args[0] == "create") {
            val index = args[1]
            if (regionPlayer.createPoint(index)) {
                sender.sendMessage("§6Флаг в регионе (${regionPlayer.getProtectedRegion(index)!!.id.uppercase()}) успешно создан!")
                return true
            }
            sender.sendMessage("§cВы не находитесь в регионе флага или он уже создан!")
            return false
        }

        if (args[0] == "delete") {
            val index = args[1]
            if (regionPlayer.removePoint(index)) {
                sender.sendMessage("§6Флаг в регионе (${regionPlayer.getProtectedRegion(index)!!.id.uppercase()}) успешно удалён!")
                return true
            }
            sender.sendMessage("§cВы не находитесь в регионе флага или его не существует!")
            return false
        }

        if (args[0] == "get") {
            val index = args[1]
            if (regionPlayer.sendStatusPoint(index)) return true
            sender.sendMessage("§cРегиона не существует!")
            return false
        }

        if (args[0] == "set") {
            val index = args[1]
            if (args[2] == "owner") {
                val owner = Bukkit.getPlayer(args[3]) ?: return false.also { sender.sendMessage("§сИгрок не найден!") }
                if (regionPlayer.setOwner(owner, index)) {
                    sender.sendMessage(
                        "§6Игрок ${owner.name} успешно добавлен в (${
                            regionPlayer.getProtectedRegion(
                                index
                            )!!.id.uppercase()
                        }) как владелец!"
                    )
                    return true
                }
            }
            if (args[2] == "invader") {
                val invader =
                    Bukkit.getPlayer(args[3]) ?: return false.also { sender.sendMessage("§сИгрок не найден!") }
                if (regionPlayer.setInvader(invader, index)) {
                    sender.sendMessage(
                        "§6Игрок ${invader.name} успешно добавлен в (${
                            regionPlayer.getProtectedRegion(
                                index
                            )!!.id.uppercase()
                        }) как захватчик!"
                    )
                    return true
                }
            }
            sender.sendMessage("§cВы не находитесь в регионе флага или его не существует!")
            return false
        }

        if (args[0] == "remove") {
            val index = args[1]
            if (args[2] == "owner") {
                if (regionPlayer.removeOwner(index)) {
                    sender.sendMessage(
                        "§6Владелец успешно удалён из региона флага (${
                            regionPlayer.getProtectedRegion(
                                index
                            )!!.id.uppercase()
                        })!"
                    )
                }
                return true
            }
            if (args[2] == "invader") {
                if (regionPlayer.removeInvader(index)) {
                    sender.sendMessage(
                        "§6Захватчик успешно удалён из региона флага (${
                            regionPlayer.getProtectedRegion(
                                index
                            )!!.id.uppercase()
                        })!"
                    )
                }
                return true
            }
            sender.sendMessage("§cВы не находитесь в регионе флага или его не существует!")
            return false
        }

        error(sender)
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if (args[0] == "create") {
            return plugin.worldGuard.regionContainer[server.mainWorld]!!.regions
                .map { it.value.id }
                .subtract(plugin.points.keys)
                .toMutableList()
        }
        if (args[0] == "delete") {
            return plugin.points
                .map { it.key }
                .toMutableList()
        }
        if (args[0] == "get") {
            return plugin.points
                .map { it.key }
                .toMutableList()
        }
        if (args[0] == "set" || args[0] == "remove") {
            if (plugin.points.keys.contains(args[1])) {
                if (args[2] == "owner" || args[2] == "invader") {
                    return server.onlinePlayers
                        .map { it.name }
                        .toMutableList()
                }
                return mutableListOf("owner", "invader")
            }
            return plugin.points
                .map { it.key }
                .toMutableList()
        }
        return mutableListOf("create", "delete", "remove", "get", "set")
    }

    private fun error(sender: Player) {
        sender.sendMessage("§cОшибка в написании команды!")
    }

}