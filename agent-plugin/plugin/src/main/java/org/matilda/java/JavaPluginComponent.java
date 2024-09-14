package org.matilda.java;

import dagger.Component;
import org.matilda.commands.CommandRegistry;
import org.matilda.commands.PluginDependenciesModule;
import org.matilda.java.generated.commands.CommandRegistryModule;

import javax.inject.Singleton;

@Component(modules = {CommandRegistryModule.class, PluginDependenciesModule.class})
@Singleton
public interface JavaPluginComponent {
    CommandRegistry commandRegistry();
}
