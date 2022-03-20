package liray.point

import com.sk89q.worldguard.protection.regions.ProtectedRegion
import liray.plugin
import liray.utils.server
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class RegionPlayer(
    private val player: Player,
) {
    fun createPoint(index: String): Boolean {
        val region = getProtectedRegion(index) ?: return false
        if (hasPoint(region.id)) return false

        val point = Point(region.id)
        point.initializePoint()
        plugin.points[region.id] = point

        region.setFlag(plugin.protectTimeFlag, 3600)
        region.setFlag(plugin.captureTimeFlag, 420)
        region.setFlag(plugin.startCaptureMessageFlag, "Точку ${point.name} начал захватывать игрок:")
        region.setFlag(plugin.endCaptureMessageFlag, "Точку ${point.name} захватил игрок:")
        region.setFlag(plugin.rewardTimeFlag, 3600)
        region.setFlag(plugin.rewardAmountFlag, 64)
        region.setFlag(plugin.pointFlag, true)
        point.captureDispatcher = true
        return true
    }

    fun removePoint(index: String): Boolean {
        val region = getProtectedRegion(index) ?: return false
        if (!hasPoint(region.id)) return false

        val point = Point(region.id)
        plugin.points.remove(region.id)
        point.captureDispatcher = false
        point.owner = null
        point.removePoint()

        region.setFlag(plugin.protectTimeFlag, null)
        region.setFlag(plugin.captureTimeFlag, null)
        region.setFlag(plugin.startCaptureMessageFlag, null)
        region.setFlag(plugin.endCaptureMessageFlag, null)
        region.setFlag(plugin.rewardTimeFlag, null)
        region.setFlag(plugin.rewardAmountFlag, null)
        region.setFlag(plugin.pointFlag, null)
        return true
    }

    fun setOwner(owner: OfflinePlayer, index: String): Boolean {
        val point = getRegionPoint(index) ?: return false
        point.owner = Owner(point, owner.uniqueId)
        return true
    }

    fun removeOwner(index: String): Boolean {
        val point = getRegionPoint(index) ?: return false
        point.owner = null
        return true
    }

    fun setInvader(invader: OfflinePlayer, index: String): Boolean {
        val point = getRegionPoint(index) ?: return false
        point.invader = Invader(point, invader.uniqueId)
        return true
    }

    fun removeInvader(index: String): Boolean {
        val point = getRegionPoint(index) ?: return false
        point.invader = null
        return true
    }

    fun sendStatusPoint(index: String): Boolean {
        val point = getRegionPoint(index) ?: return false
        val owner = point.owner?.let { server.getOfflinePlayer(it.uuid).name } ?: "нет."
        val invader = point.invader?.let { server.getOfflinePlayer(it.uuid).name } ?: "нет."
        player.sendMessage(
            "${plugin.config.getString("infoCommandMessage1")}${point.name} \n" +
                    "${plugin.config.getString("infoCommandMessage2")}$owner \n" +
                    "${plugin.config.getString("infoCommandMessage3")}$invader \n"
        )
        return true
    }

    fun giveReward(value: Int): Boolean {
        val reward = getReward() ?: return false
        if (value < 1 || value > 64) return false
        if ((reward - value) < 0) return false
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE Rewards SET Amount=Amount-? WHERE UUID=?").use { statement ->
                statement.setLong(1, value.toLong())
                statement.setString(2, player.uniqueId.toString())
                statement.execute()
            }
        }
        repeat(value) {
            player.location.world.dropItem(
                player.location,
                plugin.serialize.fromBase64(plugin.config.getString("reward"))
            )
        }
        return true
    }

    fun getReward(): Int? {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT * FROM Rewards WHERE UUID=?").use { statement ->
                statement.setString(1, player.uniqueId.toString())
                return if (statement.executeQuery().next()) {
                    statement.executeQuery().getLong("Amount").toInt()
                } else {
                    null
                }
            }
        }
    }

    fun getProtectedRegion(index: String): ProtectedRegion? =
        plugin.worldGuard.regionContainer[player.location.world]!!.getRegion(index)

    fun getRegionPoint(index: String): Point? = plugin.points[index]

    fun getProtectedRegionApplicablePlayer(): ProtectedRegion? {
        val applicableRegions = plugin.worldGuard.regionContainer[player.location.world]!!
            .getApplicableRegions(player.location)
        if (applicableRegions.regions.isEmpty()) return null
        return applicableRegions.first()
    }

    fun hasPoint(region: String): Boolean {
        if (plugin.points.contains(region)) return true
        return false
    }
}