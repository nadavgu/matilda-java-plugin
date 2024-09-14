package org.matilda.java

import org.matilda.commands.CommandRegistry
import org.matilda.commands.PluginDependenciesModule

object JavaPlugin {
    @JvmStatic
    fun createCommandRegistry(pluginDependenciesModule: PluginDependenciesModule): CommandRegistry {
        return DaggerJavaPluginComponent.builder()
            .pluginDependenciesModule(pluginDependenciesModule)
            .build()
            .commandRegistry()
    }
}
