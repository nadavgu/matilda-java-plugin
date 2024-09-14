package org.matilda.java

import org.matilda.commands.CommandRegistry
import org.matilda.commands.PluginDependencies
import org.matilda.commands.PluginDependenciesModule

object JavaPlugin {
    @JvmStatic
    fun createCommandRegistry(pluginDependencies: PluginDependencies): CommandRegistry {
        return DaggerJavaPluginComponent.builder()
            .pluginDependenciesModule(PluginDependenciesModule(pluginDependencies))
            .build()
            .commandRegistry()
    }
}
