package liray

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.StringFlag
import liray.listeners.EventHandler
import org.bukkit.plugin.java.JavaPlugin
import org.sqlite.SQLiteDataSource
import java.io.File
import javax.sql.DataSource

lateinit var plugin: CaptureTheFlagPlugin

class CaptureTheFlagPlugin : JavaPlugin(){

    val dataSource: DataSource = SQLiteDataSource().apply {
        url = "jdbc:sqlite:plugins/CaptureTheFlag/data/database.db"
    }
    val worldGuard: WorldGuardPlugin = WorldGuardPlugin.inst()
    val rustyStormProtectionFlag = StringFlag("capture")

    override fun onEnable() {
        plugin = this
        server.pluginManager.registerEvents(EventHandler, this)
        createDirection()
        initializeDataBase()
    }

    override fun onLoad() = worldGuard.flagRegistry.register(rustyStormProtectionFlag)

    private fun createDirection() {
        saveDefaultConfig()
        val dir = File("plugins/CaptureTheFlag/data/")
        if(dir.exists())
            dir.mkdirs()
    }

    private fun initializeDataBase() {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS Players(UUID bigint, TIME datetime)"
            ).use {
                it.executeUpdate()
            }
        }
    }

}