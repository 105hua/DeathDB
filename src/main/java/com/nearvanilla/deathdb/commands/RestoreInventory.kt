/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathdb.commands

import com.nearvanilla.deathdb.DeathDB
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
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
        // Get the inventory to restore.
        val inventoryToRestore = DeathDB.dbWrapper.getInventoryFromRecord(targetPlayer, index)
        if (inventoryToRestore == null) {
            val noInventoryMessage = Component.text(
                "No inventory found for that index.",
                NamedTextColor.RED,
            )
            player.sendMessage(noInventoryMessage)
            return
        }
        // Drop the current inventory.
        targetPlayer.inventory.contents.forEach { item: ItemStack? ->
            if (item != null) {
                targetPlayer.world.dropItem(targetPlayer.location, item)
            }
        }
        targetPlayer.inventory.contents = inventoryToRestore
        val successMsg = Component.text(
            "Successfully restored inventory.",
            NamedTextColor.GREEN,
        )
        player.sendMessage(successMsg)
        return
    }
}
