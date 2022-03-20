package liray.utils

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class Serialize() {

    fun toBase64(itemStack: ItemStack): String {
        val outputStream = ByteArrayOutputStream()
        BukkitObjectOutputStream(outputStream).use {
            it.writeObject(itemStack)
        }
        return Base64Coder.encodeLines(outputStream.toByteArray())
    }


    fun fromBase64(data: String): ItemStack {
        BukkitObjectInputStream(
            ByteArrayInputStream(Base64Coder.decodeLines(data))
        ).use {
            return it.readObject() as ItemStack
        }
    }

    fun handToBase64(name: String): String {
        val player = server.getPlayer(name)
        val outputStream = ByteArrayOutputStream()
        BukkitObjectOutputStream(outputStream).use {
            it.writeObject(player.itemInHand)
        }
        return Base64Coder.encodeLines(outputStream.toByteArray())
    }

}