package liray.utils

import com.sk89q.worldguard.bukkit.RegionContainer
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import liray.plugin
import org.bukkit.entity.Player


object Extensions {

    private val regionContainer: RegionContainer = plugin.worldGuard.regionContainer

    fun Player.inProtectedRegion(): Boolean {
        val regionManager = regionContainer[location.world] ?: return false
        val applicableRegions = regionManager.getApplicableRegions(location)
        return applicableRegions.any { it.hasFlag() }
    }

    private fun ProtectedRegion.hasFlag(): Boolean {
        return getFlag(plugin.rustyStormProtectionFlag).toBoolean()
    }

}