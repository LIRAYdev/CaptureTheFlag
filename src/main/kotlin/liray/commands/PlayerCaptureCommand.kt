package liray.commands

import liray.plugin
import liray.point.RegionPlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object PlayerCaptureCommand: CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return true
        val regionPlayer = RegionPlayer(sender)
        if (args[0] == "info") {
            val index = args[1]
            if (regionPlayer.sendStatusPoint(index)) return true
            sender.sendMessage("§cРегиона не существует!")
            return false
        }

        if (args[0] == "reward") {
            if (args[1] == "get") {
                val value = args[2].toInt()
                if(regionPlayer.giveReward(value)) {
                    sender.sendMessage(plugin.config.getString("giveRewardMessage"))
                    return true
                }
                sender.sendMessage(plugin.config.getString("notGiveRewardMessage"))
                return false
            }
            if (args[1] == "info") {
                val amount = let { regionPlayer.getReward() } ?: 0
                sender.sendMessage("${plugin.config.getString("infoRewardMessage")} $amount")
                return true
            }
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
        if (args[0] == "info") {
            return plugin.points
                .map { it.key }
                .toMutableList()
        }

        if (args[0] == "reward") {
            if(args[1] == "get") {
                return mutableListOf("1", "16", "32", "64")
            }
            return mutableListOf("get", "info")
        }

        return mutableListOf("info", "reward")
    }

    private fun error(sender: Player) {
        sender.sendMessage("§cОшибка в написании команды!")
    }

}