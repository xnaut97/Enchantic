package com.github.tezvn.enchantic.impl;

import com.github.tezvn.enchantic.api.EnchanticPlugin;
import com.github.tezvn.enchantic.api.item.ItemManager;
import com.github.tezvn.enchantic.impl.command.EnchanticCommand;
import com.github.tezvn.enchantic.impl.item.ItemManagerImpl;
import com.github.tezvn.enchantic.impl.utils.inventory.BaseInventory;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Level;

public final class EnchanticPluginImpl extends JavaPlugin implements EnchanticPlugin {

    private ItemManager itemManager;

    @Override
    public void onEnable() {
        BaseInventory.register(this);
        if(Bukkit.getPluginManager().getPlugin("AdvancedEnchantments") == null) {
            getLogger().severe("Could not found dependency plugin AdvancedEnchantments.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Hooked into AdvancedEnchantments.");
        saveResource("items.yml", false);
        this.itemManager = new ItemManagerImpl(this);
        setupCommands();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            ((ItemManagerImpl) this.itemManager).load();
        });
    }

    @Override
    public void onDisable() {
        BaseInventory.forceCloseAll();
    }

    @Override
    public ItemManager getItemManager() {
        return this.itemManager;
    }

    private void setupCommands() {
        PluginCommand command = getCommand("enchantic");
        if(command != null) {
            command.setExecutor(new EnchanticCommand(this));
            command.setTabCompleter(new EnchanticCommand(this));
        }
    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {
        if (resourcePath != null && !resourcePath.equals("")) {
            resourcePath = resourcePath.replace('\\', '/');
            InputStream in = this.getResource(resourcePath);
            if (in == null) {
                throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + getFile());
            } else {
                File outFile = new File(getDataFolder(), resourcePath);
                int lastIndex = resourcePath.lastIndexOf(47);
                File outDir = new File(getDataFolder(), resourcePath.substring(0, lastIndex >= 0 ? lastIndex : 0));
                if (!outDir.exists()) {
                    outDir.mkdirs();
                }

                try {
                    if (outFile.exists() && !replace) {
                    } else {
                        OutputStream out = new FileOutputStream(outFile);
                        byte[] buf = new byte[1024];

                        int len;
                        while((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        out.close();
                        in.close();
                    }
                } catch (IOException var10) {
                    this.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
                }

            }
        } else {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }
    }
}
