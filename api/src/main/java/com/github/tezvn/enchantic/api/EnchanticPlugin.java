package com.github.tezvn.enchantic.api;

import com.github.tezvn.enchantic.api.item.ItemManager;
import org.bukkit.plugin.Plugin;

public interface EnchanticPlugin extends Plugin {

    ItemManager getItemManager();

}
