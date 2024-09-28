/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathdb.libs

import com.nearvanilla.deathdb.DeathDB
import com.nearvanilla.deathdb.exceptions.DeathDBException
import com.zaxxer.hikari.HikariDataSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class DatabaseWrapper(dbPath: String) {

    private val hikariDataSource = HikariDataSource().apply {
        jdbcUrl = "jdbc:sqlite:$dbPath"
        connectionTestQuery = "SELECT 1"
        maximumPoolSize = 10
    }

    fun createDeathsTable() {
        try {
            val databaseConnection = hikariDataSource.connection
            val createTableStmt =
                databaseConnection.prepareStatement("CREATE TABLE IF NOT EXISTS deaths(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, uniqueId TEXT NOT NULL, timeOfDeath INTEGER NOT NULL, posX REAL NOT NULL, posY REAL NOT NULL, posZ REAL NOT NULL, worldName TEXT NOT NULL, serializedInventory TEXT NOT NULL)")
                    ?: throw DeathDBException("Failed to prepare the create table statement.")
            createTableStmt.execute()
            databaseConnection.close()
        } catch (e: Exception) {
            throw DeathDBException("Failed to create the deaths table.")
        }
    }

    fun addDeathRecord(playerWhoDied: Player) {
        val databaseConnection = hikariDataSource.connection
        val addDeathStmt = databaseConnection.prepareStatement("INSERT INTO deaths VALUES(NULL, ?, ?, ?, ?, ?, ?, ?)") ?: throw DeathDBException("Failed to prepare the add death statement.")
        addDeathStmt.setString(1, playerWhoDied.uniqueId.toString())
        addDeathStmt.setLong(2, Instant.now().epochSecond)
        addDeathStmt.setDouble(3, playerWhoDied.location.x)
        addDeathStmt.setDouble(4, playerWhoDied.location.y)
        addDeathStmt.setDouble(5, playerWhoDied.location.z)
        addDeathStmt.setString(6, playerWhoDied.world.name)
        addDeathStmt.setString(7, Serialization.serialize(playerWhoDied.inventory.contents))
        addDeathStmt.execute()
        databaseConnection.close()
    }

    fun getPlayerInformation(player: Player): List<TextComponent> {
        val databaseConnection = hikariDataSource.connection
        DeathDB.pluginLogger.info("Getting player information for ${player.name}")
        DeathDB.pluginLogger.info("Unique ID: ${player.uniqueId}")
        val getPlayerInfoStmt = databaseConnection.prepareStatement("SELECT * FROM deaths WHERE uniqueId = ? ORDER BY timeOfDeath DESC LIMIT 5") ?: throw DeathDBException("Failed to prepare the get player info statement.")
        getPlayerInfoStmt.setString(1, player.uniqueId.toString())
        val results = getPlayerInfoStmt.executeQuery() // Returns HikariProxyResultSet, which is a ResultSet
        val deathList = mutableListOf<TextComponent>()
        var index = 1
        while (results.next()) {
            val dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(
                    results.getInt("timeOfDeath").toLong(),
                ),
                ZoneId.systemDefault(),
            )
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy @ HH:mm")
            val formattedDateTime = dateTime.format(formatter)
            val posX = String.format("%.3f", results.getDouble("posX"))
            val posY = String.format("%.3f", results.getDouble("posY"))
            val posZ = String.format("%.3f", results.getDouble("posZ"))
            val formattedPosition = "$posX, $posY, $posZ"
            val entryComponent = Component.text(
                "$index) $formattedDateTime | $formattedPosition\n",
                NamedTextColor.GRAY,
            )
            index += 1
            deathList.add(entryComponent)
        }
        databaseConnection.close()
        return deathList
    }

    fun getInventoryFromRecord(player: Player, index: Int): Array<ItemStack>? {
        val databaseConnection = hikariDataSource.connection
        val getInventoryStmt = databaseConnection.prepareStatement(
            "SELECT serializedInventory FROM deaths WHERE uniqueId = ? ORDER BY timeOfDeath DESC LIMIT 5",
        ) ?: throw DeathDBException("Failed to prepare the get inventory statement.")
        getInventoryStmt.setString(1, player.uniqueId.toString())
        val results = getInventoryStmt.executeQuery()
        var deathIndex = 1
        var serializedInventory: String? = null
        while (results.next()) {
            if (index == deathIndex) {
                serializedInventory = results.getString("serializedInventory")
                break
            }
            deathIndex += 1
        }
        databaseConnection.close()
        return if (serializedInventory != null) {
            Serialization.deserialize(serializedInventory)
        } else {
            null
        }
    }
}
