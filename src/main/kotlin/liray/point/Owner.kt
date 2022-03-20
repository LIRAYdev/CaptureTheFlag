package liray.point

import com.github.shynixn.mccoroutine.minecraftDispatcher
import kotlinx.coroutines.*
import liray.plugin
import liray.utils.server
import java.sql.Date as SQLDate
import java.util.*

class Owner(
    val point: Point,
    val uuid: UUID,
    var date: SQLDate = SQLDate(Date().time)
) {

    private val rewardScope = CoroutineScope(plugin.minecraftDispatcher)

    var rewardDispatcher: Boolean = false
        set(value) {
            if (field && value) return
            field = value
            if (value) {
                rewardScope.launch {
                    while (isActive) {
                        updateReward()
                        delay(10000)
                    }
                }
            } else {
                rewardScope.coroutineContext.cancelChildren()
            }
        }

    fun update() {
        updateDate()
        updateUUID()
    }

    fun remove() {
        removeUUID()
        removeDate()
    }

    private fun updateReward() {
        val currentDate = java.sql.Date(Date().time)
        val result = ((currentDate.time.minus(date.time)) / 1000.0)
        if (result <= point.rewardTime) return
        date = currentDate
        updateDate()
        if (hasReward()) {
            plugin.dataSource.connection.use { connection ->
                connection.prepareStatement("UPDATE Rewards SET Amount=Amount+? WHERE UUID=?").use { statement ->
                    statement.setLong(1, point.rewardAmount.toLong())
                    statement.setString(2, uuid.toString())
                    statement.execute()
                }
            }
        } else {
            plugin.dataSource.connection.use { connection ->
                connection.prepareStatement("REPLACE INTO Rewards(Amount, UUID) VALUES(?,?)").use { statement ->
                    statement.setLong(1, point.rewardAmount.toLong())
                    statement.setString(2, uuid.toString())
                    statement.execute()
                }
            }
        }
    }

    private fun hasReward(): Boolean {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT * FROM Rewards WHERE UUID=?").use { statement ->
                statement.setString(1, uuid.toString())
                return statement.executeQuery().next()
            }
        }
    }
    
    private fun updateDate() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE Points SET OWNER_DATE=? WHERE POINT=?").use { statement ->
                statement.setDate(1, date)
                statement.setString(2, point.name)
                statement.execute()
            }
        }
    }

    private fun removeDate() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE Points SET OWNER_DATE=? WHERE POINT=?").use { statement ->
                statement.setString(1, null)
                statement.setString(2, point.name)
                statement.execute()
            }
        }
    }

    private fun updateUUID() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE Points SET OWNER_UUID=? WHERE POINT=?").use { statement ->
                statement.setString(1, uuid.toString())
                statement.setString(2, point.name)
                statement.execute()
            }
        }
    }

    private fun removeUUID() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE Points SET OWNER_UUID=? WHERE POINT=?").use { statement ->
                statement.setString(1, null)
                statement.setString(2, point.name)
                statement.execute()
            }
        }
    }

}