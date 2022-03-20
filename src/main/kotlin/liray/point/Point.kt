package liray.point

import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.*
import liray.plugin
import liray.utils.mainWorld
import liray.utils.server
import org.bukkit.entity.Player
import java.sql.Date
import java.util.*
import kotlin.math.floor


class Point(val name: String) {

    private val protectScope = CoroutineScope(plugin.minecraftDispatcher)
    private val dispatcherScope = CoroutineScope(plugin.minecraftDispatcher)

    var protectDate: java.util.Date? = null
    var isInit: Boolean = false
    var isLoad: Boolean = false

    var captureDispatcher: Boolean = false
        set(value) {
            if (field && value) return
            field = value
            if (value) {
                dispatcherScope.launch {
                    while (isActive) {
                        server.onlinePlayers.forEach {
                            if (!captureProtectDispatcher) {
                                updateOwner(it)
                            } else {
                                sendRegionProtectStatus(it)
                            }
                        }
                        delay(1000)
                    }
                }
            } else {
                dispatcherScope.coroutineContext.cancelChildren()
            }
        }

    var captureProtectDispatcher: Boolean = false
        set(value) {
            if (field && value) return
            field = value
            if (value) {
                protectScope.launch {
                    while (isActive) {
                        if (captureProtectDispatcher) {
                            if (updateProtect()) {
                                captureProtectDispatcher = false
                                protectDate = null
                            }
                        }
                        delay(1000)
                    }
                }
            } else {
                protectScope.coroutineContext.cancelChildren()
            }
        }

    var rewardAmount: Int = 64
        get() {
            val region = plugin.worldGuard.regionContainer[server.mainWorld]!!
                .getRegion(name) ?: return field
            return region.getFlag(plugin.rewardAmountFlag)!!
        }

    var rewardTime: Int = 3600
        get() {
            if (server.worlds.isEmpty()) return field
            val container = plugin.worldGuard.regionContainer[server.mainWorld] ?: return field
            if (container.regions.isEmpty()) return field
            val region = container.getRegion(name) ?: return field
            return region.getFlag(plugin.rewardTimeFlag)!!
        }

    var protectTime: Int = 3600
        get() {
            val region = plugin.worldGuard.regionContainer[server.mainWorld]!!
                .getRegion(name) ?: return field
            return region.getFlag(plugin.protectTimeFlag)!!
        }

    var captureTime: Int = 420
        get() {
            val region = plugin.worldGuard.regionContainer[server.mainWorld]!!
                .getRegion(name) ?: return field
            return region.getFlag(plugin.captureTimeFlag)!!
        }

    var startCaptureMessage: String = ""
        get() {
            val region = plugin.worldGuard.regionContainer[server.mainWorld]!!
                .getRegion(name) ?: return field
            return region.getFlag(plugin.startCaptureMessageFlag)!!
        }

    var endCaptureMessage: String = ""
        get() {
            val region = plugin.worldGuard.regionContainer[server.mainWorld]!!
                .getRegion(name) ?: return field
            return region.getFlag(plugin.endCaptureMessageFlag)!!
        }

    var owner: Owner? = null
        set(value) {
            if (value == null) {
                field?.remove()
                field?.rewardDispatcher = false
            }
            field = value
            if (!isLoad) return
            if (isInit) {
                value?.update()
                value?.rewardDispatcher = true
            }
        }

    var invader: Invader? = null
        set(value) {
            if (value == null) {
                field?.remove()
            }
            field = value
            if (!isLoad) return
            if (isInit)
                value?.update()
        }


    private fun updateOwner(player: Player) {
        val region = RegionPlayer(player).getProtectedRegionApplicablePlayer()
        if (region?.id != name) {

            if (player.uniqueId == invader?.uuid) {
                invader = null
                changePlayerLevel(player, 0.0, 0)
                return
            }

        } else {
            if (player.uniqueId == owner?.uuid) return

            if (invader?.uuid == null) {
                invader = Invader(this, player.uniqueId)
                server.broadcastMessage("${plugin.startCaptureColor}$startCaptureMessage ${player.name}")
                return
            }

            if (player.uniqueId == invader!!.uuid) {
                if (!updateInvader(player)) return
                protectDate = Date(Date().time)
                captureProtectDispatcher = true
                invader = null
                owner = Owner(this, player.uniqueId)
                server.broadcastMessage("${plugin.endCaptureColor}$endCaptureMessage ${player.name}")
            }
        }

    }

    private fun updateInvader(player: Player): Boolean {
        val date = invader?.date
        val currentDate = Date(Date().time)
        val result = ((currentDate.time.minus(date!!.time)) / 1000.0)
        changePlayerLevel(player, 0.0, captureTime - floor(result).toInt())
        if (result >= captureTime) return true
        return false
    }

    private fun updateProtect(): Boolean {
        val currentDate = Date(Date().time)
        val result = currentDate.time.minus(protectDate!!.time)
        if ((result / 1000 >= protectTime + 1)) return true
        return false
    }

    private fun sendRegionProtectStatus(player: Player) {
        val region = RegionPlayer(player).getProtectedRegionApplicablePlayer()
        if (region == null) {
            changePlayerLevel(player, 0.0, 0)
            return
        }
        if (region.id == name) {
            val currentDate = Date(Date().time)
            val result = ((currentDate.time.minus(protectDate!!.time)) / 1000.0)
            changePlayerLevel(player, 0.99, protectTime - floor(result).toInt())
        }
    }

    private fun changePlayerLevel(player: Player, exp: Double, level: Int) {
        player.level = level
        player.exp = exp.toFloat()
    }

    fun initializePoint(): Boolean {
        if (isInit) return false
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("INSERT INTO Points(POINT) VALUES(?)").use { statement ->
                statement.setString(1, name)
                statement.execute()
            }
        }
        isInit = true
        isLoad = true
        return true
    }

    fun removePoint() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("DELETE FROM Points WHERE POINT=?").use { statement ->
                statement.setString(1, name)
                statement.execute()
            }
        }
    }
}