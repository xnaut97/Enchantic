package com.github.tezvn.enchantic.impl.command;

import com.github.tezvn.enchantic.api.EnchanticPlugin;
import com.github.tezvn.enchantic.api.item.ItemManager;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabExecutor;

public abstract class AbstractCommand implements CommandExecutor, TabExecutor {

    private final EnchanticPlugin plugin;

    public AbstractCommand(EnchanticPlugin plugin) {
        this.plugin = plugin;
    }

    public EnchanticPlugin getPlugin() {
        return plugin;
    }

    public ItemManager getItemManager() {
        return plugin.getItemManager();
    }

}
