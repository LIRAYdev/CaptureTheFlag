package liray

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.BooleanFlag
import com.sk89q.worldguard.protection.flags.IntegerFlag
import com.sk89q.worldguard.protection.flags.StringFlag
import kotlinx.coroutines.cancel
import liray.commands.AdminCaptureCommand
import liray.commands.PlayerCaptureCommand
import liray.point.Invader
import liray.point.Owner
import liray.point.Point
import liray.utils.Serialize
import org.bukkit.plugin.java.JavaPlugin
import org.sqlite.SQLiteDataSource
import java.io.File
import java.util.*
import javax.sql.DataSource

lateinit var plugin: CaptureTheFlagPlugin

class CaptureTheFlagPlugin : JavaPlugin() {

    val startCaptureColor: String = this.config.getString("startCaptureColor")
    val endCaptureColor: String = this.config.getString("endCaptureColor")

    val points = mutableMapOf<String, Point>()
    val dataSource: DataSource = SQLiteDataSource().apply {
        url = "jdbc:sqlite:plugins/CaptureTheFlag/data/database.db"
    }

    val worldGuard: WorldGuardPlugin = WorldGuardPlugin.inst()
    val pointFlag = BooleanFlag("point")
    val protectTimeFlag = IntegerFlag("protect-time")
    val captureTimeFlag = IntegerFlag("capture-time")
    val startCaptureMessageFlag = StringFlag("start-capture-message")
    val endCaptureMessageFlag = StringFlag("end-capture-message")
    val rewardTimeFlag = IntegerFlag("reward-time")
    val rewardAmountFlag = IntegerFlag("reward-amount")
    val serialize = Serialize()

    override fun onEnable() {
        plugin = this
        getCommand("CTF").executor = AdminCaptureCommand
        getCommand("flag").executor = PlayerCaptureCommand
        createDirection()
        initializeDataBase()
        cleanNull()
        loadPoints()
    }

    override fun onDisable() {
        points.forEach {
            it.value.captureDispatcher = false
            it.value.captureProtectDispatcher = false
            it.value.owner?.rewardDispatcher = false
        }
    }

    override fun onLoad() {
        worldGuard.flagRegistry.register(pointFlag)
        worldGuard.flagRegistry.register(protectTimeFlag)
        worldGuard.flagRegistry.register(captureTimeFlag)
        worldGuard.flagRegistry.register(startCaptureMessageFlag)
        worldGuard.flagRegistry.register(endCaptureMessageFlag)
        worldGuard.flagRegistry.register(rewardTimeFlag)
        worldGuard.flagRegistry.register(rewardAmountFlag)
    }

    private fun createDirection() {
        saveDefaultConfig()
        val dir = File("plugins/CaptureTheFlag/data/")
        dir.mkdirs()
    }

    private fun initializeDataBase() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS Points(POINT TEXT, OWNER_UUID BIGINT, INVADER_UUID BIGINT, OWNER_DATE DATETIME, INVADER_DATE DATETIME)"
            ).use { statement ->
                statement.executeUpdate()
            }
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS Rewards(UUID BIGINT, Amount BIGINT)"
            ).use { statement ->
                statement.executeUpdate()
            }
        }
    }

    private fun cleanNull() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("DELETE FROM Points WHERE POINT IS NULL").use { statement ->
                statement.execute()
            }
            connection.prepareStatement("DELETE FROM Rewards WHERE UUID IS NULL").use { statement ->
                statement.execute()
            }
        }
    }

    private fun loadPoints() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT * FROM Points").use { statement ->
                val result = statement.executeQuery()
                while (result.next()) {

                    val point = Point(result.getString("POINT"))
                    val ownerUUID = result.getString("OWNER_UUID")
                    val invaderUUID = result.getString("INVADER_UUID")

                    val ownerDate = result.getDate("OWNER_DATE")
                    val invaderDate = result.getDate("INVADER_DATE")

                    point.isInit = true
                    point.captureDispatcher = true
                    if (ownerUUID != null) {
                        point.owner = Owner(point, UUID.fromString(ownerUUID))
                        point.owner?.date = ownerDate
                        point.owner?.rewardDispatcher = true
                    }
                    if (invaderUUID != null) {
                        point.invader = Invader(point, UUID.fromString(invaderUUID))
                        point.owner?.date = invaderDate
                    }
                    point.isLoad = true
                    points[point.name] = point
                }

            }
        }
    }

}