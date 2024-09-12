/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathdb.commands

import com.nearvanilla.deathdb.DeathDB
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.OfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@CommandContainer
class ShowDeaths {
    @CommandDescription("Show the most recent deaths of a player.")
    @Command("showdeaths|sd <player_name>")
    @Permission("deathdb.showdeaths")
    @Suppress("unused")
    fun showDeathsCommand(sourceStack: CommandSourceStack, @Argument(value = "player_name") playerName: String) {
        val sender: CommandSender = sourceStack.sender
        if (sender !is Player) {
            val noPlayerMsg = Component.text("This command can only be ran by players.")
            sender.sendMessage(noPlayerMsg)
            return
        }
        val player: Player = sender
        val targetPlayer: OfflinePlayer = DeathDB.pluginInstance.server.getOfflinePlayer(playerName)
        if (!targetPlayer.hasPlayedBefore() && !targetPlayer.isOnline) {
            val neverPlayedMsg = Component.text(
                "This player has never been on this server before.",
                NamedTextColor.RED,
            )
            player.sendMessage(neverPlayedMsg)
            return
        } else {
            val results = DeathDB.dbWrapper.getPlayerInformation(targetPlayer as Player)
            var deathListMsg = Component.text(
                "List of Deaths\n===============\n",
                NamedTextColor.GRAY,
            )
            var index = 1
            while (results.next()) {
                val dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(results.getInt("timeOfDeath").toLong()), ZoneId.systemDefault())
                val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy @ HH:mm")
                val formattedDateTime = dateTime.format(formatter)
                val posX = String.format("%.3f", results.getDouble("posX"))
                val posY = String.format("%.3f", results.getDouble("posY"))
                val posZ = String.format("%.3f", results.getDouble("posZ"))
                val formattedPosition = "$posX, $posY, $posZ"
                // TODO Make clickable button to go to inventory.
                val entryComponent = Component.text(
                    "$index) $formattedDateTime | $formattedPosition\n",
                    NamedTextColor.GRAY,
                )
                index += 1
                deathListMsg = deathListMsg.append(entryComponent)
            }
            player.sendMessage(deathListMsg)
            return
        }
    }
}
