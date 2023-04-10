package com.github.tezvn.enchantic.impl.item;

import com.cryptomorin.xseries.XMaterial;
import com.github.tezvn.enchantic.api.EnchanticPlugin;
import com.github.tezvn.enchantic.api.item.EnchanticItem;
import com.github.tezvn.enchantic.api.item.ItemManager;
import com.github.tezvn.enchantic.impl.utils.ItemCreator;
import com.google.common.collect.Maps;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

public class ItemManagerImpl implements ItemManager {

    private final Map<String, EnchanticItem> items = Maps.newHashMap();

    private final EnchanticPlugin plugin;

    public ItemManagerImpl(EnchanticPlugin plugin) {
        this.plugin = plugin;
    }

    public EnchanticPlugin getPlugin() {
        return plugin;
    }

    @Override
    public List<EnchanticItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(this.items.values()));
    }

    @Override
    @Nullable
    public EnchanticItem getItem(String id) {
        return this.items.getOrDefault(id, null);
    }

    public void load() {
        File file = new File(plugin.getDataFolder() + "/items.yml");
        if(!file.exists()) {
            plugin.getLogger().severe("Could not load items due to 'items.yml' not found");
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.getKeys(false).forEach(id -> {
            XMaterial material = XMaterial.matchXMaterial(config.getString(id + ".material", "STICK"))
                    .orElse(XMaterial.STICK);
            ItemCreator creator = new ItemCreator(Objects.requireNonNull(
                    XMaterial.matchXMaterial(config.getString(id + ".material", "STICK"))
                            .orElse(XMaterial.STICK).parseItem()));
            if(material == XMaterial.PLAYER_HEAD) {
                
            }

        });
    }

}
