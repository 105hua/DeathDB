/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathdb.commands

import com.nearvanilla.deathdb.DeathDB
import com.nearvanilla.deathdb.libs.Serialization
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.incendo.cloud.annotations.Argument
import org.incendo.cloud.annotations.Command
import org.incendo.cloud.annotations.CommandDescription
import org.incendo.cloud.annotations.Permission
import org.incendo.cloud.annotations.processing.CommandContainer

@CommandContainer
class RestoreInventory {
    @CommandDescription("Restores the inventory of a given player.")
    @Command("restoreinventory|ri <player_name> <index>")
    @Permission("deathdb.restoreinventory")
    @Suppress("unused")
    fun restoreInventoryCommand(sourceStack: CommandSourceStack, @Argument(value = "player_name") playerName: String?, @Argument(value = "index") index: Int) {
        val sender: CommandSender = sourceStack.sender
        // If sender is not player.
        if (sender !is Player) {
            val noPlayerMsg = Component.text("This command can only be ran by players.")
            sender.sendMessage(noPlayerMsg)
            return
        }
        val player: Player = sender
        if (playerName == null) {
            val noPlayerMsg = Component.text("Please provide a player name.")
            player.sendMessage(noPlayerMsg)
            return
        }
        val targetPlayer = DeathDB.pluginInstance.server.getOfflinePlayer(playerName).player
        // If target is offline.
        if (targetPlayer == null) {
            val cantFindPlayerMessage = Component.text(
                "Can't find that player, are they online?",
                NamedTextColor.RED,
            )
            player.sendMessage(cantFindPlayerMessage)
            return
        }
        // Restore Inventory.
        val results = DeathDB.dbWrapper.getPlayerInformation(targetPlayer)
        var resultIndex = 1
        var serializedInventory: String? = null
        while (results.next()) {
            if (resultIndex == index) {
                serializedInventory = results.getString("serializedInventory")
                break
            }
            resultIndex += 1
        }
        if (serializedInventory == null) {
            val noInventoryMsg = Component.text(
                "No inventory found.",
                NamedTextColor.RED,
            )
            player.sendMessage(noInventoryMsg)
            return
        }
        val targetOnlinePlayer = targetPlayer.player
        if (targetOnlinePlayer == null) {
            val invalidPlayerMsg = Component.text(
                "Failed to find the player.",
                NamedTextColor.RED,
            )
            player.sendMessage(invalidPlayerMsg)
            return
        }
        val deserializedInventory = Serialization.deserialize(serializedInventory)
        val currentInventory = targetOnlinePlayer.inventory.contents
        targetOnlinePlayer.inventory.clear()
        for (item in currentInventory) {
            if (item != null) {
                targetOnlinePlayer.world.dropItem(targetOnlinePlayer.location, item)
            }
        }
        targetOnlinePlayer.inventory.contents = deserializedInventory
        val successMsg = Component.text(
            "Successfully restored inventory.",
            NamedTextColor.GREEN,
        )
        player.sendMessage(successMsg)
        return
    }
}
