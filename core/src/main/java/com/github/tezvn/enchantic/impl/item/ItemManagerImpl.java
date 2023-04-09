package com.github.tezvn.enchantic.impl.item;

import com.github.tezvn.enchantic.api.EnchanticPlugin;
import com.github.tezvn.enchantic.api.item.EnchanticItem;
import com.github.tezvn.enchantic.api.item.ItemManager;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

}
