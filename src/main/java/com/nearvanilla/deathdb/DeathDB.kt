/* Licensed under GNU General Public License v3.0 */
package com.nearvanilla.deathdb

import com.nearvanilla.deathdb.commands.RestoreInventory
import com.nearvanilla.deathdb.commands.ShowDeaths
import com.nearvanilla.deathdb.events.OnPlayerDeath
import com.nearvanilla.deathdb.exceptions.DeathDBException
import com.nearvanilla.deathdb.libs.DatabaseWrapper
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.plugin.java.JavaPlugin
import org.incendo.cloud.annotations.AnnotationParser
import org.incendo.cloud.execution.ExecutionCoordinator
import org.incendo.cloud.paper.PaperCommandManager
import java.io.File
import java.util.logging.Logger

@Suppress("UNUSED") // Main class comes up as unused when it actually is, Kotlin dumb si
class DeathDB : JavaPlugin() {

    companion object {
        lateinit var pluginLogger: Logger
            private set
        lateinit var dbWrapper: DatabaseWrapper
            private set
        lateinit var pluginInstance: DeathDB
            private set
        fun isLoggerInitialized(): Boolean {
            return Companion::pluginLogger.isInitialized
        }
        fun isWrapperInitialized(): Boolean {
            return Companion::dbWrapper.isInitialized
        }
        fun isPluginInstanceInitialized(): Boolean {
            return Companion::pluginInstance.isInitialized
        }
    }

    // Cloud Stuff
    private lateinit var commandManager: PaperCommandManager<CommandSourceStack>
    private lateinit var annotationParser: AnnotationParser<CommandSourceStack>

    override fun onEnable() {
        logger.info("Setting up DeathDB...")
        commandManager = PaperCommandManager.builder()
            .executionCoordinator(ExecutionCoordinator.simpleCoordinator())
            .buildOnEnable(this)
        annotationParser = AnnotationParser(
            commandManager,
            CommandSourceStack::class.java,
        )
        annotationParser.parse(RestoreInventory())
        annotationParser.parse(ShowDeaths())
        pluginLogger = logger
        pluginInstance = this
        if (!dataFolder.exists()) {
            dataFolder.mkdir()
        }
        val dbPath = dataFolder.absolutePath + "/data.db"
        val dbFile = File(dbPath)
        if (!dbFile.exists()) {
            dbFile.createNewFile()
        }
        dbWrapper = DatabaseWrapper(dbPath)
        dbWrapper.createDeathsTable()
        server.pluginManager.registerEvents(OnPlayerDeath(), this)
        if (!isWrapperInitialized() || !isLoggerInitialized() || !isPluginInstanceInitialized()) {
            throw DeathDBException("The Database Wrapper, Logger or Plugin Instance has not initialized properly.")
        }
        logger.info("DeathDB has been enabled, enjoy!") // Log that plugin is enabled si.
    }

    override fun onDisable() {
        logger.info("DeathDB has been disabled, goodbye!")
    }
}
