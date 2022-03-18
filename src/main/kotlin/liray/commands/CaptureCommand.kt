package liray.commands

import liray.utils.Extensions
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object CaptureCommand : CommandExecutor, TabCompleter {

    private val extensions: Extensions = Extensions

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (sender !is Player) return true
        if (!sender.hasPermission("CTF.use") || !sender.isOp) return true
        if (args[0] == "get") {
            val player = Bukkit.getPlayer(args[1]) ?: return true

            return true
        }
        error(sender)
        return false
    }

    private fun error(sender: Player) {
        sender.sendMessage("§cОшибка в написании команды!")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        if (args[0] == "get") {
            return Bukkit.getOnlinePlayers().map { it.displayName }.toMutableList()
        }
        return mutableListOf("get")
    }

}