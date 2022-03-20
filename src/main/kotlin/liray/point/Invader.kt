package liray.point

import liray.plugin
import java.sql.Date as SQLDate
import java.util.*

class Invader(
    val point: Point,
    val uuid: UUID,
    val date: java.sql.Date = SQLDate(Date().time)
) {

    fun update() {
        updateDate()
        updateUUID()
    }

    fun remove() {
        removeUUID()
        removeDate()
    }

    private fun updateUUID() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE Points SET INVADER_UUID=? WHERE POINT=?").use { statement ->
                statement.setString(1, uuid.toString())
                statement.setString(2, point.name)
                statement.execute()
            }
        }
    }

    private fun removeUUID() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE Points SET INVADER_UUID=? WHERE POINT=?").use { statement ->
                statement.setString(1, null)
                statement.setString(2, point.name)
                statement.execute()
            }
        }
    }

    private fun updateDate() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE Points SET INVADER_DATE=? WHERE POINT=?").use { statement ->
                statement.setDate(1, date)
                statement.setString(2, point.name)
                statement.execute()
            }
        }
    }

    private fun removeDate() {
        plugin.dataSource.connection.use { connection ->
            connection.prepareStatement("UPDATE Points SET INVADER_DATE=? WHERE POINT=?").use { statement ->
                statement.setString(1, null)
                statement.setString(2, point.name)
                statement.execute()
            }
        }
    }

}