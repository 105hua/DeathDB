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
            val deathRecords = DeathDB.dbWrapper.getPlayerInformation(targetPlayer as Player)
            var deathListMsg = Component.text(
                "List of Deaths\n===============\n",
                NamedTextColor.GRAY,
            )
            for (deathRecord in deathRecords) {
                deathListMsg = deathListMsg.append(deathRecord)
            }
            player.sendMessage(deathListMsg)
            return
        }
    }
}
